package com.backandwhite.infrastructure.db.postgres.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.valueobject.SyncStatus;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.db.postgres.entity.SyncFailureEntity;
import com.backandwhite.infrastructure.db.postgres.entity.SyncLogEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class SyncInfraMapperTest {

    private final SyncInfraMapper mapper = Mappers.getMapper(SyncInfraMapper.class);

    @Test
    void syncLog_toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain((SyncLogEntity) null)).isNull();
    }

    @Test
    void syncLog_toDomain_mapsFields() {
        SyncLogEntity entity = SyncLogEntity.builder().id("1").syncType("PRODUCT_FULL").status("SUCCESS").build();
        SyncLog d = mapper.toDomain(entity);
        assertThat(d.getSyncType()).isEqualTo(SyncType.PRODUCT_FULL);
        assertThat(d.getStatus()).isEqualTo(SyncStatus.SUCCESS);
    }

    @Test
    void syncLog_toDomain_nullEnumsHandled() {
        SyncLogEntity entity = SyncLogEntity.builder().id("1").syncType(null).status(null).build();
        SyncLog d = mapper.toDomain(entity);
        assertThat(d.getSyncType()).isNull();
        assertThat(d.getStatus()).isNull();
    }

    @Test
    void syncLog_toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity((SyncLog) null)).isNull();
    }

    @Test
    void syncLog_toEntity_mapsFields() {
        SyncLog domain = SyncLog.builder().id("1").syncType(SyncType.PRODUCT_FULL).status(SyncStatus.SUCCESS).build();
        SyncLogEntity e = mapper.toEntity(domain);
        assertThat(e.getSyncType()).isEqualTo("PRODUCT_FULL");
        assertThat(e.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void syncLog_toEntity_nullEnumsHandled() {
        SyncLog domain = SyncLog.builder().id("1").syncType(null).status(null).build();
        SyncLogEntity e = mapper.toEntity(domain);
        assertThat(e.getSyncType()).isNull();
        assertThat(e.getStatus()).isNull();
    }

    @Test
    void syncFailure_toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity((SyncFailure) null)).isNull();
    }

    @Test
    void syncFailure_toEntity_mapsFields() {
        SyncFailure domain = SyncFailure.builder().id("1").entityType("Product").build();
        SyncFailureEntity e = mapper.toEntity(domain);
        assertThat(e.getEntityType()).isEqualTo("Product");
    }

    @Test
    void syncFailure_toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain((SyncFailureEntity) null)).isNull();
    }

    @Test
    void syncFailure_toDomain_mapsFields() {
        SyncFailureEntity entity = SyncFailureEntity.builder().id("1").entityType("Product").build();
        SyncFailure d = mapper.toDomain(entity);
        assertThat(d.getEntityType()).isEqualTo("Product");
    }
}
