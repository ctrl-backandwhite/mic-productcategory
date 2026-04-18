package com.backandwhite.api.controller;

import static com.backandwhite.provider.CategoryProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PageFilterRequest;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BulkCategoryDtoIn;
import com.backandwhite.api.dto.in.BulkCategoryRowDtoIn;
import com.backandwhite.api.dto.in.BulkStatusUpdateDtoIn;
import com.backandwhite.api.dto.in.CategoryFilterDto;
import com.backandwhite.api.dto.in.CategoryTranslationDtoIn;
import com.backandwhite.api.dto.out.BulkCategoryResultDtoOut;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.CategorySyncResultDtoOut;
import com.backandwhite.api.mapper.CategoryApiMapper;
import com.backandwhite.application.usecase.CategorySyncUseCase;
import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategorySyncResult;
import com.backandwhite.domain.valueobject.CategoryStatus;
import java.util.List;
import java.util.Map;
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
class CategoryControllerExtraTest {

    @Mock
    private CategoryUseCase categoryUseCase;
    @Mock
    private CategorySyncUseCase categorySyncUseCase;
    @Mock
    private CategoryApiMapper categoryApiMapper;

    @InjectMocks
    private CategoryController controller;

    @Test
    void findPaged_returnsPagedCategories() {
        Page<Category> page = new PageImpl<>(List.of(category()));
        when(categoryUseCase.findCategoriesPaged(anyString(), any(), any(), any(), any(), anyInt(), anyInt(),
                anyString(), anyBoolean())).thenReturn(page);
        when(categoryApiMapper.toDto(any(Category.class))).thenReturn(categoryDtoOut(CATEGORY_ID));

        ResponseEntity<PaginationDtoOut<CategoryDtoOut>> response = controller.findPaged("en", CategoryStatus.PUBLISHED,
                true, "name", 1, 0, 20, "level", true);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void search_withFilters() {
        CategoryFilterDto filters = CategoryFilterDto.builder().status(CategoryStatus.PUBLISHED).active(true).level(1)
                .build();
        PageFilterRequest<CategoryFilterDto> request = new PageFilterRequest<>();
        request.setFilters(filters);
        request.setLocale("en");
        request.setPage(0);
        request.setSize(10);

        Page<Category> page = new PageImpl<>(List.of(category()));
        when(categoryUseCase.findCategoriesPaged(anyString(), any(), anyBoolean(), any(), anyInt(), anyInt(), anyInt(),
                any(), anyBoolean())).thenReturn(page);
        when(categoryApiMapper.toDto(any(Category.class))).thenReturn(categoryDtoOut(CATEGORY_ID));

        ResponseEntity<PaginationDtoOut<CategoryDtoOut>> response = controller.search(request);
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void search_nullFilters() {
        PageFilterRequest<CategoryFilterDto> request = new PageFilterRequest<>();
        request.setPage(0);
        request.setSize(10);

        Page<Category> page = new PageImpl<>(List.of());
        when(categoryUseCase.findCategoriesPaged(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(),
                anyBoolean())).thenReturn(page);
        ResponseEntity<PaginationDtoOut<CategoryDtoOut>> response = controller.search(request);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(CATEGORY_ID);
        verify(categoryUseCase).delete(CATEGORY_ID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteAll_returnsNoContent() {
        ResponseEntity<Void> response = controller.deleteAll(List.of("id1"));
        verify(categoryUseCase).deleteAll(List.of("id1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void publish_returnsNoContent() {
        ResponseEntity<Void> response = controller.publish(CATEGORY_ID);
        verify(categoryUseCase).publishCategory(CATEGORY_ID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void bulkUpdateStatus_returnsNoContent() {
        BulkStatusUpdateDtoIn body = new BulkStatusUpdateDtoIn();
        body.setIds(List.of("a"));
        body.setStatus("PUBLISHED");
        ResponseEntity<Void> response = controller.bulkUpdateStatus(body);
        verify(categoryUseCase).bulkUpdateStatus(List.of("a"), "PUBLISHED");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void publishAllDrafts_returnsCount() {
        when(categoryUseCase.publishAllDrafts()).thenReturn(5);
        ResponseEntity<Map<String, Integer>> response = controller.publishAllDrafts();
        assertThat(response.getBody()).containsEntry("updated", 5);
    }

    @Test
    void toggleActive_returnsNoContent() {
        ResponseEntity<Void> response = controller.toggleActive(CATEGORY_ID, true);
        verify(categoryUseCase).toggleActive(CATEGORY_ID, true);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void toggleFeatured_returnsNoContent() {
        ResponseEntity<Void> response = controller.toggleFeatured(CATEGORY_ID, true);
        verify(categoryUseCase).toggleFeatured(CATEGORY_ID, true);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void findFeatured_returnsList() {
        when(categoryUseCase.findFeatured("en")).thenReturn(List.of(category()));
        when(categoryApiMapper.toDtoList(any())).thenReturn(List.of(categoryDtoOut(CATEGORY_ID)));
        ResponseEntity<List<CategoryDtoOut>> response = controller.findFeatured("en");
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void bulkCreate_returnsCreated() {
        BulkCategoryRowDtoIn row = BulkCategoryRowDtoIn.builder()
                .level1Translations(List.of(CategoryTranslationDtoIn.builder().locale("es").name("L1").build()))
                .level2Translations(null)
                .level3Translations(List.of(CategoryTranslationDtoIn.builder().locale("es").name("L3").build()))
                .build();
        BulkCategoryDtoIn dto = BulkCategoryDtoIn.builder().rows(List.of(row)).build();
        BulkCategoryResult result = BulkCategoryResult.builder().created(1).skipped(0).totalRows(1).build();
        when(categoryUseCase.bulkCreate(any())).thenReturn(result);

        ResponseEntity<BulkCategoryResultDtoOut> response = controller.bulkCreate(dto);
        assertThat(response.getBody().getCreated()).isEqualTo(1);
    }

    @Test
    void syncFromCjDropshipping_returnsResult() {
        when(categorySyncUseCase.syncFromCjDropshipping())
                .thenReturn(CategorySyncResult.builder().created(3).updated(2).total(5).build());
        ResponseEntity<CategorySyncResultDtoOut> response = controller.syncFromCjDropshipping();
        assertThat(response.getBody().getCreated()).isEqualTo(3);
    }
}
