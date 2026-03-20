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
@Schema(description = "Una fila del CSV de carga masiva (nivel 1 obligatorio, niveles 2 y 3 opcionales)")
public class BulkCategoryRowDtoIn {

    @NotNull(message = "Las traducciones del nivel 1 son obligatorias")
    @Size(min = 1, message = "Se requiere al menos una traducción del nivel 1")
    @Valid
    @Schema(description = "Traducciones para la categoría de nivel 1")
    private List<CategoryTranslationDtoIn> level1Translations;

    @Valid
    @Schema(description = "Traducciones para la subcategoría de nivel 2 (opcional)")
    private List<CategoryTranslationDtoIn> level2Translations;

    @Valid
    @Schema(description = "Traducciones para la subcategoría de nivel 3 (opcional)")
    private List<CategoryTranslationDtoIn> level3Translations;
}
