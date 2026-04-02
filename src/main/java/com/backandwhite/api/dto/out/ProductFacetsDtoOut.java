package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de facetas para filtrado de productos")
public class ProductFacetsDtoOut {

    @Schema(description = "Marcas disponibles con conteo de productos")
    private List<FacetBrandDto> brands;

    @Schema(description = "Precio mínimo encontrado")
    private Double priceMin;

    @Schema(description = "Precio máximo encontrado")
    private Double priceMax;

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Marca con conteo de productos")
    public static class FacetBrandDto {

        @Schema(description = "ID de la marca")
        private String id;

        @Schema(description = "Nombre de la marca")
        private String name;

        @Schema(description = "Cantidad de productos")
        private Long count;
    }
}
