package com.backandwhite.domain.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFacets {

    @Builder.Default
    private List<FacetBrand> brands = new ArrayList<>();

    private Double priceMin;
    private Double priceMax;

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacetBrand {
        private String id;
        private String name;
        private Long count;
    }
}
