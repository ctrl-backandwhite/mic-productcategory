package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.usecase.CategoryUseCase.BulkCategoryRow;
import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.valueobject.CategoryStatus;
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
class CategoryUseCaseImplExtraTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryUseCaseImpl useCase;

    @Test
    void findCategoriesPaged_ascendingSort() {
        Page<Category> page = new PageImpl<>(List.of());
        when(categoryRepository.findCategoriesPaged(anyString(), any(), any(), any(), anyInt(), any(Pageable.class)))
                .thenReturn(page);
        Page<Category> result = useCase.findCategoriesPaged("en", CategoryStatus.PUBLISHED, true, "name", 1, 0, 20,
                "level", true);
        assertThat(result).isNotNull();
    }

    @Test
    void findCategoriesPaged_descendingSort() {
        Page<Category> page = new PageImpl<>(List.of());
        when(categoryRepository.findCategoriesPaged(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        useCase.findCategoriesPaged("en", null, null, null, null, 0, 20, "level", false);
        verify(categoryRepository).findCategoriesPaged(eq("en"), eq(null), eq(null), eq(null), eq(null),
                any(Pageable.class));
    }

    @Test
    void bulkUpdateStatus_delegates() {
        useCase.bulkUpdateStatus(List.of("a"), "published");
        verify(categoryRepository).bulkUpdateStatus(List.of("a"), CategoryStatus.PUBLISHED);
    }

    @Test
    void publishAllDrafts_delegates() {
        when(categoryRepository.publishAllDrafts()).thenReturn(5);
        assertThat(useCase.publishAllDrafts()).isEqualTo(5);
    }

    @Test
    void bulkCreate_emptyRows_returnsZero() {
        BulkCategoryResult result = useCase.bulkCreate(List.of());
        assertThat(result.getTotalRows()).isZero();
    }

    @Test
    void bulkCreate_fullRow_allNew() {
        CategoryTranslation l1 = CategoryTranslation.builder().locale("es").name("L1").build();
        CategoryTranslation l2 = CategoryTranslation.builder().locale("es").name("L2").build();
        CategoryTranslation l3 = CategoryTranslation.builder().locale("es").name("L3").build();
        BulkCategoryRow row = new BulkCategoryRow(List.of(l1), List.of(l2), List.of(l3));

        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.empty());
        when(categoryRepository.saveAndReturnId(any(Category.class))).thenReturn("gen-id");

        BulkCategoryResult result = useCase.bulkCreate(List.of(row));
        assertThat(result.getCreated()).isEqualTo(3);
        assertThat(result.getSkipped()).isZero();
    }

    @Test
    void bulkCreate_existingL1L2L3_allSkipped() {
        CategoryTranslation l1 = CategoryTranslation.builder().locale("es").name("L1").build();
        CategoryTranslation l2 = CategoryTranslation.builder().locale("es").name("L2").build();
        CategoryTranslation l3 = CategoryTranslation.builder().locale("es").name("L3").build();
        BulkCategoryRow row = new BulkCategoryRow(List.of(l1), List.of(l2), List.of(l3));

        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.of("existing"));

        BulkCategoryResult result = useCase.bulkCreate(List.of(row));
        assertThat(result.getSkipped()).isEqualTo(3);
        assertThat(result.getCreated()).isZero();
    }

    @Test
    void bulkCreate_nullL1_skipsRow() {
        BulkCategoryRow row = new BulkCategoryRow(null, null, null);
        BulkCategoryResult result = useCase.bulkCreate(List.of(row));
        assertThat(result.getCreated()).isZero();
    }

    @Test
    void bulkCreate_onlyL1_stopsAfterL1() {
        CategoryTranslation l1 = CategoryTranslation.builder().locale("es").name("L1").build();
        BulkCategoryRow row = new BulkCategoryRow(List.of(l1), null, null);
        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.empty());
        when(categoryRepository.saveAndReturnId(any(Category.class))).thenReturn("gen-id");

        BulkCategoryResult result = useCase.bulkCreate(List.of(row));
        assertThat(result.getCreated()).isEqualTo(1);
    }

    @Test
    void bulkCreate_l1AndL2_noL3() {
        CategoryTranslation l1 = CategoryTranslation.builder().locale("es").name("L1").build();
        CategoryTranslation l2 = CategoryTranslation.builder().locale("es").name("L2").build();
        BulkCategoryRow row = new BulkCategoryRow(List.of(l1), List.of(l2), null);
        when(categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(anyString(), anyString(), anyInt(),
                any())).thenReturn(Optional.empty());
        when(categoryRepository.saveAndReturnId(any(Category.class))).thenReturn("gen-id");

        BulkCategoryResult result = useCase.bulkCreate(List.of(row));
        assertThat(result.getCreated()).isEqualTo(2);
    }
}
