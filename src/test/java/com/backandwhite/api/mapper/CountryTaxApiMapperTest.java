package com.backandwhite.api.mapper;

import static com.backandwhite.provider.CountryTaxProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.in.CountryTaxDtoIn;
import com.backandwhite.api.dto.out.CountryTaxDtoOut;
import com.backandwhite.domain.model.CountryTax;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class CountryTaxApiMapperTest {

    private final CountryTaxApiMapper mapper = Mappers.getMapper(CountryTaxApiMapper.class);

    @Test
    void toDto_mapsFields() {
        CountryTaxDtoOut dto = mapper.toDto(countryTax());
        assertThat(dto.getId()).isEqualTo(TAX_ID);
        assertThat(dto.getCountry()).isEqualTo(TAX_COUNTRY_CODE);
        assertThat(dto.getAppliesToCategories()).containsExactly(TAX_APPLIES_TO);
        assertThat(dto.getType()).isEqualTo(TAX_TYPE.name());
    }

    @Test
    void toDtoList_mapsList() {
        List<CountryTaxDtoOut> list = mapper.toDtoList(List.of(countryTax()));
        assertThat(list).hasSize(1);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        CountryTaxDtoIn dto = countryTaxDtoIn();
        CountryTax domain = mapper.toDomain(dto);
        assertThat(domain.getCountryCode()).isEqualTo(TAX_COUNTRY_CODE);
        assertThat(domain.getAppliesTo()).isEqualTo(TAX_APPLIES_TO);
        assertThat(domain.getIncludesShipping()).isTrue();
    }

    @Test
    void stringToList_blankInput_returnsEmpty() {
        assertThat(mapper.stringToList("")).isEmpty();
        assertThat(mapper.stringToList(null)).isEmpty();
        assertThat(mapper.stringToList("   ")).isEmpty();
    }

    @Test
    void stringToList_splitsAndTrims() {
        assertThat(mapper.stringToList("a, b, c")).containsExactly("a", "b", "c");
    }

    @Test
    void listToString_nullOrEmpty_returnsNull() {
        assertThat(mapper.listToString(null)).isNull();
        assertThat(mapper.listToString(Collections.emptyList())).isNull();
    }

    @Test
    void listToString_joins() {
        assertThat(mapper.listToString(List.of("a", "b"))).isEqualTo("a, b");
    }
}
