package com.backandwhite.api.controller;

import static com.backandwhite.provider.ReviewProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.ReviewDtoIn;
import com.backandwhite.api.dto.in.ReviewHelpfulDtoIn;
import com.backandwhite.api.dto.in.ReviewModerateDtoIn;
import com.backandwhite.api.dto.out.ReviewDtoOut;
import com.backandwhite.api.dto.out.ReviewStatsDtoOut;
import com.backandwhite.api.mapper.ReviewApiMapper;
import com.backandwhite.application.usecase.ReviewUseCase;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.valueobject.ReviewStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewUseCase reviewUseCase;

    @Mock
    private ReviewApiMapper reviewApiMapper;

    @InjectMocks
    private ReviewController controller;

    @Test
    void findByProductId_returnsPaginatedReviews() {
        Page<Review> page = new PageImpl<>(List.of(review()));
        when(reviewUseCase.findByProductId(REVIEW_PRODUCT_ID, 0, 10, "createdAt", false)).thenReturn(page);
        when(reviewApiMapper.toDto(any(Review.class))).thenReturn(reviewDtoOut());

        ResponseEntity<PaginationDtoOut<ReviewDtoOut>> response = controller.findByProductId(REVIEW_PRODUCT_ID, 0, 10,
                "createdAt", false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(reviewUseCase).findByProductId(REVIEW_PRODUCT_ID, 0, 10, "createdAt", false);
    }

    @Test
    void getStatsByProductId_returnsStats() {
        ReviewStats stats = reviewStats();
        ReviewStatsDtoOut statsDto = reviewStatsDtoOut();

        when(reviewUseCase.getStatsByProductId(REVIEW_PRODUCT_ID)).thenReturn(stats);
        when(reviewApiMapper.toStatsDto(stats)).thenReturn(statsDto);

        ResponseEntity<ReviewStatsDtoOut> response = controller.getStatsByProductId(REVIEW_PRODUCT_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(statsDto);
        verify(reviewUseCase).getStatsByProductId(REVIEW_PRODUCT_ID);
        verify(reviewApiMapper).toStatsDto(stats);
    }

    @Test
    void create_returnsCreatedReview() {
        ReviewDtoIn dtoIn = reviewDtoIn();
        Review model = review();
        ReviewDtoOut dtoOut = reviewDtoOut();

        when(reviewApiMapper.toDomain(dtoIn)).thenReturn(model);
        when(reviewUseCase.create(model)).thenReturn(model);
        when(reviewApiMapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<ReviewDtoOut> response = controller.create(REVIEW_PRODUCT_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(reviewApiMapper).toDomain(dtoIn);
        verify(reviewUseCase).create(model);
        verify(reviewApiMapper).toDto(model);
    }

    @Test
    void voteHelpful_returnsNoContent() {
        ReviewHelpfulDtoIn dto = ReviewHelpfulDtoIn.builder().sessionId("session-123").build();

        ResponseEntity<Void> response = controller.voteHelpful(REVIEW_ID, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reviewUseCase).voteHelpful(REVIEW_ID, "session-123");
    }

    @Test
    void findAll_returnsPaginatedAdminReviews() {
        Page<Review> page = new PageImpl<>(List.of(review()));
        when(reviewUseCase.findAll(null, null, 0, 20, "createdAt", false)).thenReturn(page);
        when(reviewApiMapper.toDto(any(Review.class))).thenReturn(reviewDtoOut());

        ResponseEntity<PaginationDtoOut<ReviewDtoOut>> response = controller.findAll(null, null, 0, 20, "createdAt",
                false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(reviewUseCase).findAll(null, null, 0, 20, "createdAt", false);
    }

    @Test
    void moderate_returnsNoContent() {
        ReviewModerateDtoIn dto = ReviewModerateDtoIn.builder().status(ReviewStatus.APPROVED).build();

        ResponseEntity<Void> response = controller.moderate(REVIEW_ID, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reviewUseCase).moderate(REVIEW_ID, ReviewStatus.APPROVED);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(REVIEW_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reviewUseCase).delete(REVIEW_ID);
    }
}
