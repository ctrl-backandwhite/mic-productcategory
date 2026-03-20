package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.*;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valureobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.*;
import com.backandwhite.infrastructure.db.postgres.mapper.ProductDetailInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.ProductDetailJpaRepository;
import com.backandwhite.infrastructure.db.postgres.repository.ProductDetailVariantJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.ProductDetailVariantSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductDetailRepositoryImpl implements ProductDetailRepository {

    private final ProductDetailJpaRepository productDetailJpaRepository;
    private final ProductDetailVariantJpaRepository productDetailVariantJpaRepository;
    private final ProductDetailInfraMapper productDetailInfraMapper;

    @Override
    public Optional<ProductDetail> findByPid(String pid) {
        return productDetailJpaRepository.findById(pid)
                .map(productDetailInfraMapper::toDomain);
    }

    @Override
    public boolean existsByPid(String pid) {
        return productDetailJpaRepository.existsById(pid);
    }

    @Override
    public ProductDetail save(ProductDetail detail) {
        ProductDetailEntity entity = productDetailInfraMapper.toEntityWithChildren(detail);
        ProductDetailEntity saved = productDetailJpaRepository.save(entity);
        return productDetailInfraMapper.toDomain(saved);
    }

    // ── Variant CRUD ─────────────────────────────────────────────────────────

    @Override
    public Page<ProductDetailVariant> findAllVariantsPaged(Pageable pageable) {
        return productDetailVariantJpaRepository.findAll(pageable)
                .map(productDetailInfraMapper::toVariantDomain);
    }

    @Override
    public Page<ProductDetailVariant> searchVariantsPaged(String search, Pageable pageable) {
        return productDetailVariantJpaRepository
                .findAll(ProductDetailVariantSpecification.searchByTerm(search), pageable)
                .map(productDetailInfraMapper::toVariantDomain);
    }

    @Override
    public Page<ProductDetailVariant> findVariantsFiltered(String search, ProductStatus status, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<ProductDetailVariantEntity> spec = (root, query, cb) -> cb
                .conjunction();

        if (search != null && !search.isBlank()) {
            spec = spec.and(ProductDetailVariantSpecification.searchByTerm(search.trim()));
        }
        if (status != null) {
            spec = spec.and(ProductDetailVariantSpecification.hasStatus(status));
        }

        return productDetailVariantJpaRepository.findAll(spec, pageable)
                .map(productDetailInfraMapper::toVariantDomain);
    }

    @Override
    public List<ProductDetailVariant> findVariantsByPid(String pid) {
        List<ProductDetailVariantEntity> entities = productDetailVariantJpaRepository.findByPid(pid);
        return entities.stream()
                .map(productDetailInfraMapper::toVariantDomain)
                .toList();
    }

    @Override
    public Optional<ProductDetailVariant> findVariantByVid(String vid) {
        return productDetailVariantJpaRepository.findById(vid)
                .map(productDetailInfraMapper::toVariantDomain);
    }

    @Override
    public ProductDetailVariant saveVariant(ProductDetailVariant variant) {
        // Load parent reference
        ProductDetailEntity parent = productDetailJpaRepository.findById(variant.getPid())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProductDetail with pid=" + variant.getPid() + " not found"));

        ProductDetailVariantEntity entity = productDetailInfraMapper.toVariantEntity(variant);
        entity.setProductDetail(parent);

        // Wire translations
        if (variant.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ProductDetailVariantTranslation vt : variant.getTranslations()) {
                ProductDetailVariantTranslationEntity vte = productDetailInfraMapper.toVariantTranslationEntity(vt,
                        variant.getVid());
                vte.setVariant(entity);
                entity.getTranslations().add(vte);
            }
        }

        // Wire inventories
        if (variant.getInventories() != null) {
            entity.getInventories().clear();
            for (ProductDetailVariantInventory inv : variant.getInventories()) {
                ProductDetailVariantInventoryEntity ie = productDetailInfraMapper.toInventoryEntity(inv);
                ie.setVariant(entity);
                entity.getInventories().add(ie);
            }
        }

        ProductDetailVariantEntity saved = productDetailVariantJpaRepository.save(entity);
        ProductDetailVariant result = productDetailInfraMapper.toVariantDomain(saved);
        // Ensure pid is populated from the parent relationship
        if (result.getPid() == null) {
            result = result.withPid(variant.getPid());
        }
        return result;
    }

    @Override
    public void updateVariantStatus(String vid, ProductStatus status) {
        ProductDetailVariantEntity entity = productDetailVariantJpaRepository.findById(vid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProductDetailVariant with vid=" + vid + " not found"));
        entity.setStatus(status);
        productDetailVariantJpaRepository.save(entity);
    }

    @Override
    public void deleteVariant(String vid) {
        productDetailVariantJpaRepository.deleteById(vid);
    }

    @Override
    public void deleteVariants(List<String> vids) {
        productDetailVariantJpaRepository.deleteAllByIdInBatch(vids);
    }

    @Override
    public void bulkUpdateVariantStatus(List<String> vids, ProductStatus status) {
        productDetailVariantJpaRepository.bulkUpdateStatus(vids, status);
    }
}
