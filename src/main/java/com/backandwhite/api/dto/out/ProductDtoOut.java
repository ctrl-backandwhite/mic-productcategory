package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.ProductStatus;
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
@Schema(description = "Product with its translations and variants")
public class ProductDtoOut {

    @Schema(description = "Product ID", example = "prod-001")
    private String id;

    @Schema(description = "Product SKU", example = "SKU-001")
    private String sku;

    @Schema(description = "Category ID", example = "cat-001")
    private String categoryId;

    @Schema(description = "Brand ID (nullable)", example = "brand-001")
    private String brandId;

    @Schema(description = "Warranty ID (nullable)", example = "war-001")
    private String warrantyId;

    @Schema(description = "Publication status (DRAFT, PUBLISHED)", example = "DRAFT")
    private ProductStatus status;

    @Schema(description = "Translated product name", example = "Cotton T-shirt")
    private String name;

    @Schema(description = "Main image URL")
    private String bigImage;

    @Schema(description = "Product image URLs (comma-separated)")
    private String productImageSet;

    @Schema(description = "Retail sell price (with margin)", example = "27.99")
    private String sellPrice;

    @Schema(description = "Supplier cost price", example = "19.99")
    private String costPrice;

    @Schema(description = "ISO 4217 currency code", example = "EUR")
    private String currencyCode;

    @Schema(description = "Currency symbol", example = "€")
    private String currencySymbol;

    @Schema(description = "Numeric sell price (min of range, already converted)", example = "25.88")
    private BigDecimal sellPriceRaw;

    @Schema(description = "Numeric cost price (min of range, already converted)", example = "0.78")
    private BigDecimal costPriceRaw;

    @Schema(description = "Product type", example = "NORMAL")
    private String productType;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Listed quantity", example = "100")
    private Integer listedNum;

    @Schema(description = "Warehouse inventory", example = "500")
    private Integer warehouseInventoryNum;

    @Schema(description = "Has video", example = "false")
    private Boolean isVideo;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;

    @Schema(description = "Product translations")
    private List<ProductTranslationDtoOut> translations;

    @Schema(description = "Product variants")
    private List<ProductDetailVariantDtoOut> variants;
}
