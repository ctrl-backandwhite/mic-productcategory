package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.CategoryProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CategoryInfraMapperTest {

    private final CategoryInfraMapper mapper = Mappers.getMapper(CategoryInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields_usesFirstTranslationName() {
        Category d = mapper.toDomain(categoryEntity());
        assertThat(d.getId()).isEqualTo(CATEGORY_ID);
        assertThat(d.getName()).isEqualTo(CATEGORY_NAME_ES);
    }

    @Test
    void toDomain_noTranslations_nameIsNull() {
        CategoryEntity entity = CategoryEntity.builder().id(CATEGORY_ID).level(1).build();
        entity.setTranslations(new ArrayList<>());
        Category d = mapper.toDomain(entity);
        assertThat(d.getName()).isNull();
    }

    @Test
    void getFirstTranslationName_nullList_returnsNull() {
        CategoryEntity entity = CategoryEntity.builder().id("id").build();
        entity.setTranslations(null);
        assertThat(mapper.getFirstTranslationName(entity)).isNull();
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(categoryEntity()))).hasSize(1);
    }

    @Test
    void toTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDomain(null)).isNull();
    }

    @Test
    void toTranslationDomain_mapsFields() {
        CategoryTranslationEntity entity = CategoryTranslationEntity.builder()
                .id(new CategoryTranslationId(CATEGORY_ID, "es")).name(CATEGORY_NAME_ES).build();
        CategoryTranslation t = mapper.toTranslationDomain(entity);
        assertThat(t.getLocale()).isEqualTo("es");
        assertThat(t.getName()).isEqualTo(CATEGORY_NAME_ES);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        CategoryEntity e = mapper.toEntity(category());
        assertThat(e.getId()).isEqualTo(CATEGORY_ID);
    }

    @Test
    void toTranslationEntity_mapsFields() {
        CategoryTranslation t = CategoryTranslation.builder().locale("es").name("X").build();
        CategoryTranslationEntity entity = mapper.toTranslationEntity(t, CATEGORY_ID);
        assertThat(entity.getId().getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(entity.getId().getLocale()).isEqualTo("es");
    }

    @Test
    void toEntityWithChildren_withTranslations_wiresChildren() {
        Category domain = category();
        CategoryEntity e = mapper.toEntityWithChildren(domain);
        assertThat(e.getTranslations()).hasSize(2);
        assertThat(e.getTranslations().getFirst().getCategory()).isSameAs(e);
    }

    @Test
    void toEntityWithChildren_nullTranslations_noChildren() {
        Category domain = Category.builder().id("id-1").level(1).translations(null).build();
        CategoryEntity e = mapper.toEntityWithChildren(domain);
        assertThat(e.getTranslations()).isEmpty();
    }
}
