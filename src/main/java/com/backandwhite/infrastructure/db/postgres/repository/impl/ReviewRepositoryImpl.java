package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.repository.ReviewRepository;
import com.backandwhite.domain.valueobject.ReviewStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ReviewEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ReviewHelpfulVoteEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.ReviewInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.ReviewHelpfulVoteJpaRepository;
import com.backandwhite.infrastructure.db.postgres.repository.ReviewJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.ReviewSpecification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewHelpfulVoteJpaRepository helpfulVoteJpaRepository;
    private final ReviewInfraMapper reviewInfraMapper;

    @Override
    public Page<Review> findByProductId(String productId, Pageable pageable) {
        return reviewJpaRepository.findAll(ReviewSpecification.approvedByProductId(productId), pageable)
                .map(reviewInfraMapper::toDomain);
    }

    @Override
    public Page<Review> findAll(ReviewStatus status, Integer rating, Pageable pageable) {
        return reviewJpaRepository.findAll(ReviewSpecification.withAdminFilters(status, rating), pageable)
                .map(reviewInfraMapper::toDomain);
    }

    @Override
    public Optional<Review> findById(String reviewId) {
        return reviewJpaRepository.findById(reviewId).map(reviewInfraMapper::toDomain);
    }

    @Override
    public Review save(Review review) {
        String newId = UUID.randomUUID().toString();
        review.setId(newId);
        review.setStatus(ReviewStatus.PENDING);
        review.setHelpfulCount(0);

        reviewJpaRepository.save(reviewInfraMapper.toEntity(review));

        return findById(newId).orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Review", newId));
    }

    @Override
    public void delete(String reviewId) {
        reviewJpaRepository.delete(findOrThrow(reviewId));
    }

    @Override
    public void updateStatus(String reviewId, ReviewStatus status) {
        ReviewEntity entity = findOrThrow(reviewId);
        entity.setStatus(status);
        reviewJpaRepository.save(entity);
    }

    @Override
    public ReviewStats getStatsByProductId(String productId) {
        return ReviewStats.builder().avgRating(reviewJpaRepository.avgRatingByProductId(productId))
                .totalCount(reviewJpaRepository.countApprovedByProductId(productId))
                .count1(reviewJpaRepository.countByProductIdAndRating(productId, 1))
                .count2(reviewJpaRepository.countByProductIdAndRating(productId, 2))
                .count3(reviewJpaRepository.countByProductIdAndRating(productId, 3))
                .count4(reviewJpaRepository.countByProductIdAndRating(productId, 4))
                .count5(reviewJpaRepository.countByProductIdAndRating(productId, 5)).build();
    }

    @Override
    public boolean addHelpfulVote(String reviewId, String sessionId) {
        if (helpfulVoteJpaRepository.existsByReviewIdAndSessionId(reviewId, sessionId)) {
            return false;
        }
        helpfulVoteJpaRepository.save(ReviewHelpfulVoteEntity.builder().id(UUID.randomUUID().toString())
                .reviewId(reviewId).sessionId(sessionId).build());
        return true;
    }

    @Override
    public void incrementHelpfulCount(String reviewId) {
        reviewJpaRepository.incrementHelpfulCount(reviewId);
    }

    @Override
    public void saveAll(List<Review> reviews) {
        List<ReviewEntity> entities = reviews.stream().map(reviewInfraMapper::toEntity).toList();
        reviewJpaRepository.saveAll(entities);
    }

    @Override
    public boolean existsByExternalReviewId(String externalReviewId) {
        return reviewJpaRepository.existsByExternalReviewId(externalReviewId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ReviewEntity findOrThrow(String id) {
        return reviewJpaRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Review", id));
    }
}
