package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.WarrantyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating or updating a warranty")
public class WarrantyDtoIn {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Warranty plan name", example = "Extended Premium Warranty")
    private String name;

    @NotNull(message = "Type is required")
    @Schema(description = "Warranty type", example = "EXTENDED")
    private WarrantyType type;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Minimum duration is 1 month")
    @Schema(description = "Duration in months", example = "24")
    private Integer durationMonths;

    @Schema(description = "Plan coverage", example = "Covers manufacturing defects and hardware failures")
    private String coverage;

    @Schema(description = "Plan conditions", example = "Does not cover water or drop damage")
    private String conditions;

    @Schema(description = "Includes labor", example = "true")
    private Boolean includesLabor;

    @Schema(description = "Includes parts", example = "true")
    private Boolean includesParts;

    @Schema(description = "Includes home pickup", example = "false")
    private Boolean includesPickup;

    @Schema(description = "Repair limit", example = "3")
    private Integer repairLimit;

    @Size(max = 32, message = "Phone must not exceed 32 characters")
    @Schema(description = "Contact phone", example = "+34 900 123 456")
    private String contactPhone;

    @Email(message = "Invalid email")
    @Schema(description = "Contact email", example = "warranties@example.com")
    private String contactEmail;
}
