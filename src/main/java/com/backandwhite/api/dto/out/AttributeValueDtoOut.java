package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Valor de un atributo")
public class AttributeValueDtoOut {

    @Schema(description = "ID del valor")
    private String id;

    @Schema(description = "Valor del atributo", example = "Rojo")
    private String value;

    @Schema(description = "Color HEX (solo para tipo COLOR)", example = "#FF0000")
    private String colorHex;

    @Schema(description = "Posición de orden")
    private Integer position;
}
