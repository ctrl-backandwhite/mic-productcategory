package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for voting a review as helpful")
public class ReviewHelpfulDtoIn {

    @NotBlank(message = "sessionId is required")
    @Schema(description = "Voter session ID (for idempotency)")
    private String sessionId;
}
