package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valureobject.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para moderar una reseña")
public class ReviewModerateDtoIn {

    @NotNull(message = "El estado es obligatorio")
    @Schema(description = "Nuevo estado (APPROVED o REJECTED)", example = "APPROVED")
    private ReviewStatus status;
}
