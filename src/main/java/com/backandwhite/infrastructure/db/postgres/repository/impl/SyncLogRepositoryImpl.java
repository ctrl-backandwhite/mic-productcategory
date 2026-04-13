package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.db.postgres.entity.SyncLogEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.SyncInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.SyncLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SyncLogRepositoryImpl implements SyncLogRepository {

    private final SyncLogJpaRepository syncLogJpaRepository;
    private final SyncInfraMapper syncInfraMapper;

    @Override
    public SyncLog save(SyncLog syncLog) {
        if (syncLog.getId() == null) {
            syncLog = syncLog.toBuilder().id(UUID.randomUUID().toString()).build();
        }
        SyncLogEntity entity = syncInfraMapper.toEntity(syncLog);
        return syncInfraMapper.toDomain(syncLogJpaRepository.save(entity));
    }

    @Override
    public Optional<SyncLog> findById(String id) {
        return syncLogJpaRepository.findById(id).map(syncInfraMapper::toDomain);
    }

    @Override
    public Optional<SyncLog> findRunningByType(SyncType syncType) {
        return syncLogJpaRepository.findRunningByType(syncType.name()).map(syncInfraMapper::toDomain);
    }

    @Override
    public List<SyncLog> findRecentByType(SyncType syncType, int limit) {
        return syncLogJpaRepository.findRecentByType(syncType.name(), limit).stream()
                .map(syncInfraMapper::toDomain)
                .toList();
    }
}
