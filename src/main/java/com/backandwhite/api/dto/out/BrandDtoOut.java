package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.BrandStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Marca con datos completos")
public class BrandDtoOut {

    @Schema(description = "ID de la marca")
    private String id;

    @Schema(description = "Nombre de la marca", example = "Nike")
    private String name;

    @Schema(description = "Slug URL-friendly", example = "nike")
    private String slug;

    @Schema(description = "URL del logotipo")
    private String logoUrl;

    @Schema(description = "Sitio web oficial")
    private String websiteUrl;

    @Schema(description = "Descripción de la marca")
    private String description;

    @Schema(description = "Estado de la marca (ACTIVE, INACTIVE)")
    private BrandStatus status;

    @Schema(description = "Cantidad de productos asociados")
    private Long productCount;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;
}
