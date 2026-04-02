package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.valureobject.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewRepository {

    Page<Review> findByProductId(String productId, Pageable pageable);

    Page<Review> findAll(ReviewStatus status, Integer rating, Pageable pageable);

    Optional<Review> findById(String reviewId);

    Review save(Review review);

    void delete(String reviewId);

    void updateStatus(String reviewId, ReviewStatus status);

    ReviewStats getStatsByProductId(String productId);

    boolean addHelpfulVote(String reviewId, String sessionId);

    void incrementHelpfulCount(String reviewId);
}
