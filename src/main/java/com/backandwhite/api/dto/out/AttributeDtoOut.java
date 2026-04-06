package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.AttributeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Atributo con sus valores")
public class AttributeDtoOut {

    @Schema(description = "ID del atributo")
    private String id;

    @Schema(description = "Nombre del atributo", example = "Color")
    private String name;

    @Schema(description = "Slug URL-friendly", example = "color")
    private String slug;

    @Schema(description = "Tipo de atributo (TEXT, COLOR, SIZE, SELECT)")
    private AttributeType type;

    @Schema(description = "Cantidad de productos que usan este atributo")
    private Long usedInProducts;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;

    @Schema(description = "Valores del atributo")
    private List<AttributeValueDtoOut> values;
}
