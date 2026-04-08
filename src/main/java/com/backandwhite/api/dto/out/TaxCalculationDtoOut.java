package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado del cálculo de impuestos")
public class TaxCalculationDtoOut {

    @Schema(description = "Subtotal antes de impuestos")
    private BigDecimal subtotal;

    @Schema(description = "Monto total del impuesto")
    private BigDecimal taxAmount;

    @Schema(description = "Total con impuesto incluido")
    private BigDecimal total;

    @Schema(description = "Tasas aplicadas")
    private List<AppliedRateDto> appliedRates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de tasa aplicada")
    public static class AppliedRateDto {

        @Schema(description = "Nombre de la tasa", example = "IVA General 21%")
        private String name;

        @Schema(description = "Tasa aplicada (decimal)", example = "0.21")
        private double rate;

        @Schema(description = "Monto calculado", example = "21.00")
        private double amount;
    }
}
