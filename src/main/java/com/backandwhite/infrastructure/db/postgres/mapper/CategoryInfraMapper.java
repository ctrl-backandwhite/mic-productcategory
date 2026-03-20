package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryInfraMapper {

    // ── Domain ← Entity ────────────────────────────────────────────────────

    @Mapping(target = "name", expression = "java(getFirstTranslationName(entity))")
    @Mapping(target = "subCategories", ignore = true)
    Category toDomain(CategoryEntity entity);

    List<Category> toDomainList(List<CategoryEntity> entities);

    @Mapping(target = "locale", source = "id.locale")
    @Mapping(target = "name", source = "name")
    CategoryTranslation toTranslationDomain(CategoryTranslationEntity entity);

    default String getFirstTranslationName(CategoryEntity entity) {
        if (entity.getTranslations() == null || entity.getTranslations().isEmpty()) {
            return null;
        }
        return entity.getTranslations().getFirst().getName();
    }

    // ── Domain → Entity ────────────────────────────────────────────────────

    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CategoryEntity toEntity(Category domain);

    @Mapping(target = "id", expression = "java(new CategoryTranslationId(categoryId, t.getLocale()))")
    @Mapping(target = "category", ignore = true)
    CategoryTranslationEntity toTranslationEntity(CategoryTranslation t, @Context String categoryId);

    default CategoryEntity toEntityWithChildren(Category domain) {
        CategoryEntity entity = toEntity(domain);

        if (domain.getTranslations() != null) {
            for (CategoryTranslation t : domain.getTranslations()) {
                CategoryTranslationEntity te = toTranslationEntity(t, domain.getId());
                te.setCategory(entity);
                entity.getTranslations().add(te);
            }
        }

        return entity;
    }
}
