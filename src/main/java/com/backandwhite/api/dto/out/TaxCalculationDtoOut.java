package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tax calculation result")
public class TaxCalculationDtoOut {

    @Schema(description = "Subtotal before taxes")
    private BigDecimal subtotal;

    @Schema(description = "Total tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Total with tax included")
    private BigDecimal total;

    @Schema(description = "Applied rates")
    private List<AppliedRateDto> appliedRates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Applied rate detail")
    public static class AppliedRateDto {

        @Schema(description = "Rate name", example = "General VAT 21%")
        private String name;

        @Schema(description = "Applied rate (decimal)", example = "0.21")
        private double rate;

        @Schema(description = "Calculated amount", example = "21.00")
        private double amount;
    }
}
