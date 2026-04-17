package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating or updating a product variant")
public class ProductDetailVariantDtoIn {

    @NotBlank(message = "pid is required")
    @Schema(description = "Parent product ID", example = "CJ-PID-001")
    private String pid;

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

    @Schema(description = "Variant sell price", example = "29.99")
    private BigDecimal variantSellPrice;

    @Schema(description = "Suggested sell price", example = "39.99")
    private BigDecimal variantSugSellPrice;

    @Schema(description = "Variant standard")
    private String variantStandard;

    @Valid
    @Schema(description = "Variant translations")
    private List<ProductDetailVariantTranslationDtoIn> translations;

    @Valid
    @Schema(description = "Inventories by country")
    private List<ProductDetailVariantInventoryDtoIn> inventories;
}
