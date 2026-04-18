package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.exception.ExternalServiceException;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductSyncResult;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListV2ContentDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListV2ItemDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class ProductSyncUseCaseImplTest {

    @Mock
    private DropshippingPort cjClient;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CjProductDetailMapper cjProductDetailMapper;

    @InjectMocks
    private ProductSyncUseCaseImpl useCase;

    private static CjProductListPageDto pageWith(int total, CjProductListV2ItemDto... items) {
        CjProductListPageDto page = new CjProductListPageDto();
        page.setTotalRecords(total);
        CjProductListV2ContentDto content = new CjProductListV2ContentDto();
        content.setProductList(List.of(items));
        page.setContent(List.of(content));
        return page;
    }

    private static CjProductListV2ItemDto item(String id) {
        CjProductListV2ItemDto i = new CjProductListV2ItemDto();
        i.setId(id);
        return i;
    }

    @Test
    void syncFromCjDropshipping_emptyPage_completesEarly() {
        Page<String> emptyPage = new PageImpl<>(List.of());
        when(productRepository.findAllProductIds(0, 100)).thenReturn(emptyPage);

        ProductSyncResult result = useCase.syncFromCjDropshipping(false);
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void syncFromCjDropshipping_happyPath_syncsAndBulkSaves() {
        Page<String> page1 = new PageImpl<>(List.of("pid-1"));
        when(productRepository.findAllProductIds(0, 100)).thenReturn(page1);
        CjProductDetailDto dto = new CjProductDetailDto();
        when(cjClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toProduct(dto)).thenReturn(Product.builder().id("pid-1").build());
        when(productRepository.bulkSyncProducts(anyList(), anyBoolean())).thenReturn(new int[]{1, 0});

        ProductSyncResult result = useCase.syncFromCjDropshipping(false);
        assertThat(result.getCreated()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void syncFromCjDropshipping_cjExternalError_skipsProduct() {
        Page<String> page1 = new PageImpl<>(List.of("pid-1"));
        when(productRepository.findAllProductIds(0, 100)).thenReturn(page1);
        when(cjClient.getProductDetail("pid-1")).thenThrow(new ExternalServiceException("ES001", "fail"));

        ProductSyncResult result = useCase.syncFromCjDropshipping(false);
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void syncFromCjDropshipping_bulkSaveThrows_catchesException() {
        Page<String> page1 = new PageImpl<>(List.of("pid-1"));
        when(productRepository.findAllProductIds(0, 100)).thenReturn(page1);
        CjProductDetailDto dto = new CjProductDetailDto();
        when(cjClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toProduct(dto)).thenReturn(Product.builder().id("pid-1").build());
        when(productRepository.bulkSyncProducts(anyList(), anyBoolean())).thenThrow(new RuntimeException("db fail"));

        ProductSyncResult result = useCase.syncFromCjDropshipping(false);
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void syncPageFromCjDropshipping_emptyIds_returnsZero() {
        Page<String> emptyPage = new PageImpl<>(List.of());
        when(productRepository.findAllProductIds(0, 10)).thenReturn(emptyPage);
        ProductSyncResult result = useCase.syncPageFromCjDropshipping(1, 10, false, null);
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void syncPageFromCjDropshipping_withCategoryIds_usesFilter() {
        Page<String> page = new PageImpl<>(List.of("pid-1"));
        when(productRepository.findProductIdsByCategoryIds(List.of("cat-1"), 0, 10)).thenReturn(page);
        CjProductDetailDto dto = new CjProductDetailDto();
        when(cjClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toProduct(dto)).thenReturn(Product.builder().id("pid-1").build());
        when(productRepository.bulkSyncProducts(anyList(), anyBoolean())).thenReturn(new int[]{1, 0});

        ProductSyncResult result = useCase.syncPageFromCjDropshipping(1, 10, false, List.of("cat-1"));
        assertThat(result.getCreated()).isEqualTo(1);
    }

    @Test
    void syncPageFromCjDropshipping_bulkFails_catches() {
        Page<String> page = new PageImpl<>(List.of("pid-1"));
        when(productRepository.findAllProductIds(0, 10)).thenReturn(page);
        CjProductDetailDto dto = new CjProductDetailDto();
        when(cjClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toProduct(dto)).thenReturn(Product.builder().id("pid-1").build());
        when(productRepository.bulkSyncProducts(anyList(), anyBoolean())).thenThrow(new RuntimeException("db fail"));

        ProductSyncResult result = useCase.syncPageFromCjDropshipping(1, 10, false, null);
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void discoverNewProductsByCategory_offsetBeyondTotal_returnsNothing() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>());
        ProductSyncResult result = useCase.discoverNewProductsByCategory(0);
        assertThat(result.isHasMore()).isFalse();
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void discoverNewProductsByCategory_offsetTooHigh_returnsNothing() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>(List.of("cat-1")));
        ProductSyncResult result = useCase.discoverNewProductsByCategory(5);
        assertThat(result.isHasMore()).isFalse();
    }

    @Test
    void discoverNewProductsByCategory_noPages_returnsCategoryUpdated() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>(List.of("cat-1")));
        when(cjClient.getProductListFiltered(anyInt(), anyInt(), eq("cat-1"), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(0));

        ProductSyncResult result = useCase.discoverNewProductsByCategory(0);
        verify(categoryRepository).updateLastDiscoveredAt("cat-1");
        assertThat(result.getCreated()).isZero();
    }

    @Test
    void discoverNewProductsByCategory_existingProduct_filtered() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>(List.of("cat-1")));
        when(cjClient.getProductListFiltered(anyInt(), anyInt(), eq("cat-1"), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(productRepository.existsById("pid-1")).thenReturn(true);

        ProductSyncResult result = useCase.discoverNewProductsByCategory(0);
        assertThat(result.getCreated()).isZero();
    }

    @Test
    void discoverNewProductsByCategory_cjErrorAndContinuesNextCategory() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>(List.of("cat-1")));
        when(cjClient.getProductListFiltered(anyInt(), anyInt(), eq("cat-1"), any(), any(), any(), anyInt(),
                anyString())).thenThrow(new ExternalServiceException("ES001", "fail"));

        ProductSyncResult result = useCase.discoverNewProductsByCategory(0);
        verify(categoryRepository).updateLastDiscoveredAt("cat-1");
        assertThat(result.getCreated()).isZero();
    }

    @Test
    void discoverNewProductsByCategory_newProducts_fetchesDetailAndSaves() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>(List.of("cat-1")));
        when(cjClient.getProductListFiltered(anyInt(), anyInt(), eq("cat-1"), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(productRepository.existsById("pid-1")).thenReturn(false);
        CjProductDetailDto dto = new CjProductDetailDto();
        when(cjClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toProduct(dto)).thenReturn(Product.builder().id("pid-1").build());
        when(productRepository.bulkSyncProducts(anyList(), anyBoolean())).thenReturn(new int[]{1, 0});

        ProductSyncResult result = useCase.discoverNewProductsByCategory(0);
        assertThat(result.getCreated()).isEqualTo(1);
    }

    @Test
    void discoverNewProductsByCategory_rateLimitRetried_thenFails() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>(List.of("cat-1")));
        when(cjClient.getProductListFiltered(anyInt(), anyInt(), eq("cat-1"), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(productRepository.existsById("pid-1")).thenReturn(false);
        when(cjClient.getProductDetail("pid-1")).thenThrow(new ExternalServiceException("ES003", "Too many requests"));

        ProductSyncResult result = useCase.discoverNewProductsByCategory(0);
        // retries exhausted, no created
        assertThat(result.getCreated()).isZero();
    }

    @Test
    void discoverNewProductsByCategory_genericException_handled() {
        when(categoryRepository.findAllLevel3Ids()).thenReturn(new java.util.ArrayList<>(List.of("cat-1")));
        when(cjClient.getProductListFiltered(anyInt(), anyInt(), eq("cat-1"), any(), any(), any(), anyInt(),
                anyString())).thenReturn(pageWith(1, item("pid-1")));
        when(productRepository.existsById("pid-1")).thenReturn(false);
        when(cjClient.getProductDetail("pid-1")).thenThrow(new RuntimeException("generic"));

        ProductSyncResult result = useCase.discoverNewProductsByCategory(0);
        assertThat(result.getCreated()).isZero();
    }
}
