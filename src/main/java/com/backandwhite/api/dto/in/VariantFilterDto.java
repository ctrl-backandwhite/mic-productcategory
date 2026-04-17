package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter DTO for the paginated variant listing.
 * Only fields with non-null values are applied as predicates.
 * Compatible with {@code PageableUtils.toFilterMap()} + {@code FilterUtils.buildSpecification()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filters for the variant listing")
public class VariantFilterDto {

    @Schema(description = "Publication status", example = "PUBLISHED")
    private ProductStatus status;

    @Schema(description = "Search text (name, SKU, VID, PID)", example = "SKU-001")
    private String search;

    @Schema(description = "Filter by parent product PID", example = "PROD-001")
    private String pid;
}
