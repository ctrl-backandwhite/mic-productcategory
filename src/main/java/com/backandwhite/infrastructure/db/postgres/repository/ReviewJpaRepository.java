package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, String>,
        JpaSpecificationExecutor<ReviewEntity> {

    @Modifying
    @Query("UPDATE ReviewEntity r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :reviewId")
    void incrementHelpfulCount(@Param("reviewId") String reviewId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ReviewEntity r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Double avgRatingByProductId(@Param("productId") String productId);

    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Long countApprovedByProductId(@Param("productId") String productId);

    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.productId = :productId AND r.status = 'APPROVED' AND r.rating = :rating")
    Long countByProductIdAndRating(@Param("productId") String productId, @Param("rating") int rating);
}
