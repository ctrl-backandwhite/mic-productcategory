package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
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
class ProductDetailUseCaseImplTest {

    @Mock
    private ProductDetailRepository productDetailRepository;
    @Mock
    private DropshippingPort cjDropshippingClient;
    @Mock
    private CjProductDetailMapper cjProductDetailMapper;

    @InjectMocks
    private ProductDetailUseCaseImpl useCase;

    @Test
    void getOrFetchFromCj_existingInDb_returnsCached() {
        ProductDetail cached = ProductDetail.builder().pid("pid-1").build();
        when(productDetailRepository.findByPid("pid-1")).thenReturn(Optional.of(cached));

        ProductDetail result = useCase.getOrFetchFromCj("pid-1", "en");

        assertThat(result).isSameAs(cached);
        verify(cjDropshippingClient, never()).getProductDetail(any());
    }

    @Test
    void getOrFetchFromCj_notInDb_fetchesAndPersists() {
        when(productDetailRepository.findByPid("pid-1")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(ProductDetail.builder().pid("pid-1").build()));
        CjProductDetailDto dto = new CjProductDetailDto();
        dto.setPid("pid-1");
        when(cjDropshippingClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toDomain(dto)).thenReturn(ProductDetail.builder().pid("pid-1").build());

        ProductDetail result = useCase.getOrFetchFromCj("pid-1", "en");

        assertThat(result.getPid()).isEqualTo("pid-1");
        verify(productDetailRepository).save(any());
    }

