package com.backandwhite.api.controller;

import static com.backandwhite.provider.PriceRuleProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.in.PriceRuleDtoIn;
import com.backandwhite.api.dto.out.PriceRuleDtoOut;
import com.backandwhite.api.mapper.PriceRuleApiMapper;
import com.backandwhite.application.usecase.PriceRuleUseCase;
import com.backandwhite.domain.model.PriceRule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PriceRuleControllerTest {

    @Mock
    private PriceRuleUseCase priceRuleUseCase;

    @Mock
    private PriceRuleApiMapper mapper;

    @InjectMocks
    private PriceRuleController controller;

    @Test
    void findAll_returnsRuleList() {
        List<PriceRule> rules = List.of(priceRule());
        List<PriceRuleDtoOut> dtoOuts = List.of(priceRuleDtoOut());

        when(priceRuleUseCase.findAll()).thenReturn(rules);
        when(mapper.toDtoList(rules)).thenReturn(dtoOuts);

        ResponseEntity<List<PriceRuleDtoOut>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOuts);
        verify(priceRuleUseCase).findAll();
        verify(mapper).toDtoList(rules);
    }

    @Test
    void findById_returnsRule() {
        PriceRule model = priceRule();
        PriceRuleDtoOut dtoOut = priceRuleDtoOut();

        when(priceRuleUseCase.findById(RULE_ID)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<PriceRuleDtoOut> response = controller.findById(RULE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(priceRuleUseCase).findById(RULE_ID);
        verify(mapper).toDto(model);
    }

    @Test
    void create_returnsCreatedRule() {
        PriceRuleDtoIn dtoIn = priceRuleDtoIn();
        PriceRule model = priceRule();
        PriceRuleDtoOut dtoOut = priceRuleDtoOut();

        when(mapper.toDomain(dtoIn)).thenReturn(model);
        when(priceRuleUseCase.create(model)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<PriceRuleDtoOut> response = controller.create(dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(mapper).toDomain(dtoIn);
        verify(priceRuleUseCase).create(model);
        verify(mapper).toDto(model);
    }

    @Test
    void update_returnsUpdatedRule() {
        PriceRuleDtoIn dtoIn = priceRuleDtoIn();
        PriceRule model = priceRule();
        PriceRuleDtoOut dtoOut = priceRuleDtoOut();

        when(mapper.toDomain(dtoIn)).thenReturn(model);
        when(priceRuleUseCase.update(RULE_ID, model)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(dtoOut);

        ResponseEntity<PriceRuleDtoOut> response = controller.update(RULE_ID, dtoIn);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dtoOut);
        verify(mapper).toDomain(dtoIn);
        verify(priceRuleUseCase).update(RULE_ID, model);
        verify(mapper).toDto(model);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(RULE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(priceRuleUseCase).delete(RULE_ID);
    }
}
