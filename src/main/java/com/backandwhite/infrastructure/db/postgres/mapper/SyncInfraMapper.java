package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.infrastructure.db.postgres.entity.SyncFailureEntity;
import com.backandwhite.infrastructure.db.postgres.entity.SyncLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncInfraMapper {

    @Mapping(target = "syncType", expression = "java(entity.getSyncType() != null ? com.backandwhite.domain.valueobject.SyncType.valueOf(entity.getSyncType()) : null)")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? com.backandwhite.domain.valueobject.SyncStatus.valueOf(entity.getStatus()) : null)")
    SyncLog toDomain(SyncLogEntity entity);

    @Mapping(target = "syncType", expression = "java(domain.getSyncType() != null ? domain.getSyncType().name() : null)")
    @Mapping(target = "status", expression = "java(domain.getStatus() != null ? domain.getStatus().name() : null)")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SyncLogEntity toEntity(SyncLog domain);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SyncFailureEntity toEntity(SyncFailure domain);

    SyncFailure toDomain(SyncFailureEntity entity);
}
