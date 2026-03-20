package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para carga masiva de categorías con hasta 3 niveles de jerarquía")
public class BulkCategoryDtoIn {

    @NotNull(message = "Las filas son obligatorias")
    @Size(min = 1, message = "Se requiere al menos una fila")
    @Valid
    @Schema(description = "Lista de filas, cada una representa una ruta de categorías nivel 1 → 2 → 3")
    private List<BulkCategoryRowDtoIn> rows;
}
