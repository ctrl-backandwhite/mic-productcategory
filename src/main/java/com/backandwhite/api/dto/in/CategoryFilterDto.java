package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de filtros para el listado paginado de categorías.
 * Solo los campos con valor no nulo se aplican como predicados.
 * Compatible con {@code PageableUtils.toFilterMap()} + {@code FilterUtils.buildSpecification()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para el listado de categorías")
public class CategoryFilterDto {

    @Schema(description = "Estado de publicación", example = "PUBLISHED")
    private CategoryStatus status;

    @Schema(description = "Filtrar por activo", example = "true")
    private Boolean active;

    @Schema(description = "Filtrar por destacada", example = "false")
    private Boolean featured;

    @Schema(description = "Filtrar por nivel jerárquico (1, 2, 3)", example = "1")
    private Integer level;

    @Schema(description = "ID del padre (para listar subcategorías de un nodo)", example = "abc123")
    private String parentId;
}
