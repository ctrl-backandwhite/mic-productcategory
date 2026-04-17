package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.CategoryDtoIn;
import com.backandwhite.api.dto.in.CategoryTranslationDtoIn;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.CategoryTranslationDtoOut;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryApiMapper {

    CategoryDtoOut toDto(Category category);

    List<CategoryDtoOut> toDtoList(List<Category> categories);

    CategoryTranslationDtoOut toTranslationDto(CategoryTranslation translation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    Category toDomain(CategoryDtoIn dto);

    CategoryTranslation toTranslationDomain(CategoryTranslationDtoIn dto);
}
