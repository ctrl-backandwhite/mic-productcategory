package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Regla de margen de ganancia")
public class PriceRuleDtoOut {

    @Schema(description = "ID de la regla")
    private String id;

    @Schema(description = "Ámbito: GLOBAL, CATEGORY, PRODUCT, VARIANT")
    private PriceRuleScope scope;

    @Schema(description = "ID del ámbito")
    private String scopeId;

    @Schema(description = "Tipo de margen: PERCENTAGE o FIXED")
    private MarginType marginType;

    @Schema(description = "Valor del margen", example = "40.00")
    private BigDecimal marginValue;

    @Schema(description = "Precio mínimo del rango")
    private BigDecimal minPrice;

    @Schema(description = "Precio máximo del rango")
    private BigDecimal maxPrice;

    @Schema(description = "Prioridad")
    private Integer priority;

    @Schema(description = "Si la regla está activa")
    private Boolean active;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de actualización")
    private Instant updatedAt;
}
