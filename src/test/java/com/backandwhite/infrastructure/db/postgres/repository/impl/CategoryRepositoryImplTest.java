package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.valureobject.CategoryStatus;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.CategoryInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.CategoryJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.CategorySpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.backandwhite.provider.CategoryProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryImplTest {

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @Mock
    private CategoryInfraMapper categoryInfraMapper;

    @InjectMocks
    private CategoryRepositoryImpl categoryRepository;

    @Test
    void findCategories_mapsEntitiesToDomain() {
        CategoryEntity entity = categoryEntity();
        Category model = category();

        try (MockedStatic<CategorySpecification> specMock = mockStatic(CategorySpecification.class)) {
            specMock.when(() -> CategorySpecification.withFilters(any(), any(), any()))
                    .thenReturn((org.springframework.data.jpa.domain.Specification<com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity>) (root, query, cb) -> cb.conjunction());
            when(categoryJpaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                    .thenReturn(List.of(entity));
            when(categoryInfraMapper.toDomain(entity)).thenReturn(model);

            List<Category> result = categoryRepository.findCategories("es", null, null);

            assertThat(result).containsExactly(model);
            verify(categoryInfraMapper).toDomain(entity);
        }
    }

    @Test
    void delete_existingCategory_deletesEntity() {
        CategoryEntity entity = categoryEntity();
        when(categoryJpaRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(entity));

        categoryRepository.delete(CATEGORY_ID);

        verify(categoryJpaRepository).delete(entity);
    }

    @Test
    void toggleActive_updatesAndPersists() {
        CategoryEntity entity = categoryEntity();
        when(categoryJpaRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(entity));

        categoryRepository.toggleActive(CATEGORY_ID, false);

        assertThat(entity.getActive()).isFalse();
        verify(categoryJpaRepository).save(entity);
    }

    @Test
    void toggleFeatured_updatesAndPersists() {
        CategoryEntity entity = categoryEntity();
        when(categoryJpaRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(entity));

        categoryRepository.toggleFeatured(CATEGORY_ID, true);

        assertThat(entity.getFeatured()).isTrue();
        verify(categoryJpaRepository).save(entity);
    }

    @Test
    void bulkUpdateStatus_delegatesToJpa() {
        List<String> ids = List.of(CATEGORY_ID, OTHER_CATEGORY_ID);

        categoryRepository.bulkUpdateStatus(ids, CategoryStatus.PUBLISHED);

        verify(categoryJpaRepository).bulkUpdateStatus(ids, CategoryStatus.PUBLISHED);
    }

    @Test
    void updateStatus_existingCategory_updatesAndPersists() {
        CategoryEntity entity = categoryEntity();
        when(categoryJpaRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(entity));

        categoryRepository.updateStatus(CATEGORY_ID, CategoryStatus.PUBLISHED);

        assertThat(entity.getStatus()).isEqualTo(CategoryStatus.PUBLISHED);
        verify(categoryJpaRepository).save(entity);
    }
}
