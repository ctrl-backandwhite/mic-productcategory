package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar un valor de atributo")
public class AttributeValueDtoIn {

    @Schema(description = "ID del valor (null para nuevos, presente para actualización)")
    private String id;

    @Size(max = 255, message = "El valor no puede exceder 255 caracteres")
    @Schema(description = "Valor del atributo", example = "Rojo")
    private String value;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "El color HEX debe tener formato #RRGGBB")
    @Schema(description = "Color HEX (solo para atributos tipo COLOR)", example = "#FF0000")
    private String colorHex;
}
