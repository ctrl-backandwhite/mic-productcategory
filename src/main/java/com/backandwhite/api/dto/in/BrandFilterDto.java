package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.BrandStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros dinámicos para búsqueda de marcas")
public class BrandFilterDto {

    @Schema(description = "Filtrar por estado (ACTIVE, INACTIVE)")
    private BrandStatus status;

    @Schema(description = "Buscar por nombre (parcial, case-insensitive)")
    private String name;
}
