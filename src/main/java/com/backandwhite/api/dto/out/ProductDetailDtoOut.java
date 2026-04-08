package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle completo de producto (CJ)")
public class ProductDetailDtoOut {

    @Schema(description = "CJ Product ID", example = "pid-001")
    private String pid;

    @Schema(description = "Nombre en inglés del producto")
    private String productNameEn;

    @Schema(description = "SKU del producto")
    private String productSku;

    @Schema(description = "URL de la imagen principal")
    private String bigImage;

    @Schema(description = "URL de imagen del producto")
    private String productImage;

    @Schema(description = "Conjunto de URLs de imágenes (separadas por coma)")
    private String productImageSet;

    @Schema(description = "Peso del producto")
    private String productWeight;

    @Schema(description = "Unidad del producto")
    private String productUnit;

    @Schema(description = "Tipo de producto")
    private String productType;

    @Schema(description = "ID de la categoría")
    private String categoryId;

    @Schema(description = "Nombre de la categoría")
    private String categoryName;

    @Schema(description = "Código de entrada")
    private String entryCode;

    @Schema(description = "Nombre de entrada (EN)")
    private String entryNameEn;

    @Schema(description = "Nombre del material (EN)")
    private String materialNameEn;

    @Schema(description = "Clave del material")
    private String materialKey;

    @Schema(description = "Peso del empaque")
    private String packingWeight;

    @Schema(description = "Nombre del empaque (EN)")
    private String packingNameEn;

    @Schema(description = "Clave del empaque")
    private String packingKey;

    @Schema(description = "Clave del producto (EN)")
    private String productKeyEn;

    @Schema(description = "Propiedades del producto (EN)")
    private String productProEn;

    @Schema(description = "Precio de venta", example = "19.99")
    private String sellPrice;

    @Schema(description = "Descripción HTML del producto")
    private String description;

    @Schema(description = "Precio sugerido de venta")
    private String suggestSellPrice;

    @Schema(description = "Cantidad listada")
    private Integer listedNum;

    @Schema(description = "Estado del producto")
    private String status;

    @Schema(description = "Nombre del proveedor")
    private String supplierName;

    @Schema(description = "ID del proveedor")
    private String supplierId;

    @Schema(description = "Fecha de creación en CJ")
    private Instant createrTime;

    @Schema(description = "Fecha de creación local")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;

    @Schema(description = "Traducciones del detalle")
    private List<ProductDetailTranslationDtoOut> translations;

    @Schema(description = "Variantes del detalle")
    private List<ProductDetailVariantDtoOut> variants;
}
