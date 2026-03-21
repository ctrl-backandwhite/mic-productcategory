package com.backandwhite.provider;

import com.backandwhite.api.dto.in.CategoryDtoIn;
import com.backandwhite.api.dto.in.CategoryTranslationDtoIn;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.CategoryTranslationDtoOut;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.valureobject.CategoryStatus;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationId;

import java.util.List;

public final class CategoryProvider {

    public static final String CATEGORY_ID = "cat-electronics-001";
    public static final Integer CATEGORY_LEVEL = 1;
    public static final String CATEGORY_NAME_ES = "Electrónica";
    public static final String CATEGORY_NAME_EN = "Electronics";
    public static final String CATEGORY_NAME_PT = "Eletrônica";
    public static final CategoryStatus CATEGORY_STATUS = CategoryStatus.DRAFT;
    public static final Boolean CATEGORY_ACTIVE = true;
    public static final Boolean CATEGORY_FEATURED = false;

    public static final String OTHER_CATEGORY_ID = "cat-smartphones-002";
    public static final Integer OTHER_CATEGORY_LEVEL = 2;
    public static final String OTHER_CATEGORY_NAME_ES = "Smartphones";

    private CategoryProvider() {
    }

    public static Category category() {
        return Category.builder()
                .id(CATEGORY_ID)
                .parentId(null)
                .level(CATEGORY_LEVEL)
                .name(CATEGORY_NAME_ES)
                .status(CATEGORY_STATUS)
                .active(CATEGORY_ACTIVE)
                .featured(CATEGORY_FEATURED)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .translations(List.of(
                        CategoryTranslation.builder().locale("es").name(CATEGORY_NAME_ES).build(),
                        CategoryTranslation.builder().locale("en").name(CATEGORY_NAME_EN).build()))
                .build();
    }

    public static Category otherCategory() {
        return Category.builder()
                .id(OTHER_CATEGORY_ID)
                .parentId(CATEGORY_ID)
                .level(OTHER_CATEGORY_LEVEL)
                .name(OTHER_CATEGORY_NAME_ES)
                .status(CategoryStatus.DRAFT)
                .active(true)
                .featured(false)
                .createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .translations(List.of(
                        CategoryTranslation.builder().locale("es").name(OTHER_CATEGORY_NAME_ES).build()))
                .build();
    }

    public static CategoryEntity categoryEntity() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(CATEGORY_ID)
                .parentId(null)
                .level(CATEGORY_LEVEL)
                .status(CATEGORY_STATUS)
                .active(CATEGORY_ACTIVE)
                .featured(CATEGORY_FEATURED)
                .build();

        CategoryTranslationEntity translation = CategoryTranslationEntity.builder()
                .id(new CategoryTranslationId(CATEGORY_ID, "es"))
                .name(CATEGORY_NAME_ES)
                .category(entity)
                .build();
        entity.setTranslations(List.of(translation));
        return entity;
    }

    public static CategoryEntity otherCategoryEntity() {
        CategoryEntity entity = CategoryEntity.builder()
                .id(OTHER_CATEGORY_ID)
                .parentId(CATEGORY_ID)
                .level(OTHER_CATEGORY_LEVEL)
                .status(CategoryStatus.DRAFT)
                .active(true)
                .featured(false)
                .build();

        CategoryTranslationEntity translation = CategoryTranslationEntity.builder()
                .id(new CategoryTranslationId(OTHER_CATEGORY_ID, "es"))
                .name(OTHER_CATEGORY_NAME_ES)
                .category(entity)
                .build();
        entity.setTranslations(List.of(translation));
        return entity;
    }

    public static CategoryDtoIn categoryDtoIn() {
        return CategoryDtoIn.builder()
                .parentId(null)
                .level(CATEGORY_LEVEL)
                .translations(List.of(
                        CategoryTranslationDtoIn.builder().locale("es").name(CATEGORY_NAME_ES).build(),
                        CategoryTranslationDtoIn.builder().locale("en").name(CATEGORY_NAME_EN).build(),
                        CategoryTranslationDtoIn.builder().locale("pt-BR").name(CATEGORY_NAME_PT).build()))
                .build();
    }

    public static CategoryDtoIn otherCategoryDtoIn(String parentId) {
        return CategoryDtoIn.builder()
                .parentId(parentId)
                .level(OTHER_CATEGORY_LEVEL)
                .translations(List.of(
                        CategoryTranslationDtoIn.builder().locale("es").name(OTHER_CATEGORY_NAME_ES).build()))
                .build();
    }

    public static CategoryDtoOut categoryDtoOut(String id) {
        return CategoryDtoOut.builder()
                .id(id)
                .parentId(null)
                .level(CATEGORY_LEVEL)
                .status(CATEGORY_STATUS)
                .active(CATEGORY_ACTIVE)
                .featured(CATEGORY_FEATURED)
                .name(CATEGORY_NAME_ES)
                .translations(List.of(
                        CategoryTranslationDtoOut.builder().locale("es").name(CATEGORY_NAME_ES).build(),
                        CategoryTranslationDtoOut.builder().locale("en").name(CATEGORY_NAME_EN).build()))
                .subCategories(List.of())
                .build();
    }
}
