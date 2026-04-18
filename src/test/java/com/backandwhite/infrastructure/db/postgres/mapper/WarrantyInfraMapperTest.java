package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.WarrantyProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.Warranty;
import com.backandwhite.infrastructure.db.postgres.entity.WarrantyEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WarrantyInfraMapperTest {

    private final WarrantyInfraMapper mapper = Mappers.getMapper(WarrantyInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Warranty d = mapper.toDomain(warrantyEntity());
        assertThat(d.getId()).isEqualTo(WARRANTY_ID);
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(warrantyEntity()))).hasSize(1);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        WarrantyEntity e = mapper.toEntity(warranty());
        assertThat(e.getId()).isEqualTo(WARRANTY_ID);
    }
}
