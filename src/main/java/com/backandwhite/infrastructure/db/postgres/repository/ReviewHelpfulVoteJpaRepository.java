package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.ReviewHelpfulVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewHelpfulVoteJpaRepository extends JpaRepository<ReviewHelpfulVoteEntity, String> {

    boolean existsByReviewIdAndSessionId(String reviewId, String sessionId);
}
