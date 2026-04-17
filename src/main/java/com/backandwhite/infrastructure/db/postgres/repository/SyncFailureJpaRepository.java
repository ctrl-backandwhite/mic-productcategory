package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.SyncFailureEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SyncFailureJpaRepository extends JpaRepository<SyncFailureEntity, String> {

    @Query("SELECT f FROM SyncFailureEntity f WHERE f.entityType = :entityType AND f.resolved = false ORDER BY f.retryCount ASC LIMIT :limit")
    List<SyncFailureEntity> findUnresolvedByEntityType(@Param("entityType") String entityType,
            @Param("limit") int limit);

    @Transactional
    @Modifying
    @Query("UPDATE SyncFailureEntity f SET f.resolved = true WHERE f.id = :id")
    void markResolved(@Param("id") String id);
}
