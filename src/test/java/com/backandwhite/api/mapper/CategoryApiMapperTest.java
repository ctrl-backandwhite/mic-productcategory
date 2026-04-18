package com.backandwhite.api.mapper;

import static com.backandwhite.provider.CategoryProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.in.CategoryTranslationDtoIn;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.CategoryTranslationDtoOut;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CategoryApiMapperTest {

    private final CategoryApiMapper mapper = Mappers.getMapper(CategoryApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        CategoryDtoOut dto = mapper.toDto(category());
        assertThat(dto.getId()).isEqualTo(CATEGORY_ID);
        assertThat(dto.getLevel()).isEqualTo(CATEGORY_LEVEL);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        List<CategoryDtoOut> list = mapper.toDtoList(List.of(category()));
        assertThat(list).hasSize(1);
    }

    @Test
    void toTranslationDto_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDto(null)).isNull();
    }

    @Test
    void toTranslationDto_mapsFields() {
        CategoryTranslation t = CategoryTranslation.builder().locale("es").name("Electrónica").build();
        CategoryTranslationDtoOut dto = mapper.toTranslationDto(t);
        assertThat(dto.getLocale()).isEqualTo("es");
        assertThat(dto.getName()).isEqualTo("Electrónica");
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Category domain = mapper.toDomain(categoryDtoIn());
        assertThat(domain.getLevel()).isEqualTo(CATEGORY_LEVEL);
        assertThat(domain.getTranslations()).hasSize(3);
    }

    @Test
    void toTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDomain(null)).isNull();
    }

    @Test
    void toTranslationDomain_mapsFields() {
        CategoryTranslationDtoIn dto = CategoryTranslationDtoIn.builder().locale("en").name("Electronics").build();
        CategoryTranslation domain = mapper.toTranslationDomain(dto);
        assertThat(domain.getLocale()).isEqualTo("en");
    }
}
