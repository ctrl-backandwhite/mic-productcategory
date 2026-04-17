package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter DTO for paginated category listing.
 * Only non-null fields are applied as predicates.
 * Compatible with {@code PageableUtils.toFilterMap()} + {@code FilterUtils.buildSpecification()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category listing filters")
public class CategoryFilterDto {

    @Schema(description = "Publication status", example = "PUBLISHED")
    private CategoryStatus status;

    @Schema(description = "Filter by active", example = "true")
    private Boolean active;

    @Schema(description = "Filter by featured", example = "false")
    private Boolean featured;

    @Schema(description = "Filter by hierarchy level (1, 2, 3)", example = "1")
    private Integer level;

    @Schema(description = "Parent ID (to list subcategories of a node)", example = "abc123")
    private String parentId;
}
