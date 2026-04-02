package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para votar una reseña como útil")
public class ReviewHelpfulDtoIn {

    @NotBlank(message = "El sessionId es obligatorio")
    @Schema(description = "ID de sesión del votante (para idempotencia)")
    private String sessionId;
}
