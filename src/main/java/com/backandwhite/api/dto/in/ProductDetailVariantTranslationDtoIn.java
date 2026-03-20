package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Traducción de variante")
public class ProductDetailVariantTranslationDtoIn {

    @NotBlank(message = "El locale es obligatorio")
    @Schema(description = "Código de idioma", example = "en")
    private String locale;

    @Schema(description = "Nombre de la variante traducido")
    private String variantName;
}
