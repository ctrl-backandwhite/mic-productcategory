package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Traducción del nombre de la categoría")
public class CategoryTranslationDtoIn {

    @NotBlank(message = "El locale es obligatorio")
    @Schema(description = "Código de idioma (ej: es, en, pt-BR)", example = "es")
    private String locale;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre de la categoría en el idioma indicado", example = "Electrónica")
    private String name;
}
