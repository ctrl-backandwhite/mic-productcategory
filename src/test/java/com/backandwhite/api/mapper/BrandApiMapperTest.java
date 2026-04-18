package com.backandwhite.api.mapper;

import static com.backandwhite.provider.BrandProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.out.BrandDtoOut;
import com.backandwhite.domain.model.Brand;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class BrandApiMapperTest {

    private final BrandApiMapper mapper = Mappers.getMapper(BrandApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        BrandDtoOut dto = mapper.toDto(brand());
        assertThat(dto.getId()).isEqualTo(BRAND_ID);
        assertThat(dto.getName()).isEqualTo(BRAND_NAME);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        List<BrandDtoOut> list = mapper.toDtoList(List.of(brand()));
        assertThat(list).hasSize(1);
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Brand domain = mapper.toDomain(brandDtoIn());
        assertThat(domain.getName()).isEqualTo(BRAND_NAME);
        assertThat(domain.getSlug()).isEqualTo(BRAND_SLUG);
    }
}
