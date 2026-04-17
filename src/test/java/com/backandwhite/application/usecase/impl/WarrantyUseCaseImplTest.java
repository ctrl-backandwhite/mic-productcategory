package com.backandwhite.application.usecase.impl;

import static com.backandwhite.provider.WarrantyProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.repository.WarrantyRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class WarrantyUseCaseImplTest {

    @Mock
    private WarrantyRepository warrantyRepository;

    @InjectMocks
    private WarrantyUseCaseImpl warrantyUseCase;

    @Test
    void findAll_returnsPage() {
        Page<Warranty> page = new PageImpl<>(List.of(warranty()));
        when(warrantyRepository.findAll(any(), any(), any(Pageable.class))).thenReturn(page);

        Page<Warranty> result = warrantyUseCase.findAll(true, WARRANTY_TYPE, 0, 10, "name", true);

        assertThat(result.getContent()).hasSize(1);
        verify(warrantyRepository).findAll(any(), any(), any(Pageable.class));
    }

    @Test
    void findById_existing_returnsWarranty() {
        when(warrantyRepository.findById(WARRANTY_ID)).thenReturn(Optional.of(warranty()));

        Warranty result = warrantyUseCase.findById(WARRANTY_ID);

        assertThat(result.getId()).isEqualTo(WARRANTY_ID);
    }

    @Test
    void findById_missing_throwsEntityNotFoundException() {
        when(warrantyRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> warrantyUseCase.findById("non-existent"));
    }

    @Test
    void create_setsActiveAndDelegatesToRepository() {
        ArgumentCaptor<Warranty> captor = ArgumentCaptor.forClass(Warranty.class);
        Warranty model = warranty();
        model.setActive(false);
        when(warrantyRepository.save(captor.capture())).thenReturn(warranty());

        Warranty result = warrantyUseCase.create(model);

        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(result.getId()).isEqualTo(WARRANTY_ID);
    }

    @Test
    void update_preservesIdAndActive() {
        Warranty existing = warranty();
        existing.setActive(true);
        when(warrantyRepository.findById(WARRANTY_ID)).thenReturn(Optional.of(existing));

        ArgumentCaptor<Warranty> captor = ArgumentCaptor.forClass(Warranty.class);
        when(warrantyRepository.update(captor.capture())).thenReturn(warranty());

        Warranty input = Warranty.builder().name("Updated Name").build();
        warrantyUseCase.update(WARRANTY_ID, input);

        Warranty captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(WARRANTY_ID);
        assertThat(captured.getActive()).isTrue();
    }

    @Test
    void delete_verifiesExistenceAndDelegatesToRepository() {
        when(warrantyRepository.findById(WARRANTY_ID)).thenReturn(Optional.of(warranty()));

        warrantyUseCase.delete(WARRANTY_ID);

        verify(warrantyRepository).deleteById(WARRANTY_ID);
    }

    @Test
    void delete_missing_throwsEntityNotFoundException() {
        when(warrantyRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> warrantyUseCase.delete("non-existent"));
    }

    @Test
    void toggleActive_trueToFalse() {
        Warranty existing = warranty();
        existing.setActive(true);
        when(warrantyRepository.findById(WARRANTY_ID)).thenReturn(Optional.of(existing));

        ArgumentCaptor<Warranty> captor = ArgumentCaptor.forClass(Warranty.class);
        when(warrantyRepository.update(captor.capture())).thenReturn(existing);

        warrantyUseCase.toggleActive(WARRANTY_ID);

        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void toggleActive_falseToTrue() {
        Warranty existing = warranty();
        existing.setActive(false);
        when(warrantyRepository.findById(WARRANTY_ID)).thenReturn(Optional.of(existing));

        ArgumentCaptor<Warranty> captor = ArgumentCaptor.forClass(Warranty.class);
        when(warrantyRepository.update(captor.capture())).thenReturn(existing);

        warrantyUseCase.toggleActive(WARRANTY_ID);

        assertThat(captor.getValue().getActive()).isTrue();
    }
}
