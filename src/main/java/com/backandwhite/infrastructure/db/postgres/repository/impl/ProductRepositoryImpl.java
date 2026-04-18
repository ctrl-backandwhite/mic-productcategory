package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductTranslation;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationId;
import com.backandwhite.infrastructure.db.postgres.mapper.ProductInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.CategoryJpaRepository;
import com.backandwhite.infrastructure.db.postgres.repository.ProductJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.ProductSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private static final String ENTITY_NAME = "Product";

    private final ProductJpaRepository productJpaRepository;
    private final ProductInfraMapper productInfraMapper;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public List<Product> findByCategoryId(String categoryId, String locale, ProductStatus status) {
        List<String> categoryIds = categoryJpaRepository.findDescendantIds(categoryId);
        return productJpaRepository
                .findAll(ProductSpecification.byLocaleAndCategoryIds(locale,
                        categoryIds.isEmpty() ? List.of(categoryId) : categoryIds, status))
                .stream().map(productInfraMapper::toDomain).map(product -> filterTranslations(product, locale))
                .toList();
    }

    @Override
    public Page<Product> findAllPaged(String locale, String categoryId, ProductStatus status, String name,
            Pageable pageable) {
        List<String> categoryIds = null;
        if (categoryId != null && !categoryId.isBlank()) {
            List<String> descendants = categoryJpaRepository.findDescendantIds(categoryId);
            categoryIds = descendants.isEmpty() ? List.of(categoryId) : descendants;
        }
        return productJpaRepository
                .findAll(ProductSpecification.byLocaleAndCategoryIds(locale, categoryIds, status, name), pageable)
                .map(productInfraMapper::toDomain).map(product -> filterTranslations(product, locale));
    }

    @Override
    public Optional<Product> findById(String productId, String locale) {
        return productJpaRepository.findById(productId).map(productInfraMapper::toDomain)
                .map(product -> filterTranslations(product, locale));
    }

    @Override
    public boolean existsById(String productId) {
        return productJpaRepository.existsById(productId);
    }

    @Override
    public Product save(Product product) {
        String newId = product.getId() != null ? product.getId() : UUID.randomUUID().toString().toUpperCase();
        product.setId(newId);
        productJpaRepository.save(productInfraMapper.toEntityWithChildren(product));
        return findById(newId, resolveLocale(product))
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, newId));
    }

    @Override
    public Product update(String productId, Product product) {
        ProductEntity entity = productJpaRepository.findById(productId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, productId));

        applyFieldsToEntity(entity, product);
        upsertTranslations(entity, product.getTranslations());
        productJpaRepository.save(entity);

        return findById(productId, resolveLocale(product))
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, productId));
    }

    @Override
    public void delete(String productId) {
        ProductEntity entity = productJpaRepository.findById(productId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, productId));
        productJpaRepository.delete(entity);
    }

    @Override
    public void deleteAll(List<String> productIds) {
        if (productIds == null || productIds.isEmpty())
            return;
        List<ProductEntity> entities = productJpaRepository.findAllByIdIn(productIds);
        productJpaRepository.deleteAll(entities);
    }

    @Override
    public void enrichDetail(String productId, String description, String productImageSet) {
        ProductEntity entity = productJpaRepository.findById(productId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, productId));
        if (description != null)
            entity.setDescription(description);
        if (productImageSet != null)
            entity.setProductImageSet(productImageSet);
        productJpaRepository.save(entity);
    }

    @Override
    public void updateStatus(String productId, ProductStatus status) {
        ProductEntity entity = productJpaRepository.findById(productId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, productId));
        entity.setStatus(status);
        productJpaRepository.save(entity);
    }

    @Override
    public void bulkUpdateStatus(List<String> productIds, ProductStatus status) {
        if (productIds == null || productIds.isEmpty())
            return;
        productJpaRepository.bulkUpdateStatus(productIds, status);
    }

    @Override
    public Page<String> findAllProductIds(int page, int size) {
        return productJpaRepository.findAllIds(PageRequest.of(page, size));
    }

    @Override
    public Page<String> findProductIdsByCategoryIds(List<String> categoryIds, int page, int size) {
        return productJpaRepository.findIdsByCategoryIds(categoryIds, PageRequest.of(page, size));
    }

    @Override
    public int[] bulkSyncProducts(List<Product> products, boolean forceOverwrite) {
        if (products.isEmpty())
            return new int[]{0, 0, 0};

        List<String> ids = products.stream().map(Product::getId).toList();
        Map<String, ProductEntity> existingMap = productJpaRepository.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        List<ProductEntity> toSave = new ArrayList<>();
        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (Product product : products) {
            ProductEntity existing = existingMap.get(product.getId());
            if (existing != null) {
                if (forceOverwrite) {
                    applyFieldsToEntity(existing, product);
                    upsertTranslations(existing, product.getTranslations());
                    toSave.add(existing);
                    updated++;
                } else {
                    boolean changed = applyChangedFields(existing, product);
                    changed |= upsertTranslationsIfChanged(existing, product.getTranslations());
                    if (changed) {
                        toSave.add(existing);
                        updated++;
                    } else {
                        skipped++;
                    }
                }
            } else {
                toSave.add(productInfraMapper.toEntityWithChildren(product));
                created++;
            }
        }

        if (!toSave.isEmpty()) {
            productJpaRepository.saveAll(toSave);
        }
        log.info("Bulk sync persisted: {} entities ({} created, {} updated, {} skipped)", toSave.size(), created,
                updated, skipped);
        return new int[]{created, updated, skipped};
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Applies all mutable product fields from the domain model to the entity. */
    private void applyFieldsToEntity(ProductEntity entity, Product product) {
        entity.setSku(product.getSku());
        entity.setCategoryId(product.getCategoryId());
        entity.setBigImage(product.getBigImage());
        entity.setProductImageSet(product.getProductImageSet());
        entity.setSellPrice(product.getSellPrice());
        entity.setProductType(product.getProductType());
        entity.setDescription(product.getDescription());
        if (product.getListedNum() != null)
            entity.setListedNum(product.getListedNum());
        if (product.getWarehouseInventoryNum() != null)
            entity.setWarehouseInventoryNum(product.getWarehouseInventoryNum());
        if (product.getIsVideo() != null)
            entity.setIsVideo(product.getIsVideo());
    }

    /**
     * Applies only fields that actually changed. Returns true if at least one field
     * was updated.
     */
    private boolean applyChangedFields(ProductEntity entity, Product product) {
        boolean changed = false;
        if (!Objects.equals(entity.getSku(), product.getSku())) {
            entity.setSku(product.getSku());
            changed = true;
        }
        if (!Objects.equals(entity.getCategoryId(), product.getCategoryId())) {
            entity.setCategoryId(product.getCategoryId());
            changed = true;
        }
        if (!Objects.equals(entity.getBigImage(), product.getBigImage())) {
            entity.setBigImage(product.getBigImage());
            changed = true;
        }
        if (!Objects.equals(entity.getProductImageSet(), product.getProductImageSet())) {
            entity.setProductImageSet(product.getProductImageSet());
            changed = true;
        }
        if (!Objects.equals(entity.getSellPrice(), product.getSellPrice())) {
            entity.setSellPrice(product.getSellPrice());
            changed = true;
        }
        if (!Objects.equals(entity.getProductType(), product.getProductType())) {
            entity.setProductType(product.getProductType());
            changed = true;
        }
        if (!Objects.equals(entity.getDescription(), product.getDescription())) {
            entity.setDescription(product.getDescription());
            changed = true;
        }
        if (product.getListedNum() != null && !Objects.equals(entity.getListedNum(), product.getListedNum())) {
            entity.setListedNum(product.getListedNum());
            changed = true;
        }
        if (product.getWarehouseInventoryNum() != null
                && !Objects.equals(entity.getWarehouseInventoryNum(), product.getWarehouseInventoryNum())) {
            entity.setWarehouseInventoryNum(product.getWarehouseInventoryNum());
            changed = true;
        }
        if (product.getIsVideo() != null && !Objects.equals(entity.getIsVideo(), product.getIsVideo())) {
            entity.setIsVideo(product.getIsVideo());
            changed = true;
        }
        return changed;
    }

    /**
     * Upserts translations only if they differ. Returns true if any translation was
     * added or changed.
     */
    private boolean upsertTranslationsIfChanged(ProductEntity entity, List<ProductTranslation> translations) {
        if (translations == null)
            return false;
        boolean changed = false;
        for (ProductTranslation t : translations) {
            var match = entity.getTranslations().stream().filter(e -> e.getId().getLocale().equals(t.getLocale()))
                    .findFirst();
            if (match.isPresent()) {
                if (!Objects.equals(match.get().getName(), t.getName())) {
                    match.get().setName(t.getName());
                    changed = true;
                }
            } else {
                entity.getTranslations().add(buildTranslation(entity, t.getLocale(), t.getName()));
                changed = true;
            }
        }
        return changed;
    }

    /** Upserts the given translations into the entity (add or update by locale). */
    private void upsertTranslations(ProductEntity entity, List<ProductTranslation> translations) {
        if (translations == null)
            return;
        for (ProductTranslation t : translations) {
            entity.getTranslations().stream().filter(e -> e.getId().getLocale().equals(t.getLocale())).findFirst()
                    .ifPresentOrElse(e -> e.setName(t.getName()),
                            () -> entity.getTranslations().add(buildTranslation(entity, t.getLocale(), t.getName())));
        }
    }

    private ProductTranslationEntity buildTranslation(ProductEntity product, String locale, String name) {
        return ProductTranslationEntity.builder().id(new ProductTranslationId(product.getId(), locale)).name(name)
                .product(product).build();
    }

    /**
     * Filters translations to the requested locale and updates the product name.
     * Falls back to keeping all translations when the locale is not found.
     */
    private Product filterTranslations(Product product, String locale) {
        if (locale == null || locale.isBlank())
            return product;

        List<ProductTranslation> filtered = product.getTranslations().stream().filter(t -> locale.equals(t.getLocale()))
                .toList();

        if (!filtered.isEmpty()) {
            product.setTranslations(filtered);
            product.setName(filtered.getFirst().getName());
        }

        if (product.getVariants() != null) {
            product.getVariants().forEach(variant -> variant.setTranslations(
                    variant.getTranslations().stream().filter(t -> locale.equals(t.getLocale())).toList()));
        }

        return product;
    }

    /** Returns the locale of the first translation, falling back to "en". */
    private String resolveLocale(Product product) {
        if (product.getTranslations() != null && !product.getTranslations().isEmpty()) {
            return product.getTranslations().getFirst().getLocale();
        }
        return "en";
    }
}
