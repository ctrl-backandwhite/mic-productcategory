package com.backandwhite.api.mapper;

import static com.backandwhite.provider.WarrantyProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.out.WarrantyDtoOut;
import com.backandwhite.domain.model.Warranty;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WarrantyApiMapperTest {

    private final WarrantyApiMapper mapper = Mappers.getMapper(WarrantyApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        WarrantyDtoOut dto = mapper.toDto(warranty());
        assertThat(dto.getId()).isEqualTo(WARRANTY_ID);
        assertThat(dto.getName()).isEqualTo(WARRANTY_NAME);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        List<WarrantyDtoOut> list = mapper.toDtoList(List.of(warranty()));
        assertThat(list).hasSize(1);
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Warranty domain = mapper.toDomain(warrantyDtoIn());
        assertThat(domain.getName()).isEqualTo(WARRANTY_NAME);
        assertThat(domain.getType()).isEqualTo(WARRANTY_TYPE);
    }
}
