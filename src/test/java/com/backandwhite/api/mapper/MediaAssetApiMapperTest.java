package com.backandwhite.api.mapper;

import static com.backandwhite.provider.MediaAssetProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.out.MediaAssetDtoOut;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MediaAssetApiMapperTest {

    private final MediaAssetApiMapper mapper = Mappers.getMapper(MediaAssetApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        MediaAssetDtoOut dto = mapper.toDto(mediaAsset());
        assertThat(dto.getId()).isEqualTo(MEDIA_ID);
        assertThat(dto.getFilename()).isEqualTo(MEDIA_FILENAME);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        List<MediaAssetDtoOut> list = mapper.toDtoList(List.of(mediaAsset()));
        assertThat(list).hasSize(1);
    }
}
