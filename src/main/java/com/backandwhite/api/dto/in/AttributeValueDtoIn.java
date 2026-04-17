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
@Schema(description = "DTO for creating or updating an attribute value")
public class AttributeValueDtoIn {

    @Schema(description = "Value ID (null for new, present for update)")
    private String id;

    @Size(max = 255, message = "Value must not exceed 255 characters")
    @Schema(description = "Attribute value", example = "Red")
    private String value;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "HEX color must follow #RRGGBB format")
    @Schema(description = "HEX color (only for COLOR type attributes)", example = "#FF0000")
    private String colorHex;
}
