package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.BrandUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.repository.BrandRepository;
import com.backandwhite.domain.valureobject.BrandStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class BrandUseCaseImpl implements BrandUseCase {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Brand> findAll(BrandStatus status, String name, int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return brandRepository.findAll(status, name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Brand findById(String brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Brand", brandId));
    }

    @Override
    @Transactional(readOnly = true)
    public Brand findBySlug(String slug) {
        return brandRepository.findBySlug(slug)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Brand", slug));
    }

    @Override
    @Transactional
    public Brand create(Brand brand) {
        return brandRepository.save(brand);
    }

    @Override
    @Transactional
    public Brand update(String brandId, Brand brand) {
        return brandRepository.update(brandId, brand);
    }

    @Override
    @Transactional
    public void delete(String brandId) {
        brandRepository.delete(brandId);
    }

    @Override
    @Transactional
    public void toggleStatus(String brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Brand", brandId));
        BrandStatus newStatus = brand.getStatus() == BrandStatus.ACTIVE
                ? BrandStatus.INACTIVE
                : BrandStatus.ACTIVE;
        brandRepository.updateStatus(brandId, newStatus);
    }
}
