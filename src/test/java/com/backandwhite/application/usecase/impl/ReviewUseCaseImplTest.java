package com.backandwhite.application.usecase.impl;

import static com.backandwhite.provider.ReviewProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.repository.ReviewRepository;
import com.backandwhite.domain.valueobject.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReviewUseCaseImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewUseCaseImpl reviewUseCase;

    @Test
    void findByProductId_returnsPage() {
        Page<Review> page = new PageImpl<>(List.of(review()));
        when(reviewRepository.findByProductId(any(), any(Pageable.class))).thenReturn(page);

        Page<Review> result = reviewUseCase.findByProductId(REVIEW_PRODUCT_ID, 0, 10, "createdAt", false);

        assertThat(result.getContent()).hasSize(1);
        verify(reviewRepository).findByProductId(any(), any(Pageable.class));
    }

    @Test
    void getStatsByProductId_returnsStats() {
        ReviewStats stats = reviewStats();
        when(reviewRepository.getStatsByProductId(REVIEW_PRODUCT_ID)).thenReturn(stats);

        ReviewStats result = reviewUseCase.getStatsByProductId(REVIEW_PRODUCT_ID);

        assertSame(stats, result);
        verify(reviewRepository).getStatsByProductId(REVIEW_PRODUCT_ID);
    }

    @Test
    void create_delegatesToRepository() {
        Review model = review();
        when(reviewRepository.save(model)).thenReturn(model);

        Review result = reviewUseCase.create(model);

        assertSame(model, result);
        verify(reviewRepository).save(model);
    }

    @Test
    void voteHelpful_newVote_incrementsCount() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(reviewRepository.addHelpfulVote(REVIEW_ID, "session-1")).thenReturn(true);

        reviewUseCase.voteHelpful(REVIEW_ID, "session-1");

        verify(reviewRepository).addHelpfulVote(REVIEW_ID, "session-1");
        verify(reviewRepository).incrementHelpfulCount(REVIEW_ID);
    }

    @Test
    void voteHelpful_duplicateVote_doesNotIncrement() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(reviewRepository.addHelpfulVote(REVIEW_ID, "session-1")).thenReturn(false);

        reviewUseCase.voteHelpful(REVIEW_ID, "session-1");

        verify(reviewRepository).addHelpfulVote(REVIEW_ID, "session-1");
        verify(reviewRepository, never()).incrementHelpfulCount(REVIEW_ID);
    }

    @Test
    void voteHelpful_missingReview_throwsEntityNotFoundException() {
        when(reviewRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewUseCase.voteHelpful("non-existent", "session-1"));
    }

    @Test
    void findAll_returnsFilteredPage() {
        Page<Review> page = new PageImpl<>(List.of(review()));
        when(reviewRepository.findAll(any(), any(), any(Pageable.class))).thenReturn(page);

        Page<Review> result = reviewUseCase.findAll(ReviewStatus.APPROVED, null, 0, 20, "createdAt", false);

        assertThat(result.getContent()).hasSize(1);
        verify(reviewRepository).findAll(any(), any(), any(Pageable.class));
    }

    @Test
    void moderate_updatesStatus() {
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review()));

        reviewUseCase.moderate(REVIEW_ID, ReviewStatus.REJECTED);

        verify(reviewRepository).updateStatus(REVIEW_ID, ReviewStatus.REJECTED);
    }

    @Test
    void moderate_missingReview_throwsEntityNotFoundException() {
        when(reviewRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> reviewUseCase.moderate("non-existent", ReviewStatus.APPROVED));
    }

    @Test
    void delete_delegatesToRepository() {
        reviewUseCase.delete(REVIEW_ID);

        verify(reviewRepository).delete(REVIEW_ID);
    }
}
