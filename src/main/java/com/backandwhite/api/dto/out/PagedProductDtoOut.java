package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta paginada de productos")
public class PagedProductDtoOut {

    @Schema(description = "Lista de productos")
    private List<ProductDtoOut> content;

    @Schema(description = "Número de página actual (0-based)", example = "0")
    private int page;

    @Schema(description = "Tamaño de la página", example = "20")
    private int size;

    @Schema(description = "Total de elementos", example = "150")
    private long totalElements;

    @Schema(description = "Total de páginas", example = "8")
    private int totalPages;
}
