package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Regla de margen de ganancia")
public class PriceRuleDtoIn {

    @NotNull
    @Schema(description = "Ámbito de la regla: GLOBAL, CATEGORY, PRODUCT, VARIANT", example = "CATEGORY")
    private PriceRuleScope scope;

    @Schema(description = "ID del ámbito (null para GLOBAL, categoryId, productId o variantId)", example = "cat-001")
    private String scopeId;

    @NotNull
    @Schema(description = "Tipo de margen: PERCENTAGE o FIXED", example = "PERCENTAGE")
    private MarginType marginType;

    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "Valor del margen (porcentaje o monto fijo)", example = "40.00")
    private BigDecimal marginValue;

    @DecimalMin("0.00")
    @Schema(description = "Precio mínimo del rango (null = sin límite inferior)", example = "0.00")
    private BigDecimal minPrice;

    @DecimalMin("0.00")
    @Schema(description = "Precio máximo del rango (null = sin límite superior)", example = "10.00")
    private BigDecimal maxPrice;

    @Schema(description = "Prioridad (mayor = más preferente)", example = "0")
    private Integer priority;

    @Schema(description = "Si la regla está activa", example = "true")
    private Boolean active;
}
