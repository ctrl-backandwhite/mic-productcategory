package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter DTO for the paginated product listing.
 * Only fields with non-null values are applied as predicates.
 * Compatible with {@code PageableUtils.toFilterMap()} + {@code FilterUtils.buildSpecification()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filters for the product listing")
public class ProductFilterDto {

    @Schema(description = "Publication status", example = "PUBLISHED")
    private ProductStatus status;

    @Schema(description = "Category ID", example = "abc123")
    private String categoryId;

    @Schema(description = "Product type", example = "ORDINARY_PRODUCT")
    private String productType;

    @Schema(description = "Filter by contains video", example = "false")
    private Boolean isVideo;
}
