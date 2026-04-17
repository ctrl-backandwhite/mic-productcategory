package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profit margin rule")
public class PriceRuleDtoOut {

    @Schema(description = "Rule ID")
    private String id;

    @Schema(description = "Scope: GLOBAL, CATEGORY, PRODUCT, VARIANT")
    private PriceRuleScope scope;

    @Schema(description = "Scope ID")
    private String scopeId;

    @Schema(description = "Margin type: PERCENTAGE or FIXED")
    private MarginType marginType;

    @Schema(description = "Margin value", example = "40.00")
    private BigDecimal marginValue;

    @Schema(description = "Minimum price range")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price range")
    private BigDecimal maxPrice;

    @Schema(description = "Priority")
    private Integer priority;

    @Schema(description = "Whether the rule is active")
    private Boolean active;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;
}
