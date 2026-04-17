package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantInventory;
import com.backandwhite.domain.model.ProductDetailVariantTranslation;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.ProductDetailInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.ProductDetailJpaRepository;
import com.backandwhite.infrastructure.db.postgres.repository.ProductDetailVariantJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.ProductDetailVariantSpecification;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductDetailRepositoryImpl implements ProductDetailRepository {

    private final ProductDetailJpaRepository productDetailJpaRepository;
    private final ProductDetailVariantJpaRepository productDetailVariantJpaRepository;
    private final ProductDetailInfraMapper productDetailInfraMapper;

    @Override
    public Optional<ProductDetail> findByPid(String pid) {
        return productDetailJpaRepository.findById(pid).map(productDetailInfraMapper::toDomain);
    }

    @Override
    public boolean existsByPid(String pid) {
        return productDetailJpaRepository.existsById(pid);
    }

    @Override
    public boolean existsVariantByVid(String vid) {
        return productDetailVariantJpaRepository.existsById(vid);
    }

    @Override
    public ProductDetail save(ProductDetail detail) {
        ProductDetailEntity entity = productDetailInfraMapper.toEntityWithChildren(detail);
        return productDetailInfraMapper.toDomain(productDetailJpaRepository.save(entity));
    }

    @Override
    public long countAll() {
        return productDetailJpaRepository.count();
    }

    @Override
    public List<String> findPidsNeedingInventorySync(int limit) {
        Instant threshold = Instant.now().minus(4, ChronoUnit.HOURS);
        return productDetailJpaRepository.findPidsNeedingInventorySync(threshold, limit);
    }

    @Override
    public List<String> findPidsNeedingProductSync(int limit) {
        Instant threshold = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return productDetailJpaRepository.findPidsNeedingProductSync(threshold, limit);
    }

    @Override
    public List<String> findPidsNeedingReviewsSync(int limit) {
        Instant threshold = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return productDetailJpaRepository.findPidsNeedingReviewsSync(threshold, limit);
    }

    @Override
    public void markInventorySynced(String pid) {
        productDetailJpaRepository.markInventorySynced(pid, Instant.now());
    }

    @Override
    public void markProductSynced(String pid) {
        productDetailJpaRepository.markProductSynced(pid, Instant.now());
    }

    @Override
    public void markReviewsSynced(String pid) {
        productDetailJpaRepository.markReviewsSynced(pid, Instant.now());
    }

    // ── Variant queries ───────────────────────────────────────────────────────

    @Override
    public Page<ProductDetailVariant> findAllVariantsPaged(Pageable pageable) {
        return productDetailVariantJpaRepository.findAll(pageable).map(productDetailInfraMapper::toVariantDomain);
    }

    @Override
    public Page<ProductDetailVariant> searchVariantsPaged(String search, Pageable pageable) {
        return productDetailVariantJpaRepository
                .findAll(ProductDetailVariantSpecification.searchByTerm(search), pageable)
                .map(productDetailInfraMapper::toVariantDomain);
    }

    @Override
    public Page<ProductDetailVariant> findVariantsFiltered(String locale, String search, ProductStatus status,
            String pid, Pageable pageable) {
        Specification<ProductDetailVariantEntity> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.isBlank()) {
            spec = spec.and(ProductDetailVariantSpecification.searchByTerm(search.trim()));
        }
        if (status != null) {
            spec = spec.and(ProductDetailVariantSpecification.hasStatus(status));
        }
        if (pid != null && !pid.isBlank()) {
            spec = spec.and(ProductDetailVariantSpecification.hasPid(pid));
        }

        return productDetailVariantJpaRepository.findAll(spec, pageable)
                .map(e -> filterVariantTranslations(productDetailInfraMapper.toVariantDomain(e), locale));
    }

    @Override
    public List<ProductDetailVariant> findVariantsByPid(String pid, String locale) {
        return productDetailVariantJpaRepository.findByPid(pid).stream()
                .map(e -> filterVariantTranslations(productDetailInfraMapper.toVariantDomain(e), locale)).toList();
    }

    @Override
    public Optional<ProductDetailVariant> findVariantByVid(String vid, String locale) {
        return productDetailVariantJpaRepository.findById(vid)
                .map(e -> filterVariantTranslations(productDetailInfraMapper.toVariantDomain(e), locale));
    }

    // ── Variant mutations ─────────────────────────────────────────────────────

    @Override
    public ProductDetailVariant saveVariant(ProductDetailVariant variant) {
        // Use getReferenceById to avoid an extra SELECT; existence is validated
        // upstream
        ProductDetailEntity parent = productDetailJpaRepository.getReferenceById(variant.getPid());

        ProductDetailVariantEntity entity = productDetailInfraMapper.toVariantEntity(variant);
        entity.setProductDetail(parent);

        if (variant.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ProductDetailVariantTranslation vt : variant.getTranslations()) {
                ProductDetailVariantTranslationEntity vte = productDetailInfraMapper.toVariantTranslationEntity(vt,
                        variant.getVid());
                vte.setVariant(entity);
                entity.getTranslations().add(vte);
            }
        }

        if (variant.getInventories() != null) {
            entity.getInventories().clear();
            for (ProductDetailVariantInventory inv : variant.getInventories()) {
                ProductDetailVariantInventoryEntity ie = productDetailInfraMapper.toInventoryEntity(inv);
                ie.setVariant(entity);
                entity.getInventories().add(ie);
            }
        }

        ProductDetailVariant result = productDetailInfraMapper
                .toVariantDomain(productDetailVariantJpaRepository.save(entity));
        return result.getPid() == null ? result.withPid(variant.getPid()) : result;
    }

    @Override
    public void updateVariantStatus(String vid, ProductStatus status) {
        ProductDetailVariantEntity entity = productDetailVariantJpaRepository.findById(vid)
                .orElseThrow(() -> new IllegalArgumentException("ProductDetailVariant with vid=" + vid + " not found"));
        entity.setStatus(status);
        productDetailVariantJpaRepository.save(entity);
    }

    @Override
    public void deleteVariant(String vid) {
        productDetailVariantJpaRepository.deleteById(vid);
    }

    @Override
    public void deleteVariants(List<String> vids) {
        // findAll + deleteAll ensures JPA cascade fires for translations and
        // inventories
        List<ProductDetailVariantEntity> entities = productDetailVariantJpaRepository.findAllById(vids);
        productDetailVariantJpaRepository.deleteAll(entities);
    }

    @Override
    public void bulkUpdateVariantStatus(List<String> vids, ProductStatus status) {
        productDetailVariantJpaRepository.bulkUpdateStatus(vids, status);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Filters variant translations to the requested locale. Falls back to keeping
     * all translations when the locale is not found.
     */
    private ProductDetailVariant filterVariantTranslations(ProductDetailVariant variant, String locale) {
        if (locale == null || locale.isBlank() || variant.getTranslations() == null
                || variant.getTranslations().isEmpty()) {
            return variant;
        }
        List<ProductDetailVariantTranslation> filtered = variant.getTranslations().stream()
                .filter(t -> locale.equals(t.getLocale())).toList();
        return variant.withTranslations(filtered.isEmpty() ? variant.getTranslations() : filtered);
    }
}
