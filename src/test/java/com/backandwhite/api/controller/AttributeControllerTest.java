package com.backandwhite.api.controller;

import static com.backandwhite.provider.AttributeProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.AttributeDtoIn;
import com.backandwhite.api.dto.out.AttributeDtoOut;
import com.backandwhite.api.mapper.AttributeApiMapper;
import com.backandwhite.application.usecase.AttributeUseCase;
import com.backandwhite.domain.model.Attribute;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AttributeControllerTest {

    @Mock
    private AttributeUseCase attributeUseCase;

    @Mock
    private AttributeApiMapper attributeApiMapper;

    @InjectMocks
    private AttributeController controller;

    @Test
    void findAll_returnsPaginatedAttributes() {
        Page<Attribute> page = new PageImpl<>(List.of(attribute()));
        when(attributeUseCase.findAll(null, 0, 20, "name", true)).thenReturn(page);
        when(attributeApiMapper.toDto(any(Attribute.class))).thenReturn(attributeDtoOut());

        ResponseEntity<PaginationDtoOut<AttributeDtoOut>> response = controller.findAll(null, 0, 20, "name", true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(attributeUseCase).findAll(null, 0, 20, "name", true);
    }

    @Test
    void getById_returnsAttribute() {
        Attribute model = attribute();
        AttributeDtoOut dtoOut = attributeDtoOut();

        when(attributeUseCase.findById(ATTRIBUTE_ID)).thenReturn(model);
        when(attributeApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<AttributeDtoOut> response = controller.getById(ATTRIBUTE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(attributeUseCase).findById(ATTRIBUTE_ID);
        verify(attributeApiMapper).toDto(model);
    }

    @Test
    void create_returnsCreatedAttribute() {
        AttributeDtoIn dtoIn = attributeDtoIn();
        Attribute model = attribute();
        AttributeDtoOut dtoOut = attributeDtoOut();

        when(attributeApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(attributeUseCase.create(model)).thenReturn(model);
        when(attributeApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<AttributeDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(attributeApiMapper).toDomain(dtoIn);
        verify(attributeUseCase).create(model);
        verify(attributeApiMapper).toDto(model);
    }

    @Test
    void update_returnsUpdatedAttribute() {
        AttributeDtoIn dtoIn = attributeDtoIn();
        Attribute model = attribute();
        AttributeDtoOut dtoOut = attributeDtoOut();

        when(attributeApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(attributeUseCase.update(ATTRIBUTE_ID, model)).thenReturn(model);
        when(attributeApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<AttributeDtoOut> response = controller.update(ATTRIBUTE_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(attributeApiMapper).toDomain(dtoIn);
        verify(attributeUseCase).update(ATTRIBUTE_ID, model);
        verify(attributeApiMapper).toDto(model);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(ATTRIBUTE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(attributeUseCase).delete(ATTRIBUTE_ID);
    }
}
