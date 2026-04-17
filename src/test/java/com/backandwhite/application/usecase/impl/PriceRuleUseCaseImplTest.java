package com.backandwhite.application.usecase.impl;

import static com.backandwhite.provider.PriceRuleProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.service.PricingService;
import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.repository.PriceRuleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceRuleUseCaseImplTest {

    @Mock
    private PriceRuleRepository priceRuleRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private PriceRuleUseCaseImpl priceRuleUseCase;

    @Test
    void findAll_returnsList() {
        List<PriceRule> rules = List.of(priceRule());
        when(priceRuleRepository.findAll()).thenReturn(rules);

        List<PriceRule> result = priceRuleUseCase.findAll();

        assertThat(result).hasSize(1);
        assertSame(rules, result);
    }

    @Test
    void findById_existing_returnsPriceRule() {
        when(priceRuleRepository.findById(RULE_ID)).thenReturn(Optional.of(priceRule()));

        PriceRule result = priceRuleUseCase.findById(RULE_ID);

        assertThat(result.getId()).isEqualTo(RULE_ID);
    }

    @Test
    void findById_missing_throwsEntityNotFoundException() {
        when(priceRuleRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> priceRuleUseCase.findById("non-existent"));
    }

    @Test
    void create_savesAndInvalidatesCache() {
        PriceRule model = priceRule();
        when(priceRuleRepository.save(model)).thenReturn(model);

        PriceRule result = priceRuleUseCase.create(model);

        assertSame(model, result);
        verify(priceRuleRepository).save(model);
        verify(pricingService).invalidateCache();
    }

    @Test
    void update_delegatesAndInvalidatesCache() {
        PriceRule model = priceRule();
        when(priceRuleRepository.update(RULE_ID, model)).thenReturn(model);

        PriceRule result = priceRuleUseCase.update(RULE_ID, model);

        assertSame(model, result);
        verify(priceRuleRepository).update(RULE_ID, model);
        verify(pricingService).invalidateCache();
    }

    @Test
    void delete_delegatesAndInvalidatesCache() {
        priceRuleUseCase.delete(RULE_ID);

        verify(priceRuleRepository).delete(RULE_ID);
        verify(pricingService).invalidateCache();
    }
}
