package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.BrandProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.Brand;
import com.backandwhite.infrastructure.db.postgres.entity.BrandEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class BrandInfraMapperTest {

    private final BrandInfraMapper mapper = Mappers.getMapper(BrandInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Brand d = mapper.toDomain(brandEntity());
        assertThat(d.getId()).isEqualTo(BRAND_ID);
        assertThat(d.getName()).isEqualTo(BRAND_NAME);
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(brandEntity()))).hasSize(1);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        BrandEntity e = mapper.toEntity(brand());
        assertThat(e.getId()).isEqualTo(BRAND_ID);
    }
}
