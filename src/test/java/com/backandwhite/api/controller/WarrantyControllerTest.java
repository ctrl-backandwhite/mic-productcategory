package com.backandwhite.api.controller;

import static com.backandwhite.provider.WarrantyProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.WarrantyDtoIn;
import com.backandwhite.api.dto.out.WarrantyDtoOut;
import com.backandwhite.api.mapper.WarrantyApiMapper;
import com.backandwhite.application.usecase.WarrantyUseCase;
import com.backandwhite.domain.model.Warranty;
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
class WarrantyControllerTest {

    @Mock
    private WarrantyUseCase warrantyUseCase;

    @Mock
    private WarrantyApiMapper warrantyApiMapper;

    @InjectMocks
    private WarrantyController controller;

    @Test
    void findAll_returnsPaginatedWarranties() {
        Page<Warranty> page = new PageImpl<>(List.of(warranty()));
        when(warrantyUseCase.findAll(null, null, 0, 20, "name", true)).thenReturn(page);
        when(warrantyApiMapper.toDto(any(Warranty.class))).thenReturn(warrantyDtoOut());

        ResponseEntity<PaginationDtoOut<WarrantyDtoOut>> response = controller.findAll(null, null, 0, 20, "name", true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(warrantyUseCase).findAll(null, null, 0, 20, "name", true);
    }

    @Test
    void getById_returnsWarranty() {
        Warranty model = warranty();
        WarrantyDtoOut dtoOut = warrantyDtoOut();

        when(warrantyUseCase.findById(WARRANTY_ID)).thenReturn(model);
        when(warrantyApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<WarrantyDtoOut> response = controller.getById(WARRANTY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(warrantyUseCase).findById(WARRANTY_ID);
        verify(warrantyApiMapper).toDto(model);
    }

    @Test
    void create_returnsCreatedWarranty() {
        WarrantyDtoIn dtoIn = warrantyDtoIn();
        Warranty model = warranty();
        WarrantyDtoOut dtoOut = warrantyDtoOut();

        when(warrantyApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(warrantyUseCase.create(model)).thenReturn(model);
        when(warrantyApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<WarrantyDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(warrantyApiMapper).toDomain(dtoIn);
        verify(warrantyUseCase).create(model);
        verify(warrantyApiMapper).toDto(model);
    }

    @Test
    void update_returnsUpdatedWarranty() {
        WarrantyDtoIn dtoIn = warrantyDtoIn();
        Warranty model = warranty();
        WarrantyDtoOut dtoOut = warrantyDtoOut();

        when(warrantyApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(warrantyUseCase.update(WARRANTY_ID, model)).thenReturn(model);
        when(warrantyApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<WarrantyDtoOut> response = controller.update(WARRANTY_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(warrantyApiMapper).toDomain(dtoIn);
        verify(warrantyUseCase).update(WARRANTY_ID, model);
        verify(warrantyApiMapper).toDto(model);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(WARRANTY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(warrantyUseCase).delete(WARRANTY_ID);
    }

    @Test
    void toggleActive_returnsNoContent() {
        ResponseEntity<Void> response = controller.toggleActive(WARRANTY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(warrantyUseCase).toggleActive(WARRANTY_ID);
    }
}
