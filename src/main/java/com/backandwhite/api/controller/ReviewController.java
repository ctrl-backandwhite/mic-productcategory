package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.ReviewDtoIn;
import com.backandwhite.api.dto.in.ReviewHelpfulDtoIn;
import com.backandwhite.api.dto.in.ReviewModerateDtoIn;
import com.backandwhite.api.dto.out.ReviewDtoOut;
import com.backandwhite.api.dto.out.ReviewStatsDtoOut;
import com.backandwhite.api.mapper.ReviewApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.ReviewUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxUser;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.valueobject.ReviewStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Endpoints for product review management")
public class ReviewController {

    private final ReviewUseCase reviewUseCase;
    private final ReviewApiMapper reviewApiMapper;

    // ── Public ──────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping("/product/{productId}")
    @Operation(summary = "List approved reviews for a product", description = "Returns paginated approved reviews for a given product")
    public ResponseEntity<PaginationDtoOut<ReviewDtoOut>> findByProductId(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Ascending order", example = "false") @RequestParam(defaultValue = "false") boolean ascending) {
        Page<Review> result = reviewUseCase.findByProductId(productId, page, size, sortBy, ascending);
        return ResponseEntity.ok(PageableUtils.toResponse(result.map(reviewApiMapper::toDto)));
    }

    @NxUser
    @GetMapping("/product/{productId}/stats")
    @Operation(summary = "Get review statistics", description = "Returns average rating and star distribution")
    public ResponseEntity<ReviewStatsDtoOut> getStatsByProductId(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Product ID") @PathVariable String productId) {
        return ResponseEntity.ok(reviewApiMapper.toStatsDto(reviewUseCase.getStatsByProductId(productId)));
    }

    @NxUser
    @PostMapping("/product/{productId}")
    @Operation(summary = "Create review", description = "Creates a new review for a product. Stays in PENDING status until moderated")
    public ResponseEntity<ReviewDtoOut> create(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Valid @RequestBody ReviewDtoIn dto) {
        Review review = reviewApiMapper.toDomain(dto);
        review.setProductId(productId);
        Review created = reviewUseCase.create(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewApiMapper.toDto(created));
    }

    @NxUser
    @PostMapping("/{id}/helpful")
    @Operation(summary = "Vote review as helpful", description = "Registers a helpfulness vote (idempotent by sessionId)")
    public ResponseEntity<Void> voteHelpful(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Review ID") @PathVariable String id, @Valid @RequestBody ReviewHelpfulDtoIn dto) {
        reviewUseCase.voteHelpful(id, dto.getSessionId());
        return ResponseEntity.noContent().build();
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @NxAdmin
    @GetMapping("/admin")
    @Operation(summary = "List all reviews (admin)", description = "Paginated listing with status and rating filters")
    public ResponseEntity<PaginationDtoOut<ReviewDtoOut>> findAll(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Filter by status (PENDING, APPROVED, REJECTED)") @RequestParam(required = false) ReviewStatus status,
            @Parameter(description = "Filter by rating (1-5)") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Ascending order", example = "false") @RequestParam(defaultValue = "false") boolean ascending) {
        Page<Review> result = reviewUseCase.findAll(status, rating, page, size, sortBy, ascending);
        return ResponseEntity.ok(PageableUtils.toResponse(result.map(reviewApiMapper::toDto)));
    }

    @NxAdmin
    @PatchMapping("/{id}/moderate")
    @Operation(summary = "Moderate review", description = "Changes the review status to APPROVED or REJECTED")
    public ResponseEntity<Void> moderate(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Review ID") @PathVariable String id,
            @Valid @RequestBody ReviewModerateDtoIn dto) {
        reviewUseCase.moderate(id, dto.getStatus());
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review")
    public ResponseEntity<Void> delete(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Review ID") @PathVariable String id) {
        reviewUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
