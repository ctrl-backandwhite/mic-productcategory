package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.ReviewUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.repository.ReviewRepository;
import com.backandwhite.domain.valureobject.ReviewStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReviewUseCaseImpl implements ReviewUseCase {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByProductId(String productId, int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reviewRepository.findByProductId(productId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStats getStatsByProductId(String productId) {
        return reviewRepository.getStatsByProductId(productId);
    }

    @Override
    @Transactional
    public Review create(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void voteHelpful(String reviewId, String sessionId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Review", reviewId));

        boolean added = reviewRepository.addHelpfulVote(reviewId, sessionId);
        if (added) {
            reviewRepository.incrementHelpfulCount(reviewId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findAll(ReviewStatus status, Integer rating, int page, int size, String sortBy,
            boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reviewRepository.findAll(status, rating, pageable);
    }

    @Override
    @Transactional
    public void moderate(String reviewId, ReviewStatus status) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Review", reviewId));
        reviewRepository.updateStatus(reviewId, status);
    }

    @Override
    @Transactional
    public void delete(String reviewId) {
        reviewRepository.delete(reviewId);
    }
}
