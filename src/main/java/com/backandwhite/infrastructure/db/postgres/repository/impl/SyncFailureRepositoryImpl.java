package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.infrastructure.db.postgres.mapper.SyncInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.SyncFailureJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SyncFailureRepositoryImpl implements SyncFailureRepository {

    private final SyncFailureJpaRepository syncFailureJpaRepository;
    private final SyncInfraMapper syncInfraMapper;

    @Override
    public SyncFailure save(SyncFailure failure) {
        if (failure.getId() == null) {
            failure = failure.toBuilder().id(UUID.randomUUID().toString()).build();
        }
        return syncInfraMapper.toDomain(syncFailureJpaRepository.save(syncInfraMapper.toEntity(failure)));
    }

    @Override
    public Optional<SyncFailure> findById(String id) {
        return syncFailureJpaRepository.findById(id).map(syncInfraMapper::toDomain);
    }

    @Override
    public List<SyncFailure> findUnresolvedByEntityType(String entityType, int limit) {
        return syncFailureJpaRepository.findUnresolvedByEntityType(entityType, limit).stream()
                .map(syncInfraMapper::toDomain).toList();
    }

    @Override
    public void markResolved(String id) {
        syncFailureJpaRepository.markResolved(id);
    }
}
