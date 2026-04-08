package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.ProductStatus;
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
@Schema(description = "Producto con sus traducciones y variantes")
public class ProductDtoOut {

    @Schema(description = "ID del producto", example = "prod-001")
    private String id;

    @Schema(description = "SKU del producto", example = "SKU-001")
    private String sku;

    @Schema(description = "ID de la categoría", example = "cat-001")
    private String categoryId;

    @Schema(description = "Estado de publicación (DRAFT, PUBLISHED)", example = "DRAFT")
    private ProductStatus status;

    @Schema(description = "Nombre traducido del producto", example = "Camiseta de algodón")
    private String name;

    @Schema(description = "URL de la imagen principal")
    private String bigImage;

    @Schema(description = "URLs de imágenes del producto (separadas por coma)")
    private String productImageSet;

    @Schema(description = "Precio de venta al público (con margen)", example = "27.99")
    private String sellPrice;

    @Schema(description = "Precio de costo del proveedor", example = "19.99")
    private String costPrice;

    @Schema(description = "Código ISO 4217 de la moneda", example = "EUR")
    private String currencyCode;

    @Schema(description = "Símbolo de la moneda", example = "€")
    private String currencySymbol;

    @Schema(description = "Precio de venta numérico (mín. del rango, ya convertido)", example = "25.88")
    private BigDecimal sellPriceRaw;

    @Schema(description = "Precio de costo numérico (mín. del rango, ya convertido)", example = "0.78")
    private BigDecimal costPriceRaw;

    @Schema(description = "Tipo de producto", example = "NORMAL")
    private String productType;

    @Schema(description = "Descripción del producto")
    private String description;

    @Schema(description = "Cantidad listada", example = "100")
    private Integer listedNum;

    @Schema(description = "Inventario en almacén", example = "500")
    private Integer warehouseInventoryNum;

    @Schema(description = "Tiene video", example = "false")
    private Boolean isVideo;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;

    @Schema(description = "Traducciones del producto")
    private List<ProductTranslationDtoOut> translations;

    @Schema(description = "Variantes del producto")
    private List<ProductDetailVariantDtoOut> variants;
}
