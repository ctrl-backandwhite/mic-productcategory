package com.backandwhite.api.controller;

import static com.backandwhite.provider.BrandProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BrandDtoIn;
import com.backandwhite.api.dto.out.BrandDtoOut;
import com.backandwhite.api.mapper.BrandApiMapper;
import com.backandwhite.application.usecase.BrandUseCase;
import com.backandwhite.domain.model.Brand;
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
class BrandControllerTest {

    @Mock
    private BrandUseCase brandUseCase;

    @Mock
    private BrandApiMapper brandApiMapper;

    @InjectMocks
    private BrandController controller;

    @Test
    void findAll_returnsPaginatedBrands() {
        Page<Brand> page = new PageImpl<>(List.of(brand()));
        when(brandUseCase.findAll(null, null, 0, 20, "name", true)).thenReturn(page);
        when(brandApiMapper.toDto(any(Brand.class))).thenReturn(brandDtoOut());

        ResponseEntity<PaginationDtoOut<BrandDtoOut>> response = controller.findAll("token", null, null, 0, 20, "name",
                true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(brandUseCase).findAll(null, null, 0, 20, "name", true);
    }

    @Test
    void getById_returnsBrand() {
        Brand model = brand();
        BrandDtoOut dtoOut = brandDtoOut();

        when(brandUseCase.findById(BRAND_ID)).thenReturn(model);
        when(brandApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<BrandDtoOut> response = controller.getById("token", BRAND_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(brandUseCase).findById(BRAND_ID);
        verify(brandApiMapper).toDto(model);
    }

    @Test
    void getBySlug_returnsBrand() {
        Brand model = brand();
        BrandDtoOut dtoOut = brandDtoOut();

        when(brandUseCase.findBySlug(BRAND_SLUG)).thenReturn(model);
        when(brandApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<BrandDtoOut> response = controller.getBySlug("token", BRAND_SLUG);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(brandUseCase).findBySlug(BRAND_SLUG);
        verify(brandApiMapper).toDto(model);
    }

    @Test
    void create_returnsCreatedBrand() {
        BrandDtoIn dtoIn = brandDtoIn();
        Brand model = brand();
        BrandDtoOut dtoOut = brandDtoOut();

        when(brandApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(brandUseCase.create(model)).thenReturn(model);
        when(brandApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<BrandDtoOut> response = controller.create("token", dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(brandApiMapper).toDomain(dtoIn);
        verify(brandUseCase).create(model);
        verify(brandApiMapper).toDto(model);
    }

    @Test
    void update_returnsUpdatedBrand() {
        BrandDtoIn dtoIn = brandDtoIn();
        Brand model = brand();
        BrandDtoOut dtoOut = brandDtoOut();

        when(brandApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(brandUseCase.update(BRAND_ID, model)).thenReturn(model);
        when(brandApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<BrandDtoOut> response = controller.update("token", BRAND_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(brandApiMapper).toDomain(dtoIn);
        verify(brandUseCase).update(BRAND_ID, model);
        verify(brandApiMapper).toDto(model);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete("token", BRAND_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(brandUseCase).delete(BRAND_ID);
    }

    @Test
    void toggleStatus_returnsNoContent() {
        ResponseEntity<Void> response = controller.toggleStatus("token", BRAND_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(brandUseCase).toggleStatus(BRAND_ID);
    }
}
