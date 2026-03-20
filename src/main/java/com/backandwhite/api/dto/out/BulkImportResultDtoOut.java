package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de la carga masiva")
public class BulkImportResultDtoOut {

    @Schema(description = "Cantidad de registros creados exitosamente", example = "15")
    private int created;

    @Schema(description = "Cantidad de registros que fallaron", example = "2")
    private int failed;

    @Schema(description = "Total de filas procesadas", example = "17")
    private int totalRows;

    @Builder.Default
    @Schema(description = "Errores detallados por fila")
    private List<RowError> errors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error de una fila específica")
    public static class RowError {
        @Schema(description = "Índice de la fila (0-based)", example = "3")
        private int row;

        @Schema(description = "Mensaje de error", example = "El categoryId es obligatorio")
        private String message;
    }
}
