package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Traducción del detalle de producto")
public class ProductDetailTranslationDtoOut {

    @Schema(description = "Código de idioma", example = "en")
    private String locale;

    @Schema(description = "Nombre del producto traducido")
    private String productName;

    @Schema(description = "Nombre de entrada traducido")
    private String entryName;

    @Schema(description = "Nombre del material traducido")
    private String materialName;

    @Schema(description = "Nombre del empaque traducido")
    private String packingName;

    @Schema(description = "Clave del producto traducida")
    private String productKey;

    @Schema(description = "Propiedades del producto traducidas")
    private String productPro;
}
