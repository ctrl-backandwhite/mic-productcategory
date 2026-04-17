package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attribute value")
public class AttributeValueDtoOut {

    @Schema(description = "Value ID")
    private String id;

    @Schema(description = "Attribute value", example = "Red")
    private String value;

    @Schema(description = "HEX color (only for COLOR type)", example = "#FF0000")
    private String colorHex;

    @Schema(description = "Sort position")
    private Integer position;
}
