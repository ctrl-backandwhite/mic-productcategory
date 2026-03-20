package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Traducción de una categoría")
public class CategoryTranslationDtoOut {

    @Schema(description = "Código de idioma", example = "es")
    private String locale;

    @Schema(description = "Nombre traducido", example = "Electrónica")
    private String name;
}
