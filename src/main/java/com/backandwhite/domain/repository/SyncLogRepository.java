package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.valueobject.SyncType;

import java.util.List;
import java.util.Optional;

public interface SyncLogRepository {

    SyncLog save(SyncLog syncLog);

    Optional<SyncLog> findById(String id);

    Optional<SyncLog> findRunningByType(SyncType syncType);

    List<SyncLog> findRecentByType(SyncType syncType, int limit);
}
