package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.BrandStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dynamic filters for brand search")
public class BrandFilterDto {

    @Schema(description = "Filter by status (ACTIVE, INACTIVE)")
    private BrandStatus status;

    @Schema(description = "Search by name (partial, case-insensitive)")
    private String name;
}
