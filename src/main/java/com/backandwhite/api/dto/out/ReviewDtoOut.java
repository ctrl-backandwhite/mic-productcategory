package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reseña de producto")
public class ReviewDtoOut {

    @Schema(description = "ID de la reseña")
    private String id;

    @Schema(description = "ID del producto")
    private String productId;

    @Schema(description = "ID del usuario (si autenticado)")
    private String userId;

    @Schema(description = "Nombre del autor")
    private String authorName;

    @Schema(description = "Calificación (1-5)")
    private Integer rating;

    @Schema(description = "Título de la reseña")
    private String title;

    @Schema(description = "Cuerpo de la reseña")
    private String body;

    @Schema(description = "Indica si es un comprador verificado")
    private Boolean verified;

    @Schema(description = "Estado de moderación")
    private ReviewStatus status;

    @Schema(description = "Cantidad de votos de utilidad")
    private Integer helpfulCount;

    @Schema(description = "URLs de imágenes adjuntas")
    private List<String> images;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;
}
