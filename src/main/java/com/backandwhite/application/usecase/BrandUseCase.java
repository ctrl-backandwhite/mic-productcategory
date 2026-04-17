package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.valueobject.BrandStatus;
import org.springframework.data.domain.Page;

public interface BrandUseCase {

    Page<Brand> findAll(BrandStatus status, String name, int page, int size, String sortBy, boolean ascending);

    Brand findById(String brandId);

    Brand findBySlug(String slug);

    Brand create(Brand brand);

    Brand update(String brandId, Brand brand);

    void delete(String brandId);

    void toggleStatus(String brandId);
}
