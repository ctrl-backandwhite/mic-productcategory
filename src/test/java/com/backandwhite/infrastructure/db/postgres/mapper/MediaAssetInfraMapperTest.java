package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.MediaAssetProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.infrastructure.db.postgres.entity.MediaAssetEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MediaAssetInfraMapperTest {

    private final MediaAssetInfraMapper mapper = Mappers.getMapper(MediaAssetInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        MediaAsset d = mapper.toDomain(mediaAssetEntity());
        assertThat(d.getId()).isEqualTo(MEDIA_ID);
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(mediaAssetEntity()))).hasSize(1);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        MediaAssetEntity e = mapper.toEntity(mediaAsset());
        assertThat(e.getId()).isEqualTo(MEDIA_ID);
    }
}
