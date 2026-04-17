package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.service.PricingService;
import com.backandwhite.application.usecase.PriceRuleUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.repository.PriceRuleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class PriceRuleUseCaseImpl implements PriceRuleUseCase {

    private final PriceRuleRepository priceRuleRepository;
    private final PricingService pricingService;

    @Override
    @Transactional(readOnly = true)
    public List<PriceRule> findAll() {
        return priceRuleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PriceRule findById(String id) {
        return priceRuleRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("PriceRule", id));
    }

    @Override
    @Transactional
    public PriceRule create(PriceRule priceRule) {
        PriceRule saved = priceRuleRepository.save(priceRule);
        pricingService.invalidateCache();
        log.info("Price rule created: scope={}, scopeId={}, marginType={}, marginValue={}", saved.getScope(),
                saved.getScopeId(), saved.getMarginType(), saved.getMarginValue());
        return saved;
    }

    @Override
    @Transactional
    public PriceRule update(String id, PriceRule priceRule) {
        PriceRule updated = priceRuleRepository.update(id, priceRule);
        pricingService.invalidateCache();
        log.info("Price rule updated: id={}, scope={}, marginValue={}", id, updated.getScope(),
                updated.getMarginValue());
        return updated;
    }

    @Override
    @Transactional
    public void delete(String id) {
        priceRuleRepository.delete(id);
        pricingService.invalidateCache();
        log.info("Price rule deleted: id={}", id);
    }
}
