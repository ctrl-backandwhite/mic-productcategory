package com.backandwhite.api.mapper;

import static com.backandwhite.provider.AttributeProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.in.AttributeDtoIn;
import com.backandwhite.api.dto.in.AttributeValueDtoIn;
import com.backandwhite.api.dto.out.AttributeDtoOut;
import com.backandwhite.api.dto.out.AttributeValueDtoOut;
import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.model.AttributeValue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AttributeApiMapperTest {

    private final AttributeApiMapper mapper = Mappers.getMapper(AttributeApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsAllFields() {
        AttributeDtoOut dto = mapper.toDto(attribute());
        assertThat(dto.getId()).isEqualTo(ATTRIBUTE_ID);
        assertThat(dto.getName()).isEqualTo(ATTRIBUTE_NAME);
        assertThat(dto.getType()).isEqualTo(ATTRIBUTE_TYPE);
        assertThat(dto.getValues()).hasSize(1);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        List<AttributeDtoOut> list = mapper.toDtoList(List.of(attribute()));
        assertThat(list).hasSize(1);
    }

    @Test
    void toValueDto_nullInput_returnsNull() {
        assertThat(mapper.toValueDto(null)).isNull();
    }

    @Test
    void toValueDto_mapsFields() {
        AttributeValue value = AttributeValue.builder().id(VALUE_ID).value(VALUE_TEXT).colorHex(VALUE_COLOR_HEX)
                .position(VALUE_POSITION).build();
        AttributeValueDtoOut dto = mapper.toValueDto(value);
        assertThat(dto.getId()).isEqualTo(VALUE_ID);
        assertThat(dto.getValue()).isEqualTo(VALUE_TEXT);
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        AttributeDtoIn dto = attributeDtoIn();
        Attribute domain = mapper.toDomain(dto);
        assertThat(domain.getName()).isEqualTo(ATTRIBUTE_NAME);
        assertThat(domain.getType()).isEqualTo(ATTRIBUTE_TYPE);
        assertThat(domain.getValues()).hasSize(1);
    }

    @Test
    void toValueDomain_nullInput_returnsNull() {
        assertThat(mapper.toValueDomain(null)).isNull();
    }

    @Test
    void toValueDomain_mapsFields() {
        AttributeValueDtoIn dto = AttributeValueDtoIn.builder().value(VALUE_TEXT).colorHex(VALUE_COLOR_HEX).build();
        AttributeValue domain = mapper.toValueDomain(dto);
        assertThat(domain.getValue()).isEqualTo(VALUE_TEXT);
        assertThat(domain.getColorHex()).isEqualTo(VALUE_COLOR_HEX);
    }
}