    @Test
    void getOrFetchFromCj_cjThrows_throwsEntityNotFound() {
        when(productDetailRepository.findByPid("pid-1")).thenReturn(Optional.empty());
        when(cjDropshippingClient.getProductDetail("pid-1")).thenThrow(new RuntimeException("boom"));
        assertThatThrownBy(() -> useCase.getOrFetchFromCj("pid-1", "en")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getOrFetchFromCj_cjReturnsNull_throwsEntityNotFound() {
        when(productDetailRepository.findByPid("pid-1")).thenReturn(Optional.empty());
        when(cjDropshippingClient.getProductDetail("pid-1")).thenReturn(null);
        assertThatThrownBy(() -> useCase.getOrFetchFromCj("pid-1", "en")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getOrFetchFromCj_persistedButNotFound_throwsEntityNotFound() {
        when(productDetailRepository.findByPid("pid-1")).thenReturn(Optional.empty()).thenReturn(Optional.empty());
        CjProductDetailDto dto = new CjProductDetailDto();
        dto.setPid("pid-1");
        when(cjDropshippingClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toDomain(dto)).thenReturn(ProductDetail.builder().pid("pid-1").build());

        assertThatThrownBy(() -> useCase.getOrFetchFromCj("pid-1", "en")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void publishVariant_draftToPublished() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").status(ProductStatus.DRAFT).build();
        when(productDetailRepository.findVariantByVid("vid", null)).thenReturn(Optional.of(v));
        useCase.publishVariant("vid");
        verify(productDetailRepository).updateVariantStatus("vid", ProductStatus.PUBLISHED);
    }

    @Test
    void publishVariant_publishedToDraft() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").status(ProductStatus.PUBLISHED).build();
        when(productDetailRepository.findVariantByVid("vid", null)).thenReturn(Optional.of(v));
        useCase.publishVariant("vid");
        verify(productDetailRepository).updateVariantStatus("vid", ProductStatus.DRAFT);
    }

    @Test
    void publishVariant_notFound_throws() {
        when(productDetailRepository.findVariantByVid("vid", null)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.publishVariant("vid")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAllVariantsPaged_validStatusAndSort() {
        Page<ProductDetailVariant> page = new PageImpl<>(List.of());
        when(productDetailRepository.findVariantsFiltered(eq("en"), eq("search"), eq(ProductStatus.DRAFT), eq("pid"),
                any(Pageable.class))).thenReturn(page);

        Page<ProductDetailVariant> result = useCase.findAllVariantsPaged(0, 10, "en", "search", "draft", "pid",
                "createdAt", true);
        assertThat(result).isNotNull();
    }

    @Test
    void findAllVariantsPaged_blankSortAndNullStatus_defaults() {
        Page<ProductDetailVariant> page = new PageImpl<>(List.of());
        when(productDetailRepository.findVariantsFiltered(any(), any(), eq(null), any(), any(Pageable.class)))
                .thenReturn(page);
        Page<ProductDetailVariant> result = useCase.findAllVariantsPaged(0, 10, "en", null, null, null, " ", false);
        assertThat(result).isNotNull();
    }

    @Test
    void findVariantsByPid_delegates() {
        when(productDetailRepository.findVariantsByPid("pid", "en")).thenReturn(List.of());
        useCase.findVariantsByPid("pid", "en");
        verify(productDetailRepository).findVariantsByPid("pid", "en");
    }

    @Test
    void findVariantByVid_found() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").build();
        when(productDetailRepository.findVariantByVid("vid", "en")).thenReturn(Optional.of(v));
        assertThat(useCase.findVariantByVid("vid", "en")).isSameAs(v);
    }

    @Test
    void findVariantByVid_notFound_throws() {
        when(productDetailRepository.findVariantByVid("vid", "en")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.findVariantByVid("vid", "en")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createVariant_parentExists_savesWithGeneratedVid() {
        ProductDetailVariant v = ProductDetailVariant.builder().pid("pid").build();
        when(productDetailRepository.existsByPid("pid")).thenReturn(true);
        when(productDetailRepository.saveVariant(any(ProductDetailVariant.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        ProductDetailVariant result = useCase.createVariant(v);
        assertThat(result.getVid()).isNotNull();
    }

    @Test
    void createVariant_parentMissing_throws() {
        ProductDetailVariant v = ProductDetailVariant.builder().pid("pid").build();
        when(productDetailRepository.existsByPid("pid")).thenReturn(false);
        assertThatThrownBy(() -> useCase.createVariant(v)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateVariant_existing_preservesImmutableFields() {
        ProductDetailVariant existing = ProductDetailVariant.builder().vid("vid").pid("pid").build();
        ProductDetailVariant incoming = ProductDetailVariant.builder().pid("NEW_PID").build();
        when(productDetailRepository.findVariantByVid("vid", null)).thenReturn(Optional.of(existing));
        when(productDetailRepository.saveVariant(any(ProductDetailVariant.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProductDetailVariant result = useCase.updateVariant("vid", incoming);
        assertThat(result.getVid()).isEqualTo("vid");
        assertThat(result.getPid()).isEqualTo("pid");
    }

    @Test
    void updateVariant_notFound_throws() {
        when(productDetailRepository.findVariantByVid("vid", null)).thenReturn(Optional.empty());
        ProductDetailVariant empty = ProductDetailVariant.builder().build();
        assertThatThrownBy(() -> useCase.updateVariant("vid", empty)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteVariant_existing_deletes() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").build();
        when(productDetailRepository.findVariantByVid("vid", null)).thenReturn(Optional.of(v));
        useCase.deleteVariant("vid");
        verify(productDetailRepository).deleteVariant("vid");
    }

    @Test
    void deleteVariant_notFound_throws() {
        when(productDetailRepository.findVariantByVid("vid", null)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.deleteVariant("vid")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteVariants_delegates() {
        useCase.deleteVariants(List.of("a", "b"));
        verify(productDetailRepository).deleteVariants(List.of("a", "b"));
    }

    @Test
    void bulkUpdateVariantStatus_delegates() {
        useCase.bulkUpdateVariantStatus(List.of("a"), "published");
        verify(productDetailRepository).bulkUpdateVariantStatus(List.of("a"), ProductStatus.PUBLISHED);
    }

    @Test
    void bulkCreateVariants_allSuccess() {
        ProductDetailVariant v = ProductDetailVariant.builder().pid("pid").build();
        when(productDetailRepository.existsByPid("pid")).thenReturn(true);
        when(productDetailRepository.saveVariant(any())).thenAnswer(inv -> inv.getArgument(0));
        BulkImportResult result = useCase.bulkCreateVariants(List.of(v, v));
        assertThat(result.getCreated()).isEqualTo(2);
        assertThat(result.getFailed()).isZero();
    }

    @Test
    void bulkCreateVariants_missingParent_recordsErrors() {
        ProductDetailVariant v = ProductDetailVariant.builder().pid("pid").build();
        when(productDetailRepository.existsByPid("pid")).thenReturn(false);
        BulkImportResult result = useCase.bulkCreateVariants(List.of(v));
        assertThat(result.getCreated()).isZero();
        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
    }
}
