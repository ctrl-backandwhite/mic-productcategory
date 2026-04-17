package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profit margin rule")
public class PriceRuleDtoIn {

    @NotNull
    @Schema(description = "Rule scope: GLOBAL, CATEGORY, PRODUCT, VARIANT", example = "CATEGORY")
    private PriceRuleScope scope;

    @Schema(description = "Scope ID (null for GLOBAL, categoryId, productId or variantId)", example = "cat-001")
    private String scopeId;

    @NotNull
    @Schema(description = "Margin type: PERCENTAGE or FIXED", example = "PERCENTAGE")
    private MarginType marginType;

    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "Margin value (percentage or fixed amount)", example = "40.00")
    private BigDecimal marginValue;

    @DecimalMin("0.00")
    @Schema(description = "Minimum price in range (null = no lower bound)", example = "0.00")
    private BigDecimal minPrice;

    @DecimalMin("0.00")
    @Schema(description = "Maximum price in range (null = no upper bound)", example = "10.00")
    private BigDecimal maxPrice;

    @Schema(description = "Priority (higher = more preferred)", example = "0")
    private Integer priority;

    @Schema(description = "Whether the rule is active", example = "true")
    private Boolean active;
}
