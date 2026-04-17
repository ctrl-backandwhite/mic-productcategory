package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProductDetailJpaRepository extends JpaRepository<ProductDetailEntity, String> {

    @Query("SELECT p.pid FROM ProductDetailEntity p WHERE p.inventorySyncedAt IS NULL OR p.inventorySyncedAt < :before ORDER BY p.inventorySyncedAt ASC NULLS FIRST LIMIT :limit")
    List<String> findPidsNeedingInventorySync(@Param("before") Instant before, @Param("limit") int limit);

    @Query("SELECT p.pid FROM ProductDetailEntity p WHERE p.productSyncedAt IS NULL OR p.productSyncedAt < :before ORDER BY p.productSyncedAt ASC NULLS FIRST LIMIT :limit")
    List<String> findPidsNeedingProductSync(@Param("before") Instant before, @Param("limit") int limit);

    @Query("SELECT p.pid FROM ProductDetailEntity p WHERE p.reviewsSyncedAt IS NULL OR p.reviewsSyncedAt < :before ORDER BY p.reviewsSyncedAt ASC NULLS FIRST LIMIT :limit")
    List<String> findPidsNeedingReviewsSync(@Param("before") Instant before, @Param("limit") int limit);

    @Transactional
    @Modifying
    @Query("UPDATE ProductDetailEntity p SET p.inventorySyncedAt = :now WHERE p.pid = :pid")
    void markInventorySynced(@Param("pid") String pid, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("UPDATE ProductDetailEntity p SET p.productSyncedAt = :now WHERE p.pid = :pid")
    void markProductSynced(@Param("pid") String pid, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("UPDATE ProductDetailEntity p SET p.reviewsSyncedAt = :now WHERE p.pid = :pid")
    void markReviewsSynced(@Param("pid") String pid, @Param("now") Instant now);
}
