package com.backandwhite.provider;

import com.backandwhite.api.dto.in.PriceRuleDtoIn;
import com.backandwhite.api.dto.out.PriceRuleDtoOut;
import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import com.backandwhite.infrastructure.db.postgres.entity.PriceRuleEntity;
import java.math.BigDecimal;

public final class PriceRuleProvider {

    public static final String RULE_ID = "rule-001";
    public static final PriceRuleScope RULE_SCOPE = PriceRuleScope.GLOBAL;
    public static final String RULE_SCOPE_ID = null;
    public static final MarginType RULE_MARGIN_TYPE = MarginType.PERCENTAGE;
    public static final BigDecimal RULE_MARGIN_VALUE = new BigDecimal("25.00");
    public static final BigDecimal RULE_MIN_PRICE = new BigDecimal("10.00");
    public static final BigDecimal RULE_MAX_PRICE = new BigDecimal("1000.00");
    public static final Integer RULE_PRIORITY = 0;
    public static final Boolean RULE_ACTIVE = true;

    private PriceRuleProvider() {
    }

    public static PriceRule priceRule() {
        return PriceRule.builder().id(RULE_ID).scope(RULE_SCOPE).scopeId(RULE_SCOPE_ID).marginType(RULE_MARGIN_TYPE)
                .marginValue(RULE_MARGIN_VALUE).minPrice(RULE_MIN_PRICE).maxPrice(RULE_MAX_PRICE)
                .priority(RULE_PRIORITY).active(RULE_ACTIVE).createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT).build();
    }

    public static PriceRuleEntity priceRuleEntity() {
        return PriceRuleEntity.builder().id(RULE_ID).scope(RULE_SCOPE).scopeId(RULE_SCOPE_ID)
                .marginType(RULE_MARGIN_TYPE).marginValue(RULE_MARGIN_VALUE).minPrice(RULE_MIN_PRICE)
                .maxPrice(RULE_MAX_PRICE).priority(RULE_PRIORITY).active(RULE_ACTIVE).build();
    }

    public static PriceRuleDtoIn priceRuleDtoIn() {
        return PriceRuleDtoIn.builder().scope(RULE_SCOPE).scopeId(RULE_SCOPE_ID).marginType(RULE_MARGIN_TYPE)
                .marginValue(RULE_MARGIN_VALUE).minPrice(RULE_MIN_PRICE).maxPrice(RULE_MAX_PRICE)
                .priority(RULE_PRIORITY).active(RULE_ACTIVE).build();
    }

    public static PriceRuleDtoOut priceRuleDtoOut() {
        return PriceRuleDtoOut.builder().id(RULE_ID).scope(RULE_SCOPE).scopeId(RULE_SCOPE_ID)
                .marginType(RULE_MARGIN_TYPE).marginValue(RULE_MARGIN_VALUE).minPrice(RULE_MIN_PRICE)
                .maxPrice(RULE_MAX_PRICE).priority(RULE_PRIORITY).active(RULE_ACTIVE)
                .createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT).build();
    }
}
