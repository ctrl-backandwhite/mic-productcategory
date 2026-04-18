package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.CountryTaxProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.infrastructure.db.postgres.entity.CountryTaxEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CountryTaxInfraMapperTest {

    private final CountryTaxInfraMapper mapper = Mappers.getMapper(CountryTaxInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        CountryTax d = mapper.toDomain(countryTaxEntity());
        assertThat(d.getId()).isEqualTo(TAX_ID);
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(countryTaxEntity()))).hasSize(1);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        CountryTaxEntity e = mapper.toEntity(countryTax());
        assertThat(e.getId()).isEqualTo(TAX_ID);
    }
}
