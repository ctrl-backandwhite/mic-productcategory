package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.PriceRuleProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.infrastructure.db.postgres.entity.PriceRuleEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PriceRuleInfraMapperTest {

    private final PriceRuleInfraMapper mapper = Mappers.getMapper(PriceRuleInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        PriceRule d = mapper.toDomain(priceRuleEntity());
        assertThat(d.getId()).isEqualTo(RULE_ID);
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(priceRuleEntity()))).hasSize(1);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        PriceRuleEntity e = mapper.toEntity(priceRule());
        assertThat(e.getId()).isEqualTo(RULE_ID);
    }
}
