package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.repository.BrandRepository;
import com.backandwhite.domain.valueobject.BrandStatus;
import com.backandwhite.infrastructure.db.postgres.entity.BrandEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.BrandInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.BrandJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.BrandSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;
    private final BrandInfraMapper brandInfraMapper;

    @Override
    public Page<Brand> findAll(BrandStatus status, String name, Pageable pageable) {
        return brandJpaRepository.findAll(BrandSpecification.withFilters(status, name), pageable)
                .map(entity -> {
                    Brand brand = brandInfraMapper.toDomain(entity);
                    brand.setProductCount(brandJpaRepository.countProductsByBrandId(entity.getId()));
                    return brand;
                });
    }

    @Override
    public Optional<Brand> findById(String brandId) {
        return brandJpaRepository.findById(brandId)
                .map(entity -> {
                    Brand brand = brandInfraMapper.toDomain(entity);
                    brand.setProductCount(brandJpaRepository.countProductsByBrandId(entity.getId()));
                    return brand;
                });
    }

    @Override
    public Optional<Brand> findBySlug(String slug) {
        return brandJpaRepository.findBySlug(slug)
                .map(entity -> {
                    Brand brand = brandInfraMapper.toDomain(entity);
                    brand.setProductCount(brandJpaRepository.countProductsByBrandId(entity.getId()));
                    return brand;
                });
    }

    @Override
    public Brand save(Brand brand) {
        String newId = UUID.randomUUID().toString();
        brand.setId(newId);
        brand.setStatus(BrandStatus.ACTIVE);

        brandJpaRepository.save(brandInfraMapper.toEntity(brand));

        return findById(newId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Brand", newId));
    }

    @Override
    public Brand update(String brandId, Brand brand) {
        BrandEntity entity = findOrThrow(brandId);

        entity.setName(brand.getName());
        entity.setSlug(brand.getSlug());
        entity.setLogoUrl(brand.getLogoUrl());
        entity.setWebsiteUrl(brand.getWebsiteUrl());
        entity.setDescription(brand.getDescription());

        brandJpaRepository.save(entity);

        return findById(brandId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Brand", brandId));
    }

    @Override
    public void delete(String brandId) {
        brandJpaRepository.delete(findOrThrow(brandId));
    }

    @Override
    public void updateStatus(String brandId, BrandStatus status) {
        BrandEntity entity = findOrThrow(brandId);
        entity.setStatus(status);
        brandJpaRepository.save(entity);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return brandJpaRepository.existsBySlug(slug);
    }

    @Override
    public long countProductsByBrandId(String brandId) {
        return brandJpaRepository.countProductsByBrandId(brandId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private BrandEntity findOrThrow(String id) {
        return brandJpaRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Brand", id));
    }
}
