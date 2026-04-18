package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.AttributeProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.model.AttributeValue;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeEntity;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeValueEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AttributeInfraMapperTest {

    private final AttributeInfraMapper mapper = Mappers.getMapper(AttributeInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Attribute d = mapper.toDomain(attributeEntity());
        assertThat(d.getId()).isEqualTo(ATTRIBUTE_ID);
        assertThat(d.getName()).isEqualTo(ATTRIBUTE_NAME);
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(attributeEntity()))).hasSize(1);
    }

    @Test
    void toValueDomain_nullInput_returnsNull() {
        assertThat(mapper.toValueDomain(null)).isNull();
    }

    @Test
    void toValueDomain_mapsFields() {
        AttributeValueEntity entity = AttributeValueEntity.builder().id(VALUE_ID).attributeId(ATTRIBUTE_ID)
                .value(VALUE_TEXT).colorHex(VALUE_COLOR_HEX).position(VALUE_POSITION).build();
        AttributeValue v = mapper.toValueDomain(entity);
        assertThat(v.getValue()).isEqualTo(VALUE_TEXT);
        assertThat(v.getAttributeId()).isEqualTo(ATTRIBUTE_ID);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        AttributeEntity e = mapper.toEntity(attribute());
        assertThat(e.getId()).isEqualTo(ATTRIBUTE_ID);
    }

    @Test
    void toValueEntity_nullInput_returnsNull() {
        assertThat(mapper.toValueEntity(null)).isNull();
    }

    @Test
    void toValueEntity_mapsFields() {
        AttributeValue v = AttributeValue.builder().id(VALUE_ID).attributeId(ATTRIBUTE_ID).value(VALUE_TEXT)
                .colorHex(VALUE_COLOR_HEX).position(VALUE_POSITION).build();
        AttributeValueEntity e = mapper.toValueEntity(v);
        assertThat(e.getValue()).isEqualTo(VALUE_TEXT);
    }

    @Test
    void toEntityWithValues_withValues_setsRelation() {
        Attribute a = attribute();
        AttributeEntity e = mapper.toEntityWithValues(a);
        assertThat(e.getValues()).hasSize(1);
        assertThat(e.getValues().getFirst().getAttribute()).isSameAs(e);
    }

    @Test
    void toEntityWithValues_nullValues_returnsEmpty() {
        Attribute a = Attribute.builder().id(ATTRIBUTE_ID).name(ATTRIBUTE_NAME).slug(ATTRIBUTE_SLUG)
                .type(ATTRIBUTE_TYPE).values(null).build();
        AttributeEntity e = mapper.toEntityWithValues(a);
        assertThat(e.getValues()).isEmpty();
    }
}
