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
@Schema(description = "DTO for bulk variant upload (CSV / JSON)")
public class BulkVariantDtoIn {

    @NotNull(message = "Variants are required")
    @Size(min = 1, message = "At least one variant is required")
    @Valid
    @Schema(description = "List of variants to create")
    private List<ProductDetailVariantDtoIn> rows;
}
