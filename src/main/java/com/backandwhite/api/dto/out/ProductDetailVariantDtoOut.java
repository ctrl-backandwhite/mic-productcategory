package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valureobject.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Variante del detalle de producto")
public class ProductDetailVariantDtoOut {

    @Schema(description = "ID de la variante", example = "vid-001")
    private String vid;

    @Schema(description = "ID del producto padre")
    private String pid;

    @Schema(description = "Estado de publicación (DRAFT, PUBLISHED)", example = "DRAFT")
    private ProductStatus status;

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

    @Schema(description = "Fecha de creación en CJ")
    private Instant createTime;

    @Schema(description = "Fecha de creación local")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;

    @Schema(description = "Traducciones de la variante")
    private List<ProductDetailVariantTranslationDtoOut> translations;

    @Schema(description = "Inventarios por país")
    private List<ProductDetailVariantInventoryDtoOut> inventories;
}
