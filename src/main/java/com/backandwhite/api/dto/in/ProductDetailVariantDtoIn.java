package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una variante de producto")
public class ProductDetailVariantDtoIn {

    @NotBlank(message = "El pid es obligatorio")
    @Schema(description = "ID del producto padre", example = "CJ-PID-001")
    private String pid;

    @Schema(description = "Nombre en inglés de la variante")
    private String variantNameEn;

    @Schema(description = "SKU de la variante")
    private String variantSku;

    @Schema(description = "Unidad de medida")
    private String variantUnit;

    @Schema(description = "Clave de la variante")
    private String variantKey;

    @Schema(description = "URL de imagen de la variante")
    private String variantImage;

    @Schema(description = "Largo", example = "30.00")
    private BigDecimal variantLength;

    @Schema(description = "Ancho", example = "20.00")
    private BigDecimal variantWidth;

    @Schema(description = "Alto", example = "5.00")
    private BigDecimal variantHeight;

    @Schema(description = "Volumen", example = "3000.00")
    private BigDecimal variantVolume;

    @Schema(description = "Peso", example = "0.50")
    private BigDecimal variantWeight;

    @Schema(description = "Precio de venta de la variante", example = "29.99")
    private BigDecimal variantSellPrice;

    @Schema(description = "Precio sugerido de venta", example = "39.99")
    private BigDecimal variantSugSellPrice;

    @Schema(description = "Estándar de la variante")
    private String variantStandard;

    @Valid
    @Schema(description = "Traducciones de la variante")
    private List<ProductDetailVariantTranslationDtoIn> translations;

    @Valid
    @Schema(description = "Inventarios por país")
    private List<ProductDetailVariantInventoryDtoIn> inventories;
}
