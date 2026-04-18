package com.backandwhite.infrastructure.db.postgres.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.valueobject.DiscoveryStateStatus;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.db.postgres.entity.DiscoveredPidEntity;
import com.backandwhite.infrastructure.db.postgres.entity.DiscoveryStateEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class DiscoveryInfraMapperTest {

    private final DiscoveryInfraMapper mapper = Mappers.getMapper(DiscoveryInfraMapper.class);

    @Test
    void strategyToString_mapsValues() {
        assertThat(mapper.strategyToString(null)).isNull();
        assertThat(mapper.strategyToString(DiscoveryStrategy.BY_CATEGORY)).isEqualTo("BY_CATEGORY");
    }

    @Test
    void stringToStrategy_mapsValues() {
        assertThat(mapper.stringToStrategy(null)).isNull();
        assertThat(mapper.stringToStrategy("BY_CATEGORY")).isEqualTo(DiscoveryStrategy.BY_CATEGORY);
    }

    @Test
    void discoveryStatusToString_mapsValues() {
        assertThat(mapper.discoveryStatusToString(null)).isNull();
        assertThat(mapper.discoveryStatusToString(DiscoveryStatus.NEW)).isEqualTo("NEW");
    }

    @Test
    void stringToDiscoveryStatus_mapsValues() {
        assertThat(mapper.stringToDiscoveryStatus(null)).isNull();
        assertThat(mapper.stringToDiscoveryStatus("NEW")).isEqualTo(DiscoveryStatus.NEW);
    }

    @Test
    void stateStatusToString_mapsValues() {
        assertThat(mapper.stateStatusToString(null)).isNull();
        assertThat(mapper.stateStatusToString(DiscoveryStateStatus.IDLE)).isEqualTo("IDLE");
    }

    @Test
    void stringToStateStatus_mapsValues() {
        assertThat(mapper.stringToStateStatus(null)).isNull();
        assertThat(mapper.stringToStateStatus("IDLE")).isEqualTo(DiscoveryStateStatus.IDLE);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        DiscoveredPid d = DiscoveredPid.builder().pid("pid").strategy(DiscoveryStrategy.BY_CATEGORY)
                .status(DiscoveryStatus.NEW).build();
        DiscoveredPidEntity e = mapper.toEntity(d);
        assertThat(e.getStrategy()).isEqualTo("BY_CATEGORY");
        assertThat(e.getStatus()).isEqualTo("NEW");
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        DiscoveredPidEntity entity = DiscoveredPidEntity.builder().pid("pid").strategy("BY_CATEGORY").status("NEW")
                .build();
        DiscoveredPid d = mapper.toDomain(entity);
        assertThat(d.getStrategy()).isEqualTo(DiscoveryStrategy.BY_CATEGORY);
        assertThat(d.getStatus()).isEqualTo(DiscoveryStatus.NEW);
    }

    @Test
    void stateToEntity_nullInput_returnsNull() {
        assertThat(mapper.stateToEntity(null)).isNull();
    }

    @Test
    void stateToEntity_mapsFields() {
        DiscoveryState d = DiscoveryState.builder().strategy(DiscoveryStrategy.BY_CATEGORY)
                .status(DiscoveryStateStatus.IDLE).build();
        DiscoveryStateEntity e = mapper.stateToEntity(d);
        assertThat(e.getStrategy()).isEqualTo("BY_CATEGORY");
        assertThat(e.getStatus()).isEqualTo("IDLE");
    }

    @Test
    void stateToDomain_nullInput_returnsNull() {
        assertThat(mapper.stateToDomain(null)).isNull();
    }

    @Test
    void stateToDomain_mapsFields() {
        DiscoveryStateEntity entity = DiscoveryStateEntity.builder().strategy("BY_CATEGORY").status("IDLE").build();
        DiscoveryState d = mapper.stateToDomain(entity);
        assertThat(d.getStrategy()).isEqualTo(DiscoveryStrategy.BY_CATEGORY);
        assertThat(d.getStatus()).isEqualTo(DiscoveryStateStatus.IDLE);
    }
}
