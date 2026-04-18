package com.backandwhite.api.mapper;

import static com.backandwhite.provider.PriceRuleProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.out.PriceRuleDtoOut;
import com.backandwhite.domain.model.PriceRule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PriceRuleApiMapperTest {

    private final PriceRuleApiMapper mapper = Mappers.getMapper(PriceRuleApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        PriceRuleDtoOut dto = mapper.toDto(priceRule());
        assertThat(dto.getId()).isEqualTo(RULE_ID);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        List<PriceRuleDtoOut> list = mapper.toDtoList(List.of(priceRule()));
        assertThat(list).hasSize(1);
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        PriceRule domain = mapper.toDomain(priceRuleDtoIn());
        assertThat(domain.getMarginType()).isEqualTo(RULE_MARGIN_TYPE);
        assertThat(domain.getMarginValue()).isEqualTo(RULE_MARGIN_VALUE);
    }
}
