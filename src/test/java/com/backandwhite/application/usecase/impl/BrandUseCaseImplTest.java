package com.backandwhite.application.usecase.impl;

import static com.backandwhite.provider.BrandProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.repository.BrandRepository;
import com.backandwhite.domain.valueobject.BrandStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BrandUseCaseImplTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandUseCaseImpl brandUseCase;

    @Test
    void findAll_returnsPage() {
        Page<Brand> page = new PageImpl<>(List.of(brand()));
        when(brandRepository.findAll(any(), any(), any(Pageable.class))).thenReturn(page);

        Page<Brand> result = brandUseCase.findAll(null, null, 0, 20, "name", true);

        assertThat(result.getContent()).hasSize(1);
        verify(brandRepository).findAll(any(), any(), any(Pageable.class));
    }

    @Test
    void findById_existing_returnsBrand() {
        Brand model = brand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(model));

        Brand result = brandUseCase.findById(BRAND_ID);

        assertSame(model, result);
        verify(brandRepository).findById(BRAND_ID);
    }

    @Test
    void findById_missing_throwsEntityNotFoundException() {
        when(brandRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> brandUseCase.findById("non-existent"));
    }

    @Test
    void findBySlug_existing_returnsBrand() {
        Brand model = brand();
        when(brandRepository.findBySlug(BRAND_SLUG)).thenReturn(Optional.of(model));

        Brand result = brandUseCase.findBySlug(BRAND_SLUG);

        assertSame(model, result);
        verify(brandRepository).findBySlug(BRAND_SLUG);
    }

    @Test
    void findBySlug_missing_throwsEntityNotFoundException() {
        when(brandRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> brandUseCase.findBySlug("missing"));
    }

    @Test
    void create_delegatesToRepository() {
        Brand model = brand();
        when(brandRepository.save(model)).thenReturn(model);

        Brand result = brandUseCase.create(model);

        assertSame(model, result);
        verify(brandRepository).save(model);
    }

    @Test
    void update_delegatesToRepository() {
        Brand model = brand();
        when(brandRepository.update(BRAND_ID, model)).thenReturn(model);

        Brand result = brandUseCase.update(BRAND_ID, model);

        assertSame(model, result);
        verify(brandRepository).update(BRAND_ID, model);
    }

    @Test
    void delete_delegatesToRepository() {
        brandUseCase.delete(BRAND_ID);

        verify(brandRepository).delete(BRAND_ID);
    }

    @Test
    void toggleStatus_activeToInactive() {
        Brand activeBrand = brand();
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(activeBrand));

        brandUseCase.toggleStatus(BRAND_ID);

        verify(brandRepository).updateStatus(BRAND_ID, BrandStatus.INACTIVE);
    }

    @Test
    void toggleStatus_inactiveToActive() {
        Brand inactiveBrand = brand().withStatus(BrandStatus.INACTIVE);
        when(brandRepository.findById(BRAND_ID)).thenReturn(Optional.of(inactiveBrand));

        brandUseCase.toggleStatus(BRAND_ID);

        verify(brandRepository).updateStatus(BRAND_ID, BrandStatus.ACTIVE);
    }
}
