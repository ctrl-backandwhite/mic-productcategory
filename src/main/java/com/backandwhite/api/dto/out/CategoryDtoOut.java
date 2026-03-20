package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valureobject.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Categoría con sus subcategorías traducidas")
public class CategoryDtoOut {

    @Schema(description = "ID de la categoría", example = "electronics")
    private String id;

    @Schema(description = "ID del padre", example = "root-id")
    private String parentId;

    @Schema(description = "Nombre traducido de la categoría", example = "Electrónica")
    private String name;

    @Schema(description = "Nivel jerárquico (1=raíz, 2=sub, 3=sub-sub)", example = "1")
    private Integer level;

    @Schema(description = "Estado de publicación (DRAFT, PUBLISHED)", example = "PUBLISHED")
    private CategoryStatus status;

    @Schema(description = "Indica si la categoría está activa", example = "true")
    private Boolean active;

    @Schema(description = "Indica si la categoría es principal/destacada", example = "false")
    private Boolean featured;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;

    @Schema(description = "Traducciones de la categoría")
    private List<CategoryTranslationDtoOut> translations;

    @Schema(description = "Subcategorías de esta categoría")
    private List<CategoryDtoOut> subCategories;
}
