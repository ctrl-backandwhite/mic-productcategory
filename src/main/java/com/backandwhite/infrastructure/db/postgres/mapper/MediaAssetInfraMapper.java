package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.infrastructure.db.postgres.entity.MediaAssetEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaAssetInfraMapper {

    MediaAsset toDomain(MediaAssetEntity entity);

    List<MediaAsset> toDomainList(List<MediaAssetEntity> entities);

    MediaAssetEntity toEntity(MediaAsset domain);
}
