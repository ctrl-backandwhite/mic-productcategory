package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full product detail (CJ)")
public class ProductDetailDtoOut {

    @Schema(description = "CJ Product ID", example = "pid-001")
    private String pid;

    @Schema(description = "Product name in English")
    private String productNameEn;

    @Schema(description = "Product SKU")
    private String productSku;

    @Schema(description = "Main image URL")
    private String bigImage;

    @Schema(description = "Product image URL")
    private String productImage;

    @Schema(description = "Image URL set (comma-separated)")
    private String productImageSet;

    @Schema(description = "Product weight")
    private String productWeight;

    @Schema(description = "Product unit")
    private String productUnit;

    @Schema(description = "Product type")
    private String productType;

    @Schema(description = "Category ID")
    private String categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Entry code")
    private String entryCode;

    @Schema(description = "Entry name (EN)")
    private String entryNameEn;

    @Schema(description = "Material name (EN)")
    private String materialNameEn;

    @Schema(description = "Material key")
    private String materialKey;

    @Schema(description = "Packing weight")
    private String packingWeight;

    @Schema(description = "Packing name (EN)")
    private String packingNameEn;

    @Schema(description = "Packing key")
    private String packingKey;

    @Schema(description = "Product key (EN)")
    private String productKeyEn;

    @Schema(description = "Product properties (EN)")
    private String productProEn;

    @Schema(description = "Sell price", example = "19.99")
    private String sellPrice;

    @Schema(description = "Product HTML description")
    private String description;

    @Schema(description = "Suggested sell price")
    private String suggestSellPrice;

    @Schema(description = "Listed quantity")
    private Integer listedNum;

    @Schema(description = "Product status")
    private String status;

    @Schema(description = "Supplier name")
    private String supplierName;

    @Schema(description = "Supplier ID")
    private String supplierId;

    @Schema(description = "CJ creation date")
    private Instant createrTime;

    @Schema(description = "Local creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;

    @Schema(description = "Currency code (ISO 4217)", example = "USD")
    private String currencyCode;

    @Schema(description = "Currency symbol", example = "$")
    private String currencySymbol;

    @Schema(description = "Numeric sell price (converted to user currency)")
    private BigDecimal sellPriceRaw;

    @Schema(description = "Numeric cost price (converted to user currency)")
    private BigDecimal costPriceRaw;

    @Schema(description = "Cost price (string, may be a range)")
    private String costPrice;

    @Schema(description = "Associated warranty plan id (null when the product has no warranty)")
    private String warrantyId;

    @Schema(description = "Detail translations")
    private List<ProductDetailTranslationDtoOut> translations;

    @Schema(description = "Detail variants")
    private List<ProductDetailVariantDtoOut> variants;
}
