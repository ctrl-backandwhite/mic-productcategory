package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk import result")
public class BulkImportResultDtoOut {

    @Schema(description = "Number of successfully created records", example = "15")
    private int created;

    @Schema(description = "Number of failed records", example = "2")
    private int failed;

    @Schema(description = "Total processed rows", example = "17")
    private int totalRows;

    @Builder.Default
    @Schema(description = "Detailed errors per row")
    private List<RowError> errors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error for a specific row")
    public static class RowError {
        @Schema(description = "Row index (0-based)", example = "3")
        private int row;

        @Schema(description = "Error message", example = "categoryId is required")
        private String message;
    }
}
