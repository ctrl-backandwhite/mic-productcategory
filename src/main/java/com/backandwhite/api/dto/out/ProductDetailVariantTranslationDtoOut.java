package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Traducción de la variante del detalle")
public class ProductDetailVariantTranslationDtoOut {

    @Schema(description = "Código de idioma", example = "en")
    private String locale;

    @Schema(description = "Nombre de la variante traducido")
    private String variantName;
}
