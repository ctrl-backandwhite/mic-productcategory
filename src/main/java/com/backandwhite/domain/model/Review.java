package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.ReviewStatus;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private String id;
    private String productId;
    private String userId;
    private String authorName;
    private Integer rating;
    private String title;
    private String body;
    private Boolean verified;
    private ReviewStatus status;
    private Integer helpfulCount;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private String externalReviewId;

    @Builder.Default
    private String source = "USER";

    private String countryCode;
}
