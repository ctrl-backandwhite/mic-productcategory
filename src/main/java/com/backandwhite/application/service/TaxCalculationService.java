package com.backandwhite.application.service;

import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.domain.repository.CountryTaxRepository;
import com.backandwhite.domain.valueobject.TaxType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Tax calculation engine that resolves country-specific tax rules and computes
 * the tax amount for a given subtotal.
 * <p>
 * Resolution order (most specific wins): 1. Rules matching country + region (if
 * region specified) 2. Rules matching country only (region is null)
 * <p>
 * Multiple rates may apply if different categories have different rates.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private final CountryTaxRepository countryTaxRepository;

    /**
     * Calculate tax for a given subtotal, country, and optional state/region.
     */
    public TaxCalculationResult calculate(BigDecimal subtotal, String countryCode, String state) {
        List<CountryTax> countryRules = countryTaxRepository.findActiveByCountryCode(countryCode);

        if (countryRules.isEmpty()) {
            log.debug("No tax rules found for country={}, returning zero tax", countryCode);
            return TaxCalculationResult.builder().subtotal(subtotal)
                    .taxAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)).total(subtotal)
                    .appliedRates(List.of()).build();
        }

        // If state is provided, look for region-specific rules first
        List<CountryTax> applicableRules;
        if (state != null && !state.isBlank()) {
            applicableRules = countryRules.stream().filter(r -> state.equalsIgnoreCase(r.getRegion())).toList();
            // Fall back to country-level rules if no region match
            if (applicableRules.isEmpty()) {
                applicableRules = countryRules.stream().filter(r -> r.getRegion() == null || r.getRegion().isBlank())
                        .toList();
            }
        } else {
            applicableRules = countryRules.stream().filter(r -> r.getRegion() == null || r.getRegion().isBlank())
                    .toList();
        }

        // Use the "General" rule if available, otherwise the first one
        CountryTax primaryRule = applicableRules.stream()
                .filter(r -> r.getAppliesTo() == null || r.getAppliesTo().contains("General")).findFirst()
                .orElse(applicableRules.isEmpty() ? null : applicableRules.getFirst());

        if (primaryRule == null) {
            return TaxCalculationResult.builder().subtotal(subtotal)
                    .taxAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)).total(subtotal)
                    .appliedRates(List.of()).build();
        }

        BigDecimal taxAmount;
        if (primaryRule.getType() == TaxType.PERCENTAGE) {
            taxAmount = subtotal.multiply(primaryRule.getRate()).setScale(2, RoundingMode.HALF_UP);
        } else {
            taxAmount = primaryRule.getRate().setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = subtotal.add(taxAmount);

        String rateName = buildRateName(primaryRule);
        List<AppliedRate> appliedRates = new ArrayList<>();
        appliedRates.add(AppliedRate.builder().name(rateName).rate(primaryRule.getRate().doubleValue())
                .amount(taxAmount.doubleValue()).build());

        log.debug("Tax calculated: country={}, subtotal={}, taxAmount={}, total={}", countryCode, subtotal, taxAmount,
                total);

        return TaxCalculationResult.builder().subtotal(subtotal).taxAmount(taxAmount).total(total)
                .appliedRates(appliedRates).build();
    }

    private String buildRateName(CountryTax tax) {
        BigDecimal pct = tax.getRate().multiply(BigDecimal.valueOf(100));
        String pctStr = pct.stripTrailingZeros().toPlainString();
        String category = tax.getAppliesTo() != null ? tax.getAppliesTo() : "General";
        return String.format("IVA %s %s%%", category, pctStr);
    }

    // ── Result types ──────────────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TaxCalculationResult {
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal total;
        private List<AppliedRate> appliedRates;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AppliedRate {
        private String name;
        private double rate;
        private double amount;
    }
}
