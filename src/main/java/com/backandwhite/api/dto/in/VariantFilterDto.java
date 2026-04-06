package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de filtros para el listado paginado de variantes.
 * Solo los campos con valor no nulo se aplican como predicados.
 * Compatible con {@code PageableUtils.toFilterMap()} + {@code FilterUtils.buildSpecification()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para el listado de variantes")
public class VariantFilterDto {

    @Schema(description = "Estado de publicación", example = "PUBLISHED")
    private ProductStatus status;

    @Schema(description = "Texto de búsqueda (nombre, SKU, VID, PID)", example = "SKU-001")
    private String search;

    @Schema(description = "Filtrar por PID del producto padre", example = "PROD-001")
    private String pid;
}
