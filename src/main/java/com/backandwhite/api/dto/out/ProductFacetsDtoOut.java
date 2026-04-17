package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Facet results for product filtering")
public class ProductFacetsDtoOut {

    @Schema(description = "Available brands with product count")
    private List<FacetBrandDto> brands;

    @Schema(description = "Minimum price found")
    private BigDecimal priceMin;

    @Schema(description = "Maximum price found")
    private BigDecimal priceMax;

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Brand with product count")
    public static class FacetBrandDto {

        @Schema(description = "Brand ID")
        private String id;

        @Schema(description = "Brand name")
        private String name;

        @Schema(description = "Product count")
        private Long count;
    }
}
