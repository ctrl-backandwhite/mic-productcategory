package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import java.util.List;
import java.util.Optional;

public interface PriceRuleRepository {

    List<PriceRule> findAll();

    List<PriceRule> findAllActive();

    Optional<PriceRule> findById(String id);

    Optional<PriceRule> findByScopeAndScopeId(PriceRuleScope scope, String scopeId);

    PriceRule save(PriceRule priceRule);

    PriceRule update(String id, PriceRule priceRule);

    void delete(String id);
}
