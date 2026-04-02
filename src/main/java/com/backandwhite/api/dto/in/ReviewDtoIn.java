package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear una reseña de producto")
public class ReviewDtoIn {

    @NotBlank(message = "El nombre del autor es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Schema(description = "Nombre del autor", example = "Juan García")
    private String authorName;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    @Schema(description = "Calificación de 1 a 5 estrellas", example = "4")
    private Integer rating;

    @Size(max = 500, message = "El título no puede exceder 500 caracteres")
    @Schema(description = "Título de la reseña", example = "Excelente producto")
    private String title;

    @Schema(description = "Cuerpo de la reseña", example = "La calidad superó mis expectativas...")
    private String body;

    @Schema(description = "URLs de imágenes adjuntas")
    private List<String> images;
}
