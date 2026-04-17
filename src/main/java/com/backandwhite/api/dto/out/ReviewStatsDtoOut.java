package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product review statistics")
public class ReviewStatsDtoOut {

    @Schema(description = "Average rating", example = "4.2")
    private Double avgRating;

    @Schema(description = "Total approved reviews", example = "150")
    private Long totalCount;

    @Schema(description = "Reviews with 1 star", example = "5")
    private Long count1;

    @Schema(description = "Reviews with 2 stars", example = "10")
    private Long count2;

    @Schema(description = "Reviews with 3 stars", example = "20")
    private Long count3;

    @Schema(description = "Reviews with 4 stars", example = "45")
    private Long count4;

    @Schema(description = "Reviews with 5 stars", example = "70")
    private Long count5;
}
