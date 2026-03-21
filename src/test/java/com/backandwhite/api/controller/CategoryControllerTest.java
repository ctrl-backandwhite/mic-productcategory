package com.backandwhite.api.controller;

import com.backandwhite.api.dto.in.CategoryDtoIn;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.mapper.CategoryApiMapper;
import com.backandwhite.application.usecase.CategorySyncUseCase;
import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.domain.model.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.backandwhite.provider.CategoryProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryUseCase categoryUseCase;

    @Mock
    private CategorySyncUseCase categorySyncUseCase;

    @Mock
    private CategoryApiMapper categoryApiMapper;

    @InjectMocks
    private CategoryController controller;

    @Test
    void findByLocale_returnsCategoryList() {
        List<Category> categories = List.of(category());
        List<CategoryDtoOut> dtoOuts = List.of(categoryDtoOut(CATEGORY_ID));

        when(categoryUseCase.findCategories("es", null, null)).thenReturn(categories);
        when(categoryApiMapper.toDtoList(categories)).thenReturn(dtoOuts);

        ResponseEntity<List<CategoryDtoOut>> response = controller.findByLocale("es", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOuts);
        verify(categoryUseCase).findCategories("es", null, null);
        verify(categoryApiMapper).toDtoList(categories);
    }

    @Test
    void getById_returnsCategory() {
        Category model = category();
        CategoryDtoOut dtoOut = categoryDtoOut(CATEGORY_ID);

        when(categoryUseCase.findById(CATEGORY_ID, "es")).thenReturn(model);
        when(categoryApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<CategoryDtoOut> response = controller.getById(CATEGORY_ID, "es");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(categoryUseCase).findById(CATEGORY_ID, "es");
        verify(categoryApiMapper).toDto(model);
    }

    @Test
    void create_returnsCreatedCategory() {
        CategoryDtoIn dtoIn = categoryDtoIn();
        Category model = category();
        CategoryDtoOut dtoOut = categoryDtoOut(CATEGORY_ID);

        when(categoryApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(categoryUseCase.create(model)).thenReturn(model);
        when(categoryApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<CategoryDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(categoryApiMapper).toDomain(dtoIn);
        verify(categoryUseCase).create(model);
        verify(categoryApiMapper).toDto(model);
    }

    @Test
    void update_returnsUpdatedCategory() {
        CategoryDtoIn dtoIn = categoryDtoIn();
        Category model = category();
        CategoryDtoOut dtoOut = categoryDtoOut(CATEGORY_ID);

        when(categoryApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(categoryUseCase.update(CATEGORY_ID, model)).thenReturn(model);
        when(categoryApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<CategoryDtoOut> response = controller.update(CATEGORY_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(categoryApiMapper).toDomain(dtoIn);
        verify(categoryUseCase).update(CATEGORY_ID, model);
        verify(categoryApiMapper).toDto(model);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(CATEGORY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryUseCase).delete(CATEGORY_ID);
    }

    @Test
    void toggleActive_returnsNoContent() {
        ResponseEntity<Void> response = controller.toggleActive(CATEGORY_ID, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryUseCase).toggleActive(CATEGORY_ID, true);
    }

    @Test
    void toggleFeatured_returnsNoContent() {
        ResponseEntity<Void> response = controller.toggleFeatured(CATEGORY_ID, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryUseCase).toggleFeatured(CATEGORY_ID, true);
    }

    @Test
    void publish_returnsNoContent() {
        ResponseEntity<Void> response = controller.publish(CATEGORY_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryUseCase).publishCategory(CATEGORY_ID);
    }

    @Test
    void findFeatured_returnsCategoryList() {
        List<Category> categories = List.of(category());
        List<CategoryDtoOut> dtoOuts = List.of(categoryDtoOut(CATEGORY_ID));

        when(categoryUseCase.findFeatured("es")).thenReturn(categories);
        when(categoryApiMapper.toDtoList(categories)).thenReturn(dtoOuts);

        ResponseEntity<List<CategoryDtoOut>> response = controller.findFeatured("es");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOuts);
        verify(categoryUseCase).findFeatured("es");
    }
}
