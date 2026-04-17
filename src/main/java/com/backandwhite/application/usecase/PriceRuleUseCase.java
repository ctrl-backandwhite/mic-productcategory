package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.PriceRule;
import java.util.List;

public interface PriceRuleUseCase {

    List<PriceRule> findAll();

    PriceRule findById(String id);

    PriceRule create(PriceRule priceRule);

    PriceRule update(String id, PriceRule priceRule);

    void delete(String id);
}
