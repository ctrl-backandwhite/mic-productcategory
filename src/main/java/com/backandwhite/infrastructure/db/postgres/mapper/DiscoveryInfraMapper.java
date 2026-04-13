package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.valueobject.DiscoveryStateStatus;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.db.postgres.entity.DiscoveredPidEntity;
import com.backandwhite.infrastructure.db.postgres.entity.DiscoveryStateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DiscoveryInfraMapper {

    @Mapping(target = "strategy", source = "strategy", qualifiedByName = "strategyToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "discoveryStatusToString")
    DiscoveredPidEntity toEntity(DiscoveredPid domain);

    @Mapping(target = "strategy", source = "strategy", qualifiedByName = "stringToStrategy")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToDiscoveryStatus")
    DiscoveredPid toDomain(DiscoveredPidEntity entity);

    @Mapping(target = "strategy", source = "strategy", qualifiedByName = "strategyToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "stateStatusToString")
    DiscoveryStateEntity stateToEntity(DiscoveryState domain);

    @Mapping(target = "strategy", source = "strategy", qualifiedByName = "stringToStrategy")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStateStatus")
    DiscoveryState stateToDomain(DiscoveryStateEntity entity);

    @Named("strategyToString")
    default String strategyToString(DiscoveryStrategy strategy) {
        return strategy != null ? strategy.name() : null;
    }

    @Named("stringToStrategy")
    default DiscoveryStrategy stringToStrategy(String strategy) {
        return strategy != null ? DiscoveryStrategy.valueOf(strategy) : null;
    }

    @Named("discoveryStatusToString")
    default String discoveryStatusToString(DiscoveryStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToDiscoveryStatus")
    default DiscoveryStatus stringToDiscoveryStatus(String status) {
        return status != null ? DiscoveryStatus.valueOf(status) : null;
    }

    @Named("stateStatusToString")
    default String stateStatusToString(DiscoveryStateStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStateStatus")
    default DiscoveryStateStatus stringToStateStatus(String status) {
        return status != null ? DiscoveryStateStatus.valueOf(status) : null;
    }
}
