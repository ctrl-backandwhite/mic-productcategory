package com.backandwhite.domain.model;

import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStats {

    private Double avgRating;
    private Long totalCount;
    private Long count1;
    private Long count2;
    private Long count3;
    private Long count4;
    private Long count5;
}
