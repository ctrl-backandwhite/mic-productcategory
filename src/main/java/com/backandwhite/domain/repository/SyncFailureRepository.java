package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.SyncFailure;

import java.util.List;
import java.util.Optional;

public interface SyncFailureRepository {

    SyncFailure save(SyncFailure failure);

    Optional<SyncFailure> findById(String id);

    List<SyncFailure> findUnresolvedByEntityType(String entityType, int limit);

    void markResolved(String id);
}
