package com.backandwhite.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFacets {

    @Builder.Default
    private List<FacetBrand> brands = new ArrayList<>();

    private BigDecimal priceMin;
    private BigDecimal priceMax;

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
