package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceRule {

    private String id;
    private PriceRuleScope scope;
    private String scopeId;
    private MarginType marginType;
    private BigDecimal marginValue;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer priority;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
