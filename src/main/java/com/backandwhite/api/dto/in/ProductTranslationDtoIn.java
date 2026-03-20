package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Traducción del nombre de un producto")
public class ProductTranslationDtoIn {

    @NotBlank(message = "El locale es obligatorio")
    @Schema(description = "Código de idioma (ej: es, en, pt-BR)", example = "es")
    private String locale;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre del producto en el idioma indicado", example = "Camiseta de algodón")
    private String name;
}
