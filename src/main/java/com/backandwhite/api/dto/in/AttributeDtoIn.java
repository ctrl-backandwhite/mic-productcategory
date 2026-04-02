package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valureobject.AttributeType;
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
@Schema(description = "DTO para crear o actualizar un atributo")
public class AttributeDtoIn {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Schema(description = "Nombre del atributo", example = "Color")
    private String name;

    @NotBlank(message = "El slug es obligatorio")
    @Size(max = 255, message = "El slug no puede exceder 255 caracteres")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "El slug debe ser alfanumérico en minúsculas separado por guiones")
    @Schema(description = "Slug URL-friendly del atributo", example = "color")
    private String slug;

    @NotNull(message = "El tipo es obligatorio")
    @Schema(description = "Tipo de atributo (TEXT, COLOR, SIZE, SELECT)", example = "COLOR")
    private AttributeType type;

    @Valid
    @Schema(description = "Valores del atributo")
    private List<AttributeValueDtoIn> values;
}
