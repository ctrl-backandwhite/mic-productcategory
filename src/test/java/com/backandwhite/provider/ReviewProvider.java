package com.backandwhite.provider;

import com.backandwhite.api.dto.in.ReviewDtoIn;
import com.backandwhite.api.dto.out.ReviewDtoOut;
import com.backandwhite.api.dto.out.ReviewStatsDtoOut;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import com.backandwhite.domain.valueobject.ReviewStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ReviewEntity;
import java.util.List;

public final class ReviewProvider {

    public static final String REVIEW_ID = "rev-001";
    public static final String REVIEW_PRODUCT_ID = "prod-001";
    public static final String REVIEW_USER_ID = "user-001";
    public static final String REVIEW_AUTHOR = "John Doe";
    public static final Integer REVIEW_RATING = 5;
    public static final String REVIEW_TITLE = "Excellent product";
    public static final String REVIEW_BODY = "Very satisfied with the quality";
    public static final Boolean REVIEW_VERIFIED = true;
    public static final ReviewStatus REVIEW_STATUS = ReviewStatus.APPROVED;
    public static final Integer REVIEW_HELPFUL_COUNT = 3;

    private ReviewProvider() {
    }

    public static Review review() {
        return Review.builder().id(REVIEW_ID).productId(REVIEW_PRODUCT_ID).userId(REVIEW_USER_ID)
                .authorName(REVIEW_AUTHOR).rating(REVIEW_RATING).title(REVIEW_TITLE).body(REVIEW_BODY)
                .verified(REVIEW_VERIFIED).status(REVIEW_STATUS).helpfulCount(REVIEW_HELPFUL_COUNT)
                .images(List.of("img1.jpg")).createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT)
                .build();
    }

    public static ReviewStats reviewStats() {
        return ReviewStats.builder().avgRating(4.5).totalCount(100L).count1(2L).count2(5L).count3(10L).count4(30L)
                .count5(53L).build();
    }

    public static ReviewEntity reviewEntity() {
        return ReviewEntity.builder().id(REVIEW_ID).productId(REVIEW_PRODUCT_ID).userId(REVIEW_USER_ID)
                .authorName(REVIEW_AUTHOR).rating(REVIEW_RATING).title(REVIEW_TITLE).body(REVIEW_BODY)
                .verified(REVIEW_VERIFIED).status(REVIEW_STATUS).helpfulCount(REVIEW_HELPFUL_COUNT)
                .images(List.of("img1.jpg")).build();
    }

    public static ReviewDtoIn reviewDtoIn() {
        return ReviewDtoIn.builder().authorName(REVIEW_AUTHOR).rating(REVIEW_RATING).title(REVIEW_TITLE)
                .body(REVIEW_BODY).images(List.of("img1.jpg")).build();
    }

    public static ReviewDtoOut reviewDtoOut() {
        return ReviewDtoOut.builder().id(REVIEW_ID).productId(REVIEW_PRODUCT_ID).userId(REVIEW_USER_ID)
                .authorName(REVIEW_AUTHOR).rating(REVIEW_RATING).title(REVIEW_TITLE).body(REVIEW_BODY)
                .verified(REVIEW_VERIFIED).status(REVIEW_STATUS).helpfulCount(REVIEW_HELPFUL_COUNT)
                .images(List.of("img1.jpg")).createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT)
                .build();
    }

    public static ReviewStatsDtoOut reviewStatsDtoOut() {
        return ReviewStatsDtoOut.builder().avgRating(4.5).totalCount(100L).count1(2L).count2(5L).count3(10L).count4(30L)
                .count5(53L).build();
    }
}
