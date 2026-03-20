package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.exception.Message;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductTranslation;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.domain.valureobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationId;
import com.backandwhite.infrastructure.db.postgres.mapper.ProductInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.CategoryJpaRepository;
import com.backandwhite.infrastructure.db.postgres.repository.ProductJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

        private final ProductJpaRepository productJpaRepository;
        private final ProductInfraMapper productInfraMapper;
        private final CategoryJpaRepository categoryJpaRepository;

        @Override
        public List<Product> findByCategoryId(String categoryId, String locale, ProductStatus status) {
                List<String> categoryIds = categoryJpaRepository.findDescendantIds(categoryId);

                return productJpaRepository
                                .findAll(ProductSpecification.byLocaleAndCategoryIds(locale,
                                                categoryIds.isEmpty() ? List.of(categoryId) : categoryIds, status))
                                .stream()
                                .map(productInfraMapper::toDomain)
                                .map(product -> filterTranslations(product, locale))
                                .toList();
        }

        @Override
        public Page<Product> findAllPaged(String locale, String categoryId, ProductStatus status, String name,
                        Pageable pageable) {
                if (categoryId != null && !categoryId.isBlank()) {
                        List<String> categoryIds = categoryJpaRepository.findDescendantIds(categoryId);
                        return productJpaRepository
                                        .findAll(ProductSpecification.byLocaleAndCategoryIds(locale,
                                                        categoryIds.isEmpty() ? List.of(categoryId) : categoryIds,
                                                        status, name),
                                                        pageable)
                                        .map(productInfraMapper::toDomain)
                                        .map(product -> filterTranslations(product, locale));
                }

                return productJpaRepository
                                .findAll(ProductSpecification.byLocaleAndCategoryIds(locale, null, status, name),
                                                pageable)
                                .map(productInfraMapper::toDomain)
                                .map(product -> filterTranslations(product, locale));
        }

        @Override
        public Optional<Product> findById(String productId, String locale) {
                return productJpaRepository.findById(productId)
                                .map(productInfraMapper::toDomain)
                                .map(product -> filterTranslations(product, locale));
        }

        @Override
        public boolean existsById(String productId) {
                return productJpaRepository.existsById(productId);
        }

        @Override
        public Product save(Product product) {
                String newId = product.getId() != null ? product.getId()
                                : UUID.randomUUID().toString().toUpperCase();

                product.setId(newId);
                ProductEntity entity = productInfraMapper.toEntityWithChildren(product);

                // Variants are read-only (come from CJ detail fetch), not created here

                productJpaRepository.save(entity);

                return findById(newId, resolveLocale(product))
                                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", newId));
        }

        @Override
        public Product update(String productId, Product product) {
                ProductEntity entity = productJpaRepository.findById(productId)
                                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", productId));

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

                // Update translations (upsert pattern)
                if (product.getTranslations() != null) {
                        product.getTranslations().forEach(t -> entity.getTranslations().stream()
                                        .filter(existing -> existing.getId().getLocale().equals(t.getLocale()))
                                        .findFirst()
                                        .ifPresentOrElse(
                                                        existing -> existing.setName(t.getName()),
                                                        () -> entity.getTranslations().add(
                                                                        buildTranslation(entity, t.getLocale(),
                                                                                        t.getName()))));
                }

                // Variants are read-only (come from CJ detail fetch), not updated here

                productJpaRepository.save(entity);

                return findById(productId, resolveLocale(product))
                                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", productId));
        }

        @Override
        public void delete(String productId) {
                ProductEntity entity = productJpaRepository.findById(productId)
                                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", productId));
                productJpaRepository.delete(entity);
        }

        @Override
        public void deleteAll(List<String> productIds) {
                if (productIds == null || productIds.isEmpty())
                        return;
                productJpaRepository.deleteAllByIdInBatch(productIds);
        }

        // ── Private helpers ──────────────────────────────────────────────────────

        private ProductTranslationEntity buildTranslation(ProductEntity product, String locale, String name) {
                return ProductTranslationEntity.builder()
                                .id(new ProductTranslationId(product.getId(), locale))
                                .name(name)
                                .product(product)
                                .build();
        }

        private String resolveLocale(Product product) {
                if (product.getTranslations() != null && !product.getTranslations().isEmpty()) {
                        return product.getTranslations().getFirst().getLocale();
                }
                return "es";
        }

        /**
         * Filters product and variant translations to only include the requested
         * locale.
         */
        private Product filterTranslations(Product product, String locale) {
                if (locale == null || locale.isBlank()) {
                        return product;
                }

                // Filter product-level translations
                product.setTranslations(
                                product.getTranslations().stream()
                                                .filter(t -> locale.equals(t.getLocale()))
                                                .toList());

                // Update product name from the filtered translation
                product.getTranslations().stream()
                                .findFirst()
                                .ifPresent(t -> product.setName(t.getName()));

                // Filter variant-level translations (read-only from CJ)
                if (product.getVariants() != null) {
                        product.getVariants().forEach(variant -> {
                                variant.setTranslations(
                                                variant.getTranslations().stream()
                                                                .filter(t -> locale.equals(t.getLocale()))
                                                                .toList());
                        });
                }

                return product;
        }

        @Override
        public void enrichDetail(String productId, String description, String productImageSet) {
                ProductEntity entity = productJpaRepository.findById(productId)
                                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", productId));
                if (description != null)
                        entity.setDescription(description);
                if (productImageSet != null)
                        entity.setProductImageSet(productImageSet);
                productJpaRepository.save(entity);
        }

        @Override
        public void updateStatus(String productId, ProductStatus status) {
                ProductEntity entity = productJpaRepository.findById(productId)
                                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Product", productId));
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
                return productJpaRepository.findAllIds(
                                org.springframework.data.domain.PageRequest.of(page, size));
        }

        @Override
        public int[] bulkSyncProducts(List<Product> products) {
                if (products.isEmpty()) {
                        return new int[] { 0, 0 };
                }

                // 1) Collect all IDs we want to sync
                List<String> ids = products.stream().map(Product::getId).toList();

                // 2) Single query: load all existing entities at once
                Map<String, ProductEntity> existingMap = productJpaRepository.findAllByIdIn(ids)
                                .stream()
                                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

                List<ProductEntity> toSave = new ArrayList<>();
                int created = 0;
                int updated = 0;

                for (Product product : products) {
                        ProductEntity existing = existingMap.get(product.getId());

                        if (existing != null) {
                                // Update existing entity fields
                                existing.setSku(product.getSku());
                                existing.setCategoryId(product.getCategoryId());
                                existing.setBigImage(product.getBigImage());
                                existing.setProductImageSet(product.getProductImageSet());
                                existing.setSellPrice(product.getSellPrice());
                                existing.setProductType(product.getProductType());
                                existing.setDescription(product.getDescription());
                                if (product.getListedNum() != null)
                                        existing.setListedNum(product.getListedNum());
                                if (product.getWarehouseInventoryNum() != null)
                                        existing.setWarehouseInventoryNum(product.getWarehouseInventoryNum());
                                if (product.getIsVideo() != null)
                                        existing.setIsVideo(product.getIsVideo());

                                // Upsert translations
                                if (product.getTranslations() != null) {
                                        for (ProductTranslation t : product.getTranslations()) {
                                                existing.getTranslations().stream()
                                                                .filter(e -> e.getId().getLocale()
                                                                                .equals(t.getLocale()))
                                                                .findFirst()
                                                                .ifPresentOrElse(
                                                                                e -> e.setName(t.getName()),
                                                                                () -> existing.getTranslations().add(
                                                                                                buildTranslation(
                                                                                                                existing,
                                                                                                                t.getLocale(),
                                                                                                                t.getName())));
                                        }
                                }

                                toSave.add(existing);
                                updated++;
                        } else {
                                // Create new entity
                                ProductEntity entity = productInfraMapper.toEntityWithChildren(product);
                                toSave.add(entity);
                                created++;
                        }
                }

                // 3) Single batch save
                productJpaRepository.saveAll(toSave);
                log.info("Bulk sync persisted: {} entities ({} created, {} updated)", toSave.size(), created, updated);

                return new int[] { created, updated };
        }
}
