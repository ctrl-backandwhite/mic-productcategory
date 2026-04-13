package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.DiscoveredPidEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface DiscoveredPidJpaRepository extends JpaRepository<DiscoveredPidEntity, String> {

    boolean existsByPid(String pid);

    List<DiscoveredPidEntity> findByStatusOrderByDiscoveredAtAsc(String status, Pageable pageable);

    Page<DiscoveredPidEntity> findByStatus(String status, Pageable pageable);

    Page<DiscoveredPidEntity> findByStatusAndStrategy(String status, String strategy, Pageable pageable);

    long countByStatus(String status);

    long countByStrategy(String strategy);

    @Transactional
    @Modifying
    @Query("UPDATE DiscoveredPidEntity d SET d.status = :status, d.updatedAt = :now WHERE d.id = :id")
    void updateStatus(@Param("id") String id, @Param("status") String status, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("UPDATE DiscoveredPidEntity d SET d.status = 'SYNCED', d.syncedAt = :now, d.updatedAt = :now WHERE d.id = :id")
    void markSynced(@Param("id") String id, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("UPDATE DiscoveredPidEntity d SET d.status = 'FAILED', d.errorCount = d.errorCount + 1, "
            + "d.lastError = :error, d.updatedAt = :now WHERE d.id = :id")
    void markFailed(@Param("id") String id, @Param("error") String error, @Param("now") Instant now);
}
