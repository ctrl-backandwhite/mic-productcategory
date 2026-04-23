package com.backandwhite.api.controller;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PageFilterRequest;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BulkProductDtoIn;
import com.backandwhite.api.dto.in.BulkStatusUpdateDtoIn;
import com.backandwhite.api.dto.in.BulkVariantDtoIn;
import com.backandwhite.api.dto.in.ProductDetailVariantDtoIn;
import com.backandwhite.api.dto.in.ProductDtoIn;
import com.backandwhite.api.dto.in.ProductFilterDto;
import com.backandwhite.api.dto.in.VariantFilterDto;
import com.backandwhite.api.dto.out.BulkImportResultDtoOut;
import com.backandwhite.api.dto.out.ProductDetailDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantDtoOut;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.dto.out.ProductSyncResultDtoOut;
import com.backandwhite.api.mapper.ProductApiMapper;
import com.backandwhite.api.mapper.ProductDetailApiMapper;
import com.backandwhite.application.service.PricingService;
import com.backandwhite.application.usecase.ProductDetailUseCase;
import com.backandwhite.application.usecase.ProductSyncUseCase;
import com.backandwhite.application.usecase.ProductUseCase;
import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductSyncResult;
import com.backandwhite.domain.valueobject.ProductStatus;
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
class ProductControllerExtraTest {

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
    void findAllPaged_returnsPaginated() {
        Page<Product> page = new PageImpl<>(List.of(product(CATEGORY_ID)));
        when(productUseCase.findAllPaged(anyString(), any(), any(), any(), anyInt(), anyInt(), anyString(),
                anyBoolean(), anyBoolean())).thenReturn(page);
        when(productApiMapper.toDto(any(Product.class))).thenReturn(productDtoOut(PRODUCT_ID, CATEGORY_ID));

        ResponseEntity<PaginationDtoOut<ProductDtoOut>> response = controller.findAllPaged("en", null, null, null, 0,
                20, "createdAt", true, false);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void search_withFilters() {
        ProductFilterDto filters = ProductFilterDto.builder().categoryId("cat-1").status(ProductStatus.PUBLISHED)
                .build();
        PageFilterRequest<ProductFilterDto> request = new PageFilterRequest<>();
        request.setFilters(filters);
        request.setLocale("en");
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("name");

        Page<Product> page = new PageImpl<>(List.of(product(CATEGORY_ID)));
        when(productUseCase.findAllPaged(anyString(), anyString(), anyString(), any(), anyInt(), anyInt(), any(),
                anyBoolean())).thenReturn(page);
        when(productApiMapper.toDto(any(Product.class))).thenReturn(productDtoOut(PRODUCT_ID, CATEGORY_ID));

        ResponseEntity<PaginationDtoOut<ProductDtoOut>> response = controller.search(request);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void search_nullFilters() {
        PageFilterRequest<ProductFilterDto> request = new PageFilterRequest<>();
        request.setLocale("en");
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("name");

        Page<Product> page = new PageImpl<>(List.of());
        when(productUseCase.findAllPaged(anyString(), any(), any(), any(), anyInt(), anyInt(), any(), anyBoolean()))
                .thenReturn(page);

        ResponseEntity<PaginationDtoOut<ProductDtoOut>> response = controller.search(request);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void bulkUpdateStatus_returnsNoContent() {
        BulkStatusUpdateDtoIn body = new BulkStatusUpdateDtoIn();
        body.setIds(List.of("id1"));
        body.setStatus("PUBLISHED");
        ResponseEntity<Void> response = controller.bulkUpdateStatus(body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productUseCase).bulkUpdateStatus(List.of("id1"), "PUBLISHED");
    }

    @Test
    void getProductDetail_returnsDto() {
        ProductDetail detail = ProductDetail.builder().pid("pid").build();
        when(productDetailUseCase.getOrFetchFromCj("pid", "en")).thenReturn(detail);
        when(productDetailApiMapper.toDto(detail)).thenReturn(new ProductDetailDtoOut());

        ResponseEntity<ProductDetailDtoOut> response = controller.getProductDetail("pid", "en");
        assertThat(response.getBody()).isNotNull();
        verify(pricingService).applyMarginsToProductDetail(detail);
    }

    @Test
    void findAllVariantsPaged_returnsPagedResponse() {
        Page<ProductDetailVariant> page = new PageImpl<>(List.of(ProductDetailVariant.builder().vid("v1").build()));
        when(productDetailUseCase.findAllVariantsPaged(anyInt(), anyInt(), anyString(), any(), any(), any(), any(),
                anyBoolean())).thenReturn(page);
        when(productDetailApiMapper.toVariantDto(any())).thenReturn(new ProductDetailVariantDtoOut());

        ResponseEntity<PaginationDtoOut<ProductDetailVariantDtoOut>> response = controller.findAllVariantsPaged("en", 0,
                20, null, null, null, null, false);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void searchVariants_withFilters() {
        VariantFilterDto filters = VariantFilterDto.builder().status(ProductStatus.PUBLISHED).pid("pid").build();
        PageFilterRequest<VariantFilterDto> request = new PageFilterRequest<>();
        request.setFilters(filters);
        request.setLocale(null);
        request.setPage(0);
        request.setSize(10);

        Page<ProductDetailVariant> page = new PageImpl<>(List.of(ProductDetailVariant.builder().build()));
        when(productDetailUseCase.findAllVariantsPaged(anyInt(), anyInt(), anyString(), any(), anyString(), any(),
                any(), anyBoolean())).thenReturn(page);
        when(productDetailApiMapper.toVariantDto(any())).thenReturn(new ProductDetailVariantDtoOut());

        ResponseEntity<PaginationDtoOut<ProductDetailVariantDtoOut>> response = controller.searchVariants(request);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void searchVariants_nullFilters() {
        PageFilterRequest<VariantFilterDto> request = new PageFilterRequest<>();
        request.setLocale("en");
        request.setPage(0);
        request.setSize(10);

        Page<ProductDetailVariant> page = new PageImpl<>(List.of());
        when(productDetailUseCase.findAllVariantsPaged(anyInt(), anyInt(), anyString(), any(), any(), any(), any(),
                anyBoolean())).thenReturn(page);

        ResponseEntity<PaginationDtoOut<ProductDetailVariantDtoOut>> response = controller.searchVariants(request);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void findVariantsByPid_returnsList() {
        ProductDetailVariant v = ProductDetailVariant.builder().build();
        when(productDetailUseCase.findVariantsByPid("pid", "en")).thenReturn(List.of(v));
        when(productDetailApiMapper.toVariantDtoList(any())).thenReturn(List.of(new ProductDetailVariantDtoOut()));
        ResponseEntity<List<ProductDetailVariantDtoOut>> response = controller.findVariantsByPid("pid", "en");
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void findVariantByVid_returnsDto() {
        ProductDetailVariant v = ProductDetailVariant.builder().build();
        when(productDetailUseCase.findVariantByVid("vid", "en")).thenReturn(v);
        when(productDetailApiMapper.toVariantDto(v)).thenReturn(new ProductDetailVariantDtoOut());
        ResponseEntity<ProductDetailVariantDtoOut> response = controller.findVariantByVid("vid", "en");
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void createVariant_returnsCreated() {
        ProductDetailVariantDtoIn dto = ProductDetailVariantDtoIn.builder().build();
        ProductDetailVariant v = ProductDetailVariant.builder().build();
        when(productDetailApiMapper.toVariantDomain(dto)).thenReturn(v);
        when(productDetailUseCase.createVariant(v)).thenReturn(v);
        when(productDetailApiMapper.toVariantDto(v)).thenReturn(new ProductDetailVariantDtoOut());
        ResponseEntity<ProductDetailVariantDtoOut> response = controller.createVariant(dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateVariant_returnsUpdated() {
        ProductDetailVariantDtoIn dto = ProductDetailVariantDtoIn.builder().build();
        ProductDetailVariant v = ProductDetailVariant.builder().build();
        when(productDetailApiMapper.toVariantDomain(dto)).thenReturn(v);
        when(productDetailUseCase.updateVariant(eq("vid"), any())).thenReturn(v);
        when(productDetailApiMapper.toVariantDto(v)).thenReturn(new ProductDetailVariantDtoOut());
        ResponseEntity<ProductDetailVariantDtoOut> response = controller.updateVariant("vid", dto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteVariant_returnsNoContent() {
        ResponseEntity<Void> response = controller.deleteVariant("vid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productDetailUseCase).deleteVariant("vid");
    }

    @Test
    void deleteVariants_returnsNoContent() {
        ResponseEntity<Void> response = controller.deleteVariants(List.of("a", "b"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productDetailUseCase).deleteVariants(List.of("a", "b"));
    }

    @Test
    void publishVariant_returnsNoContent() {
        ResponseEntity<Void> response = controller.publishVariant("vid");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void bulkUpdateVariantStatus_returnsNoContent() {
        BulkStatusUpdateDtoIn body = new BulkStatusUpdateDtoIn();
        body.setIds(List.of("v1"));
        body.setStatus("DRAFT");
        ResponseEntity<Void> response = controller.bulkUpdateVariantStatus(body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void bulkCreateProducts_returnsResult() {
        ProductDtoIn row = productDtoIn(CATEGORY_ID);
        BulkProductDtoIn dto = BulkProductDtoIn.builder().rows(List.of(row)).build();
        when(productApiMapper.toDomain(row)).thenReturn(product(CATEGORY_ID));
        BulkImportResult result = BulkImportResult.builder().created(1).failed(0).totalRows(1)
                .errors(java.util.List.of()).build();
        when(productUseCase.bulkCreate(any())).thenReturn(result);

        ResponseEntity<BulkImportResultDtoOut> response = controller.bulkCreateProducts(dto);
        assertThat(response.getBody().getCreated()).isEqualTo(1);
    }

    @Test
    void bulkCreateVariants_returnsResult() {
        ProductDetailVariantDtoIn row = ProductDetailVariantDtoIn.builder().build();
        BulkVariantDtoIn dto = BulkVariantDtoIn.builder().rows(List.of(row)).build();
        when(productDetailApiMapper.toVariantDomain(row)).thenReturn(ProductDetailVariant.builder().build());
        BulkImportResult result = BulkImportResult.builder().created(1).failed(0).totalRows(1)
                .errors(java.util.List.of()).build();
        when(productDetailUseCase.bulkCreateVariants(any())).thenReturn(result);

        ResponseEntity<BulkImportResultDtoOut> response = controller.bulkCreateVariants(dto);
        assertThat(response.getBody().getCreated()).isEqualTo(1);
    }

    @Test
    void syncFromCjDropshipping_returnsResult() {
        when(productSyncUseCase.syncFromCjDropshipping(true))
                .thenReturn(ProductSyncResult.builder().created(10).build());
        ResponseEntity<ProductSyncResultDtoOut> response = controller.syncFromCjDropshipping(true);
        assertThat(response.getBody().getCreated()).isEqualTo(10);
    }

    @Test
    void syncPageFromCjDropshipping_returnsResult() {
        when(productSyncUseCase.syncPageFromCjDropshipping(1, 10, true, null))
                .thenReturn(ProductSyncResult.builder().created(5).build());
        ResponseEntity<ProductSyncResultDtoOut> response = controller.syncPageFromCjDropshipping(1, 10, true, null);
        assertThat(response.getBody().getCreated()).isEqualTo(5);
    }

    @Test
    void discoverNewByCategory_returnsResult() {
        when(productSyncUseCase.discoverNewProductsByCategory(0))
                .thenReturn(ProductSyncResult.builder().created(3).build());
        ResponseEntity<ProductSyncResultDtoOut> response = controller.discoverNewByCategory(0);
        assertThat(response.getBody().getCreated()).isEqualTo(3);
    }
}
