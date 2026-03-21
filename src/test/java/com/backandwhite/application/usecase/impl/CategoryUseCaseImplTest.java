package com.backandwhite.application.usecase.impl;

import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.valureobject.CategoryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.backandwhite.provider.CategoryProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryUseCaseImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryUseCaseImpl categoryUseCase;

    @Test
    void findCategories_returnsRootCategoriesInTree() {
        Category root = category();
        Category child = otherCategory();

        when(categoryRepository.findCategories("es", null, null)).thenReturn(List.of(root, child));

        List<Category> result = categoryUseCase.findCategories("es", null, null);

        // buildTree returns only root categories (parentId == null)
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(CATEGORY_ID);
        // child is nested inside root's subCategories
        assertThat(result.getFirst().getSubCategories()).hasSize(1);
        verify(categoryRepository).findCategories("es", null, null);
    }

    @Test
    void findCategories_emptyList_returnsEmpty() {
        when(categoryRepository.findCategories("es", null, null)).thenReturn(List.of());

        List<Category> result = categoryUseCase.findCategories("es", null, null);

        assertThat(result).isEmpty();
        verify(categoryRepository).findCategories("es", null, null);
    }

    @Test
    void findById_existingCategory_returnsCategory() {
        Category model = category();
        when(categoryRepository.findById(CATEGORY_ID, "es")).thenReturn(Optional.of(model));

        Category result = categoryUseCase.findById(CATEGORY_ID, "es");

        assertSame(model, result);
        verify(categoryRepository).findById(CATEGORY_ID, "es");
    }

    @Test
    void findById_missingCategory_throwsEntityNotFoundException() {
        when(categoryRepository.findById("non-existent", "es")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> categoryUseCase.findById("non-existent", "es"));
        verify(categoryRepository).findById("non-existent", "es");
    }

    @Test
    void create_delegatesToRepository() {
        Category model = category();
        when(categoryRepository.save(model)).thenReturn(model);

        Category result = categoryUseCase.create(model);

        assertSame(model, result);
        verify(categoryRepository).save(model);
    }

    @Test
    void update_delegatesToRepository() {
        Category model = category();
        when(categoryRepository.update(CATEGORY_ID, model)).thenReturn(model);

        Category result = categoryUseCase.update(CATEGORY_ID, model);

        assertSame(model, result);
        verify(categoryRepository).update(CATEGORY_ID, model);
    }

    @Test
    void delete_delegatesToRepository() {
        categoryUseCase.delete(CATEGORY_ID);

        verify(categoryRepository).delete(CATEGORY_ID);
    }

    @Test
    void publishCategory_draftCategory_setsPublished() {
        Category draft = category().withStatus(CategoryStatus.DRAFT);
        when(categoryRepository.findById(CATEGORY_ID, null)).thenReturn(Optional.of(draft));

        categoryUseCase.publishCategory(CATEGORY_ID);

        verify(categoryRepository).updateStatus(CATEGORY_ID, CategoryStatus.PUBLISHED);
    }

    @Test
    void publishCategory_publishedCategory_setsDraft() {
        Category published = category().withStatus(CategoryStatus.PUBLISHED);
        when(categoryRepository.findById(CATEGORY_ID, null)).thenReturn(Optional.of(published));

        categoryUseCase.publishCategory(CATEGORY_ID);

        verify(categoryRepository).updateStatus(CATEGORY_ID, CategoryStatus.DRAFT);
    }

    @Test
    void publishCategory_notFound_throwsEntityNotFoundException() {
        when(categoryRepository.findById("missing", null)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> categoryUseCase.publishCategory("missing"));
    }

    @Test
    void toggleActive_delegatesToRepository() {
        categoryUseCase.toggleActive(CATEGORY_ID, false);

        verify(categoryRepository).toggleActive(CATEGORY_ID, false);
    }

    @Test
    void toggleFeatured_delegatesToRepository() {
        categoryUseCase.toggleFeatured(CATEGORY_ID, true);

        verify(categoryRepository).toggleFeatured(CATEGORY_ID, true);
    }

    @Test
    void deleteAll_delegatesToRepository() {
        List<String> ids = List.of(CATEGORY_ID, OTHER_CATEGORY_ID);

        categoryUseCase.deleteAll(ids);

        verify(categoryRepository).deleteAll(ids);
    }

    @Test
    void findFeatured_delegatesToRepository() {
        List<Category> featured = List.of(category());
        when(categoryRepository.findFeatured("es")).thenReturn(featured);

        List<Category> result = categoryUseCase.findFeatured("es");

        assertSame(featured, result);
        verify(categoryRepository).findFeatured("es");
    }
}
