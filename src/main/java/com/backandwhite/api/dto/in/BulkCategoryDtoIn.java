package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for bulk category upload with up to 3 hierarchy levels")
public class BulkCategoryDtoIn {

    @NotNull(message = "Rows are required")
    @Size(min = 1, message = "At least one row is required")
    @Valid
    @Schema(description = "List of rows, each representing a category path level 1 → 2 → 3")
    private List<BulkCategoryRowDtoIn> rows;
}
