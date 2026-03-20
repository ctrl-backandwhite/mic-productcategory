package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de la carga masiva de categorías")
public class BulkCategoryResultDtoOut {

    @Schema(description = "Cantidad de categorías creadas", example = "15")
    private int created;

    @Schema(description = "Cantidad de categorías que ya existían (omitidas)", example = "3")
    private int skipped;

    @Schema(description = "Total de filas procesadas", example = "10")
    private int totalRows;
}
