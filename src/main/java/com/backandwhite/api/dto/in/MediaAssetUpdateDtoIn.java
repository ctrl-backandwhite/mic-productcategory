package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.MediaCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para actualizar metadatos de un media asset")
public class MediaAssetUpdateDtoIn {

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "Categoría del asset", example = "PRODUCT")
    private MediaCategory category;

    @Schema(description = "Texto alternativo para accesibilidad", example = "Producto Nike Air Max")
    private String alt;

    @Schema(description = "Etiquetas del asset")
    private List<String> tags;
}
