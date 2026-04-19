package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.CatalogEventPort;
import com.backandwhite.application.port.out.ProductBrowsePort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.application.usecase.ProductUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.domain.valueobject.ProductStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class ProductUseCaseImpl implements ProductUseCase {

    private static final String PRODUCT_ENTITY = "Product";

    private final ProductRepository productRepository;
    private final CatalogEventPort catalogEventPort;
    private final ProductSearchIndexPort productSearchIndexPort;
    /** Optional — only present when Elasticsearch is enabled. */
    private final ObjectProvider<ProductBrowsePort> productBrowsePortProvider;

    public ProductUseCaseImpl(ProductRepository productRepository, CatalogEventPort catalogEventPort,
            ProductSearchIndexPort productSearchIndexPort,
            ObjectProvider<ProductBrowsePort> productBrowsePortProvider) {
        this.productRepository = productRepository;
        this.catalogEventPort = catalogEventPort;
        this.productSearchIndexPort = productSearchIndexPort;
        this.productBrowsePortProvider = productBrowsePortProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategoryId(String categoryId, String locale, String status) {
        ProductStatus ps = parseStatus(status);
        return productRepository.findByCategoryId(categoryId, locale, ps);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAllPaged(String locale, String categoryId, String status, String name, int page, int size,
            String sortBy, boolean ascending) {
        ProductStatus ps = parseStatus(status);
        // sortBy=random → ORDER BY random() at DB level; returns a fresh
        // page drawn from the full matching set instead of the first N
        // rows by createdAt. Ignores `page` and `ascending`.
        if ("random".equalsIgnoreCase(sortBy)) {
            List<Product> sample = productRepository.findRandomSample(locale, categoryId, ps, size);
            return new PageImpl<>(sample, PageRequest.of(0, Math.max(size, 1)), sample.size());
        }
        // When Elasticsearch is available and the caller isn't doing a text
        // search by name, browse via ES (fast, scales to big catalogues) and
        // then hydrate rows from Postgres so translations for the requested
        // locale are applied from the source of truth.
        if ((name == null || name.isBlank())) {
            Page<Product> fromEs = tryBrowseFromEs(locale, categoryId, ps, sortBy, page, size);
            if (fromEs != null)
                return fromEs;
        }
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAllPaged(locale, categoryId, ps, name, pageable);
    }

    /**
     * Attempts to resolve the listing through the Elasticsearch browse port.
     * Returns {@code null} when ES is not wired (so the caller falls back to
     * Postgres) or any ES error occurs.
     */
    private Page<Product> tryBrowseFromEs(String locale, String categoryId, ProductStatus status, String sortBy,
            int page, int size) {
        ProductBrowsePort port = productBrowsePortProvider.getIfAvailable();
        if (port == null)
            return null;
        // The browse index holds PUBLISHED docs only; respect that constraint.
        if (status != null && status != ProductStatus.PUBLISHED)
            return null;
        try {
            ProductBrowsePort.BrowseCriteria criteria = new ProductBrowsePort.BrowseCriteria(categoryId, null, null,
                    null, null, null, sortBy, page, size);
            ProductBrowsePort.BrowsePage result = port.browse(criteria);
            List<Product> hydrated = productRepository.findByIdsInOrder(result.ids(), locale);
            return new PageImpl<>(hydrated, PageRequest.of(result.currentPage(), Math.max(result.size(), 1)),
                    result.totalElements());
        } catch (Exception e) {
            log.warn("Elasticsearch browse failed, falling back to Postgres: {}", e.getMessage());
            return null;
        }
    }

    private ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank())
            return null;
        try {
            return ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(String productId, String locale) {
        return productRepository.findById(productId, locale)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(PRODUCT_ENTITY, productId));
    }

    @Override
    @Transactional
    public Product create(Product product) {
        Product saved = productRepository.save(product);
        // Publish product.created event (L-12)
        catalogEventPort.publishProductCreated(saved.getId(), saved.getName(), saved.getSku(), saved.getSellPrice(),
                saved.getCategoryId(), null);
        productSearchIndexPort.indexProduct(saved);
        return saved;
    }

    @Override
    @Transactional
    public Product update(String productId, Product product) {
        Product updated = productRepository.update(productId, product);
        // Publish product.updated event (L-12)
        catalogEventPort.publishProductUpdated(updated.getId(), updated.getName(), updated.getSellPrice(),
                updated.getCategoryId(), null, updated.getStatus() == ProductStatus.PUBLISHED);
        productSearchIndexPort.indexProduct(updated);
        return updated;
    }

    @Override
    @Transactional
    public void publishProduct(String productId) {
        Product product = productRepository.findById(productId, null)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(PRODUCT_ENTITY, productId));
        ProductStatus newStatus = product.getStatus() == ProductStatus.PUBLISHED
                ? ProductStatus.DRAFT
                : ProductStatus.PUBLISHED;
        productRepository.updateStatus(productId, newStatus);
        productSearchIndexPort.updateStatus(productId, newStatus);
    }

    @Override
    @Transactional
    public void linkBrand(String productId, String brandId) {
        ensureExists(productId);
        productRepository.updateBrand(productId, brandId);
    }

    @Override
    @Transactional
    public void linkWarranty(String productId, String warrantyId) {
        ensureExists(productId);
        productRepository.updateWarranty(productId, warrantyId);
    }

    @Override
    @Transactional
    public void linkCategory(String productId, String categoryId) {
        ensureExists(productId);
        productRepository.updateCategory(productId, categoryId);
    }

    private void ensureExists(String productId) {
        if (!productRepository.existsById(productId)) {
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound(PRODUCT_ENTITY, productId);
        }
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(List<String> productIds, String status) {
        if (productIds == null || productIds.isEmpty())
            return;
        ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
        productRepository.bulkUpdateStatus(productIds, productStatus);
        productIds.forEach(id -> productSearchIndexPort.updateStatus(id, productStatus));
    }

    @Override
    @Transactional
    public void delete(String productId) {
        productRepository.delete(productId);
        productSearchIndexPort.removeProduct(productId);
    }

    @Override
    @Transactional
    public void deleteAll(List<String> productIds) {
        productRepository.deleteAll(productIds);
        productSearchIndexPort.removeBulk(productIds);
    }

    @Override
    @Transactional
    public BulkImportResult bulkCreate(List<Product> products) {
        int created = 0;
        List<BulkImportResult.RowError> errors = new ArrayList<>();
        List<Product> savedProducts = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            try {
                Product saved = productRepository.save(products.get(i));
                savedProducts.add(saved);
                created++;
            } catch (Exception e) {
                log.warn("Bulk product row {} failed: {}", i, e.getMessage());
                errors.add(BulkImportResult.RowError.builder().row(i).message(e.getMessage()).build());
            }
        }

        if (!savedProducts.isEmpty()) {
            productSearchIndexPort.indexBulk(savedProducts);
        }

        log.info("Bulk product import: created={}, failed={}, total={}", created, errors.size(), products.size());
        return BulkImportResult.builder().created(created).failed(errors.size()).totalRows(products.size())
                .errors(errors).build();
    }
}
