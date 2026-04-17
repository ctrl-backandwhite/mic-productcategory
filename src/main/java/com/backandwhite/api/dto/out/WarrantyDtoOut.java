package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.WarrantyType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Warranty plan")
public class WarrantyDtoOut {

    @Schema(description = "Warranty ID")
    private String id;

    @Schema(description = "Plan name")
    private String name;

    @Schema(description = "Warranty type")
    private WarrantyType type;

    @Schema(description = "Duration in months")
    private Integer durationMonths;

    @Schema(description = "Coverage")
    private String coverage;

    @Schema(description = "Conditions")
    private String conditions;

    @Schema(description = "Includes labor")
    private Boolean includesLabor;

    @Schema(description = "Includes parts")
    private Boolean includesParts;

    @Schema(description = "Includes home pickup")
    private Boolean includesPickup;

    @Schema(description = "Repair limit")
    private Integer repairLimit;

    @Schema(description = "Contact phone")
    private String contactPhone;

    @Schema(description = "Contact email")
    private String contactEmail;

    @Schema(description = "Active/inactive status")
    private Boolean active;

    @Schema(description = "Number of associated products")
    private Long productsCount;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;
}
