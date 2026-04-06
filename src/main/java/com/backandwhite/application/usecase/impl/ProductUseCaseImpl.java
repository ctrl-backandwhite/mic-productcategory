package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.ProductUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.application.port.out.CatalogEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductUseCaseImpl implements ProductUseCase {

    private final ProductRepository productRepository;
    private final CatalogEventPort catalogEventPort;

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
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAllPaged(locale, categoryId, ps, name, pageable);
    }

    private ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank())
            return null;
        try {
            return ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(String productId, String locale) {
        return productRepository.findById(productId, locale)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", productId));
    }

    @Override
    @Transactional
    public Product create(Product product) {
        Product saved = productRepository.save(product);
        // Publish product.created event (L-12)
        catalogEventPort.publishProductCreated(
                saved.getId(), saved.getName(), saved.getSku(),
                saved.getSellPrice(), saved.getCategoryId(), null);
        return saved;
    }

    @Override
    @Transactional
    public Product update(String productId, Product product) {
        Product updated = productRepository.update(productId, product);
        // Publish product.updated event (L-12)
        catalogEventPort.publishProductUpdated(
                updated.getId(), updated.getName(), updated.getSellPrice(),
                updated.getCategoryId(), null,
                updated.getStatus() == ProductStatus.PUBLISHED);
        return updated;
    }

    @Override
    @Transactional
    public void publishProduct(String productId) {
        Product product = productRepository.findById(productId, null)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", productId));
        ProductStatus newStatus = product.getStatus() == ProductStatus.PUBLISHED
                ? ProductStatus.DRAFT
                : ProductStatus.PUBLISHED;
        productRepository.updateStatus(productId, newStatus);
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(List<String> productIds, String status) {
        if (productIds == null || productIds.isEmpty())
            return;
        ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
        productRepository.bulkUpdateStatus(productIds, productStatus);
    }

    @Override
    @Transactional
    public void delete(String productId) {
        productRepository.delete(productId);
    }

    @Override
    @Transactional
    public void deleteAll(List<String> productIds) {
        productRepository.deleteAll(productIds);
    }

    @Override
    @Transactional
    public BulkImportResult bulkCreate(List<Product> products) {
        int created = 0;
        List<BulkImportResult.RowError> errors = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            try {
                productRepository.save(products.get(i));
                created++;
            } catch (Exception e) {
                log.warn("Bulk product row {} failed: {}", i, e.getMessage());
                errors.add(BulkImportResult.RowError.builder()
                        .row(i)
                        .message(e.getMessage())
                        .build());
            }
        }

        log.info("Bulk product import: created={}, failed={}, total={}", created, errors.size(), products.size());
        return BulkImportResult.builder()
                .created(created)
                .failed(errors.size())
                .totalRows(products.size())
                .errors(errors)
                .build();
    }
}
