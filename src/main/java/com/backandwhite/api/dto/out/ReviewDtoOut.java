package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product review")
public class ReviewDtoOut {

    @Schema(description = "Review ID")
    private String id;

    @Schema(description = "Product ID")
    private String productId;

    @Schema(description = "User ID (if authenticated)")
    private String userId;

    @Schema(description = "Author name")
    private String authorName;

    @Schema(description = "Rating (1-5)")
    private Integer rating;

    @Schema(description = "Review title")
    private String title;

    @Schema(description = "Review body")
    private String body;

    @Schema(description = "Whether the reviewer is a verified buyer")
    private Boolean verified;

    @Schema(description = "Moderation status")
    private ReviewStatus status;

    @Schema(description = "Helpfulness vote count")
    private Integer helpfulCount;

    @Schema(description = "Attached image URLs")
    private List<String> images;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;
}
