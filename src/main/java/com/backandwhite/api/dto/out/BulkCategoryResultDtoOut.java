package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk category upload result")
public class BulkCategoryResultDtoOut {

    @Schema(description = "Number of categories created", example = "15")
    private int created;

    @Schema(description = "Number of categories that already existed (skipped)", example = "3")
    private int skipped;

    @Schema(description = "Total rows processed", example = "10")
    private int totalRows;
}
