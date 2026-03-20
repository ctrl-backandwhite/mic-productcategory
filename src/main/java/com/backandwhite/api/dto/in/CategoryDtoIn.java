package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una categoría")
public class CategoryDtoIn {

    @Schema(description = "ID del padre (null para categorías raíz)", example = "electronics")
    private String parentId;

    @NotNull(message = "El nivel es obligatorio")
    @Schema(description = "Nivel jerárquico (1=raíz, 2=sub, 3=sub-sub)", example = "1")
    private Integer level;

    @NotNull(message = "Las traducciones son obligatorias")
    @Size(min = 1, message = "Se requiere al menos una traducción")
    @Valid
    @Schema(description = "Traducciones del nombre de la categoría")
    private List<CategoryTranslationDtoIn> translations;
}
