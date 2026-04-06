package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.valueobject.ReviewStatus;
import org.springframework.data.domain.Page;

public interface ReviewUseCase {

    Page<Review> findByProductId(String productId, int page, int size, String sortBy, boolean ascending);

    ReviewStats getStatsByProductId(String productId);

    Review create(Review review);

    void voteHelpful(String reviewId, String sessionId);

    Page<Review> findAll(ReviewStatus status, Integer rating, int page, int size, String sortBy, boolean ascending);

    void moderate(String reviewId, ReviewStatus status);

    void delete(String reviewId);
}
