package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.AttributeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating or updating an attribute")
public class AttributeDtoIn {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Attribute name", example = "Color")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric separated by hyphens")
    @Schema(description = "URL-friendly attribute slug", example = "color")
    private String slug;

    @NotNull(message = "Type is required")
    @Schema(description = "Attribute type (TEXT, COLOR, SIZE, SELECT)", example = "COLOR")
    private AttributeType type;

    @Valid
    @Schema(description = "Attribute values")
    private List<AttributeValueDtoIn> values;
}
