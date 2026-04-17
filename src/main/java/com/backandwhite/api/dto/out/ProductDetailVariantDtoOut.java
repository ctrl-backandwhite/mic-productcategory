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
@Schema(description = "Product detail variant")
public class ProductDetailVariantDtoOut {

    @Schema(description = "Variant ID", example = "vid-001")
    private String vid;

    @Schema(description = "Parent product ID")
    private String pid;

    @Schema(description = "Publication status (DRAFT, PUBLISHED)", example = "DRAFT")
    private ProductStatus status;

    @Schema(description = "Variant English name")
    private String variantNameEn;

    @Schema(description = "Variant SKU")
    private String variantSku;

    @Schema(description = "Unit of measure")
    private String variantUnit;

    @Schema(description = "Variant key")
    private String variantKey;

    @Schema(description = "Variant image URL")
    private String variantImage;

    @Schema(description = "Length", example = "30.00")
    private BigDecimal variantLength;

    @Schema(description = "Width", example = "20.00")
    private BigDecimal variantWidth;

    @Schema(description = "Height", example = "5.00")
    private BigDecimal variantHeight;

    @Schema(description = "Volume", example = "3000.00")
    private BigDecimal variantVolume;

    @Schema(description = "Weight", example = "0.50")
    private BigDecimal variantWeight;

    @Schema(description = "Supplier cost price", example = "29.99")
    private BigDecimal variantSellPrice;

    @Schema(description = "CJ suggested price", example = "39.99")
    private BigDecimal variantSugSellPrice;

    @Schema(description = "Retail price (with margin applied)", example = "41.99")
    private BigDecimal retailPrice;

    @Schema(description = "ISO 4217 currency code", example = "EUR")
    private String currencyCode;

    @Schema(description = "Variant standard")
    private String variantStandard;

    @Schema(description = "CJ creation date")
    private Instant createTime;

    @Schema(description = "Local creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;

    @Schema(description = "Variant translations")
    private List<ProductDetailVariantTranslationDtoOut> translations;

    @Schema(description = "Inventories by country")
    private List<ProductDetailVariantInventoryDtoOut> inventories;
}
