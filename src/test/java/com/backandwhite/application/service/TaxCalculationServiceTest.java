package com.backandwhite.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.domain.repository.CountryTaxRepository;
import com.backandwhite.domain.valueobject.TaxType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxCalculationServiceTest {

    @Mock
    private CountryTaxRepository countryTaxRepository;

    @InjectMocks
    private TaxCalculationService taxCalculationService;

    @Test
    void calculate_noRules_zeroTax() {
        when(countryTaxRepository.findActiveByCountryCode("US")).thenReturn(List.of());

        var result = taxCalculationService.calculate(new BigDecimal("100.00"), "US", null);

        assertThat(result.getTaxAmount()).isEqualByComparingTo("0.00");
        assertThat(result.getTotal()).isEqualByComparingTo("100.00");
        assertThat(result.getAppliedRates()).isEmpty();
    }

    @Test
    void calculate_percentageTax_correctAmount() {
        CountryTax tax = CountryTax.builder().countryCode("ES").rate(new BigDecimal("0.21")).type(TaxType.PERCENTAGE)
                .appliesTo("General").build();
        when(countryTaxRepository.findActiveByCountryCode("ES")).thenReturn(List.of(tax));

        var result = taxCalculationService.calculate(new BigDecimal("100.00"), "ES", null);

        assertThat(result.getTaxAmount()).isEqualByComparingTo("21.00");
        assertThat(result.getTotal()).isEqualByComparingTo("121.00");
        assertThat(result.getAppliedRates()).hasSize(1);
    }

    @Test
    void calculate_fixedTax_correctAmount() {
        CountryTax tax = CountryTax.builder().countryCode("US").rate(new BigDecimal("5.00")).type(TaxType.FIXED)
                .appliesTo("General").build();
        when(countryTaxRepository.findActiveByCountryCode("US")).thenReturn(List.of(tax));

        var result = taxCalculationService.calculate(new BigDecimal("100.00"), "US", null);

        assertThat(result.getTaxAmount()).isEqualByComparingTo("5.00");
        assertThat(result.getTotal()).isEqualByComparingTo("105.00");
    }

    @Test
    void calculate_regionSpecificRule_preferred() {
        CountryTax countryRule = CountryTax.builder().countryCode("US").region(null).rate(new BigDecimal("0.05"))
                .type(TaxType.PERCENTAGE).appliesTo("General").build();
        CountryTax regionRule = CountryTax.builder().countryCode("US").region("CA").rate(new BigDecimal("0.0725"))
                .type(TaxType.PERCENTAGE).appliesTo("General").build();
        when(countryTaxRepository.findActiveByCountryCode("US")).thenReturn(List.of(countryRule, regionRule));

        var result = taxCalculationService.calculate(new BigDecimal("100.00"), "US", "CA");

        assertThat(result.getTaxAmount()).isEqualByComparingTo("7.25");
    }

    @Test
    void calculate_regionNotFound_fallsBackToCountry() {
        CountryTax countryRule = CountryTax.builder().countryCode("US").region(null).rate(new BigDecimal("0.05"))
                .type(TaxType.PERCENTAGE).appliesTo("General").build();
        when(countryTaxRepository.findActiveByCountryCode("US")).thenReturn(List.of(countryRule));

        var result = taxCalculationService.calculate(new BigDecimal("100.00"), "US", "UNKNOWN");

        assertThat(result.getTaxAmount()).isEqualByComparingTo("5.00");
    }

    @Test
    void calculate_blankState_usesCountryRules() {
        CountryTax countryRule = CountryTax.builder().countryCode("ES").region(null).rate(new BigDecimal("0.21"))
                .type(TaxType.PERCENTAGE).appliesTo("General").build();
        when(countryTaxRepository.findActiveByCountryCode("ES")).thenReturn(List.of(countryRule));

        var result = taxCalculationService.calculate(new BigDecimal("200.00"), "ES", "  ");

        assertThat(result.getTaxAmount()).isEqualByComparingTo("42.00");
    }

    @Test
    void calculate_noMatchingGeneralRule_usesFirstRule() {
        CountryTax tax = CountryTax.builder().countryCode("MX").region(null).rate(new BigDecimal("0.16"))
                .type(TaxType.PERCENTAGE).appliesTo("Electronics").build();
        when(countryTaxRepository.findActiveByCountryCode("MX")).thenReturn(List.of(tax));

        var result = taxCalculationService.calculate(new BigDecimal("50.00"), "MX", null);

        assertThat(result.getTaxAmount()).isEqualByComparingTo("8.00");
    }

    @Test
    void calculate_appliedRateFormat() {
        CountryTax tax = CountryTax.builder().countryCode("ES").rate(new BigDecimal("0.21")).type(TaxType.PERCENTAGE)
                .appliesTo("General").build();
        when(countryTaxRepository.findActiveByCountryCode("ES")).thenReturn(List.of(tax));

        var result = taxCalculationService.calculate(new BigDecimal("100.00"), "ES", null);

        assertThat(result.getAppliedRates().getFirst().getName()).contains("IVA");
        assertThat(result.getAppliedRates().getFirst().getRate()).isEqualTo(0.21);
    }
}
