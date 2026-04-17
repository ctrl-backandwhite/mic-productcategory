package com.backandwhite.application.usecase.impl;

import static com.backandwhite.provider.AttributeProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.repository.AttributeRepository;
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
class AttributeUseCaseImplTest {

    @Mock
    private AttributeRepository attributeRepository;

    @InjectMocks
    private AttributeUseCaseImpl attributeUseCase;

    @Test
    void findAll_returnsPage() {
        Page<Attribute> page = new PageImpl<>(List.of(attribute()));
        when(attributeRepository.findAll(any(), any(Pageable.class))).thenReturn(page);

        Page<Attribute> result = attributeUseCase.findAll(null, 0, 20, "name", true);

        assertThat(result.getContent()).hasSize(1);
        verify(attributeRepository).findAll(any(), any(Pageable.class));
    }

    @Test
    void findById_existing_returnsAttribute() {
        Attribute model = attribute();
        when(attributeRepository.findById(ATTRIBUTE_ID)).thenReturn(Optional.of(model));

        Attribute result = attributeUseCase.findById(ATTRIBUTE_ID);

        assertSame(model, result);
        verify(attributeRepository).findById(ATTRIBUTE_ID);
    }

    @Test
    void findById_missing_throwsEntityNotFoundException() {
        when(attributeRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> attributeUseCase.findById("non-existent"));
    }

    @Test
    void create_delegatesToRepository() {
        Attribute model = attribute();
        when(attributeRepository.save(model)).thenReturn(model);

        Attribute result = attributeUseCase.create(model);

        assertSame(model, result);
        verify(attributeRepository).save(model);
    }

    @Test
    void update_delegatesToRepository() {
        Attribute model = attribute();
        when(attributeRepository.update(ATTRIBUTE_ID, model)).thenReturn(model);

        Attribute result = attributeUseCase.update(ATTRIBUTE_ID, model);

        assertSame(model, result);
        verify(attributeRepository).update(ATTRIBUTE_ID, model);
    }

    @Test
    void delete_delegatesToRepository() {
        attributeUseCase.delete(ATTRIBUTE_ID);

        verify(attributeRepository).delete(ATTRIBUTE_ID);
    }
}
