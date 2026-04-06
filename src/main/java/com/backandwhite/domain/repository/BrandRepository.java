package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.valueobject.BrandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BrandRepository {

    Page<Brand> findAll(BrandStatus status, String name, Pageable pageable);

    Optional<Brand> findById(String brandId);

    Optional<Brand> findBySlug(String slug);

    Brand save(Brand brand);

    Brand update(String brandId, Brand brand);

    void delete(String brandId);

    void updateStatus(String brandId, BrandStatus status);

    boolean existsBySlug(String slug);

    long countProductsByBrandId(String brandId);
}
