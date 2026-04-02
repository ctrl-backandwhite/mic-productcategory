package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock availability for a product variant")
public class StockDtoOut {

    @Schema(description = "Variant ID", example = "VID-001")
    private String variantId;

    @Schema(description = "Available stock quantity", example = "42")
    private int available;

    @Schema(description = "Whether the variant is in stock", example = "true")
    private boolean inStock;
}
