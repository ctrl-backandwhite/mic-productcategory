package com.backandwhite.api.controller;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.in.ProductDtoIn;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.mapper.ProductApiMapper;
import com.backandwhite.api.mapper.ProductDetailApiMapper;
import com.backandwhite.application.service.PricingService;
import com.backandwhite.application.usecase.ProductDetailUseCase;
import com.backandwhite.application.usecase.ProductSyncUseCase;
import com.backandwhite.application.usecase.ProductUseCase;
import com.backandwhite.domain.model.Product;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductUseCase productUseCase;

    @Mock
    private ProductDetailUseCase productDetailUseCase;

    @Mock
    private ProductSyncUseCase productSyncUseCase;

    @Mock
    private ProductApiMapper productApiMapper;

    @Mock
    private ProductDetailApiMapper productDetailApiMapper;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private ProductController controller;

    @Test
    void findByCategoryId_returnsProductList() {
        List<Product> products = List.of(product(CATEGORY_ID));
        List<ProductDtoOut> dtoOuts = List.of(productDtoOut(PRODUCT_ID, CATEGORY_ID));

        when(productUseCase.findByCategoryId(CATEGORY_ID, "es", null)).thenReturn(products);
        when(productApiMapper.toDtoList(products)).thenReturn(dtoOuts);

        ResponseEntity<List<ProductDtoOut>> response = controller.findByCategoryId(CATEGORY_ID, "es", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOuts);
        verify(productUseCase).findByCategoryId(CATEGORY_ID, "es", null);
        verify(productApiMapper).toDtoList(products);
    }

    @Test
    void getById_returnsProduct() {
        Product model = product(CATEGORY_ID);
        ProductDtoOut dtoOut = productDtoOut(PRODUCT_ID, CATEGORY_ID);

        when(productUseCase.findById(PRODUCT_ID, "es")).thenReturn(model);
        when(productApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<ProductDtoOut> response = controller.getById(PRODUCT_ID, "es");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(productUseCase).findById(PRODUCT_ID, "es");
        verify(productApiMapper).toDto(model);
    }

    @Test
    void create_returnsCreatedProduct() {
        ProductDtoIn dtoIn = productDtoIn(CATEGORY_ID);
        Product model = product(CATEGORY_ID);
        ProductDtoOut dtoOut = productDtoOut(PRODUCT_ID, CATEGORY_ID);

        when(productApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(productUseCase.create(model)).thenReturn(model);
        when(productApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<ProductDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(productApiMapper).toDomain(dtoIn);
        verify(productUseCase).create(model);
        verify(productApiMapper).toDto(model);
    }

    @Test
    void update_returnsUpdatedProduct() {
        ProductDtoIn dtoIn = productDtoIn(CATEGORY_ID);
        Product model = product(CATEGORY_ID);
        ProductDtoOut dtoOut = productDtoOut(PRODUCT_ID, CATEGORY_ID);

        when(productApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(productUseCase.update(PRODUCT_ID, model)).thenReturn(model);
        when(productApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<ProductDtoOut> response = controller.update(PRODUCT_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(productApiMapper).toDomain(dtoIn);
        verify(productUseCase).update(PRODUCT_ID, model);
        verify(productApiMapper).toDto(model);
    }

    @Test
    void deleteAll_returnsNoContent() {
        List<String> ids = List.of(PRODUCT_ID);

        ResponseEntity<Void> response = controller.deleteAll(ids);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productUseCase).deleteAll(ids);
    }

    @Test
    void publishProduct_returnsNoContent() {
        ResponseEntity<Void> response = controller.publishProduct(PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productUseCase).publishProduct(PRODUCT_ID);
    }
}
