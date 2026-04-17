package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating or updating a product")
public class ProductDtoIn {

    @Schema(description = "Product SKU", example = "SKU-001")
    private String sku;

    @NotNull(message = "categoryId is required")
    @Schema(description = "Category ID the product belongs to", example = "cat-001")
    private String categoryId;

    @Schema(description = "Main image URL")
    private String bigImage;

    @Schema(description = "Sell price", example = "19.99")
    private String sellPrice;

    @Schema(description = "Product type (e.g. NORMAL)", example = "NORMAL")
    private String productType;

    @Schema(description = "Listed quantity", example = "100")
    private Integer listedNum;

    @Schema(description = "Warehouse inventory", example = "500")
    private Integer warehouseInventoryNum;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Product image URLs (comma-separated)")
    private String productImageSet;

    @Schema(description = "Has video", example = "false")
    private Boolean isVideo;

    @NotNull(message = "Translations are required")
    @Size(min = 1, message = "At least one translation is required")
    @Valid
    @Schema(description = "Product translations")
    private List<ProductTranslationDtoIn> translations;
}
