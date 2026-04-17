package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.out.MediaAssetDtoOut;
import com.backandwhite.domain.model.MediaAsset;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaAssetApiMapper {

    MediaAssetDtoOut toDto(MediaAsset mediaAsset);

    List<MediaAssetDtoOut> toDtoList(List<MediaAsset> assets);
}
