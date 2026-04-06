package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de filtros para el listado paginado de productos.
 * Solo los campos con valor no nulo se aplican como predicados.
 * Compatible con {@code PageableUtils.toFilterMap()} + {@code FilterUtils.buildSpecification()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para el listado de productos")
public class ProductFilterDto {

    @Schema(description = "Estado de publicación", example = "PUBLISHED")
    private ProductStatus status;

    @Schema(description = "ID de categoría", example = "abc123")
    private String categoryId;

    @Schema(description = "Tipo de producto", example = "ORDINARY_PRODUCT")
    private String productType;

    @Schema(description = "Filtrar por contiene video", example = "false")
    private Boolean isVideo;
}
