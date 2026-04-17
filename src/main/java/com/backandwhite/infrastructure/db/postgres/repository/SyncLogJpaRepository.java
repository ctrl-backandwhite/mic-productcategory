package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.SyncLogEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SyncLogJpaRepository extends JpaRepository<SyncLogEntity, String> {

    @Query("SELECT s FROM SyncLogEntity s WHERE s.syncType = :syncType AND s.status = 'RUNNING' ORDER BY s.startedAt DESC")
    Optional<SyncLogEntity> findRunningByType(@Param("syncType") String syncType);

    @Query("SELECT s FROM SyncLogEntity s WHERE s.syncType = :syncType ORDER BY s.startedAt DESC LIMIT :limit")
    List<SyncLogEntity> findRecentByType(@Param("syncType") String syncType, @Param("limit") int limit);
}
