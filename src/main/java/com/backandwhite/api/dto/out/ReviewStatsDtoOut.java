package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estadísticas de reseñas de un producto")
public class ReviewStatsDtoOut {

    @Schema(description = "Calificación promedio", example = "4.2")
    private Double avgRating;

    @Schema(description = "Total de reseñas aprobadas", example = "150")
    private Long totalCount;

    @Schema(description = "Reseñas con 1 estrella", example = "5")
    private Long count1;

    @Schema(description = "Reseñas con 2 estrellas", example = "10")
    private Long count2;

    @Schema(description = "Reseñas con 3 estrellas", example = "20")
    private Long count3;

    @Schema(description = "Reseñas con 4 estrellas", example = "45")
    private Long count4;

    @Schema(description = "Reseñas con 5 estrellas", example = "70")
    private Long count5;
}
