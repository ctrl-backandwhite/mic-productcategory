package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for moderating a review")
public class ReviewModerateDtoIn {

    @NotNull(message = "Status is required")
    @Schema(description = "New status (APPROVED or REJECTED)", example = "APPROVED")
    private ReviewStatus status;
}
