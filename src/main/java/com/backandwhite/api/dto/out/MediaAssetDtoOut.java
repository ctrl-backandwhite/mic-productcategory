package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valureobject.MediaCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Media asset")
public class MediaAssetDtoOut {

    @Schema(description = "ID del asset")
    private String id;

    @Schema(description = "Nombre de archivo generado")
    private String filename;

    @Schema(description = "Nombre de archivo original")
    private String originalName;

    @Schema(description = "Tipo MIME", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "Tamaño en bytes")
    private Long sizeBytes;

    @Schema(description = "URL del archivo")
    private String url;

    @Schema(description = "URL del thumbnail")
    private String thumbnailUrl;

    @Schema(description = "Categoría")
    private MediaCategory category;

    @Schema(description = "Etiquetas")
    private List<String> tags;

    @Schema(description = "Texto alternativo")
    private String alt;

    @Schema(description = "Ancho en píxeles")
    private Integer width;

    @Schema(description = "Alto en píxeles")
    private Integer height;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;
}
