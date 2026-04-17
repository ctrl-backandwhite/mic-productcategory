package com.backandwhite.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.backandwhite.common.currency.CurrencyHolder;
import com.backandwhite.common.currency.CurrencyRateCache;
import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.repository.PriceRuleRepository;
import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PriceRuleRepository priceRuleRepository;

    @Mock
    private CurrencyRateCache currencyRateCache;

    @InjectMocks
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        CurrencyHolder.set("USD");
    }

    @AfterEach
    void tearDown() {
        CurrencyHolder.clear();
        pricingService.invalidateCache();
    }

    // ── applyMargin ──────────────────────────────────────────────────────────

    @Nested
    class ApplyMargin {

        @Test
        void percentageMargin_appliesCorrectly() {
            Money cost = Money.of(new BigDecimal("100.00"));
            PriceRule rule = PriceRule.builder().marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("25.00"))
                    .build();

            Money result = pricingService.applyMargin(cost, rule);

            assertThat(result.getAmount()).isEqualByComparingTo("125.00");
        }

        @Test
        void fixedMargin_appliesCorrectly() {
            Money cost = Money.of(new BigDecimal("50.00"));
            PriceRule rule = PriceRule.builder().marginType(MarginType.FIXED).marginValue(new BigDecimal("10.00"))
                    .build();

            Money result = pricingService.applyMargin(cost, rule);

            assertThat(result.getAmount()).isEqualByComparingTo("60.00");
        }

        @Test
        void nullCostPrice_returnsNull() {
            PriceRule rule = PriceRule.builder().marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("25.00"))
                    .build();

            Money result = pricingService.applyMargin(null, rule);

            assertThat(result).isNull();
        }

        @Test
        void nullRule_returnsCostPrice() {
            Money cost = Money.of(new BigDecimal("50.00"));

            Money result = pricingService.applyMargin(cost, null);

            assertThat(result).isEqualTo(cost);
        }

        @Test
        void percentageMargin_roundsToTwoDecimals() {
            Money cost = Money.of(new BigDecimal("33.33"));
            PriceRule rule = PriceRule.builder().marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("33.33"))
                    .build();

            Money result = pricingService.applyMargin(cost, rule);

            assertThat(result.getAmount().scale()).isEqualTo(2);
        }
    }

    // ── resolveRule ──────────────────────────────────────────────────────────

    @Nested
    class ResolveRule {

        @Test
        void variantRule_takesHighestPriority() {
            PriceRule variantRule = PriceRule.builder().scope(PriceRuleScope.VARIANT).scopeId("v1")
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("10")).priority(0).active(true)
                    .build();
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("25")).priority(0).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(variantRule, globalRule));

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1");

            assertThat(result).isPresent();
            assertThat(result.get().getScope()).isEqualTo(PriceRuleScope.VARIANT);
        }

        @Test
        void productRule_whenNoVariantRule() {
            PriceRule productRule = PriceRule.builder().scope(PriceRuleScope.PRODUCT).scopeId("p1")
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("15")).priority(0).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(productRule));

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1");

            assertThat(result).isPresent();
            assertThat(result.get().getScope()).isEqualTo(PriceRuleScope.PRODUCT);
        }

        @Test
        void categoryRule_whenNoVariantOrProductRule() {
            PriceRule categoryRule = PriceRule.builder().scope(PriceRuleScope.CATEGORY).scopeId("c1")
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("20")).priority(0).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(categoryRule));

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1");

            assertThat(result).isPresent();
            assertThat(result.get().getScope()).isEqualTo(PriceRuleScope.CATEGORY);
        }

        @Test
        void globalRule_asFallback() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("25")).priority(0).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1");

            assertThat(result).isPresent();
            assertThat(result.get().getScope()).isEqualTo(PriceRuleScope.GLOBAL);
        }

        @Test
        void noRules_returnsEmpty() {
            when(priceRuleRepository.findAllActive()).thenReturn(List.of());

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1");

            assertThat(result).isEmpty();
        }

        @Test
        void rangeMatchingRule_takesPriorityOverNoRange() {
            PriceRule noRange = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("10")).priority(0).active(true)
                    .build();
            PriceRule withRange = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("30"))
                    .minPrice(new BigDecimal("5.00")).maxPrice(new BigDecimal("50.00")).priority(1).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(noRange, withRange));

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1", new BigDecimal("10.00"));

            assertThat(result).isPresent();
            assertThat(result.get().getMarginValue()).isEqualByComparingTo("30");
        }

        @Test
        void rangeMatchingRule_costOutsideRange_fallsBackToNoRange() {
            PriceRule noRange = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("10")).priority(0).active(true)
                    .build();
            PriceRule withRange = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("30"))
                    .minPrice(new BigDecimal("5.00")).maxPrice(new BigDecimal("50.00")).priority(1).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(noRange, withRange));

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1", new BigDecimal("100.00"));

            assertThat(result).isPresent();
            assertThat(result.get().getMarginValue()).isEqualByComparingTo("10");
        }

        @Test
        void resolveRuleOverload_delegatesToFourArgMethod() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("25")).priority(0).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            Optional<PriceRule> result = pricingService.resolveRule("v1", "p1", "c1");

            assertThat(result).isPresent();
        }
    }

    // ── calculateRetailPrice ─────────────────────────────────────────────────

    @Nested
    class CalculateRetailPrice {

        @Test
        void appliesMarginToVariant() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("50")).priority(0).active(true)
                    .build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            ProductDetailVariant variant = new ProductDetailVariant();
            variant.setVid("v1");
            variant.setVariantSellPrice(Money.of(new BigDecimal("10.00")));

            Money result = pricingService.calculateRetailPrice(variant, "p1", "c1");

            assertThat(result.getAmount()).isEqualByComparingTo("15.00");
        }

        @Test
        void nullSellPrice_returnsNull() {
            ProductDetailVariant variant = new ProductDetailVariant();
            variant.setVid("v1");
            variant.setVariantSellPrice(null);

            Money result = pricingService.calculateRetailPrice(variant, "p1", "c1");

            assertThat(result).isNull();
        }

        @Test
        void noMatchingRule_returnsCostPrice() {
            when(priceRuleRepository.findAllActive()).thenReturn(List.of());

            ProductDetailVariant variant = new ProductDetailVariant();
            variant.setVid("v1");
            variant.setVariantSellPrice(Money.of(new BigDecimal("10.00")));

            Money result = pricingService.calculateRetailPrice(variant, "p1", "c1");

            assertThat(result.getAmount()).isEqualByComparingTo("10.00");
        }
    }

    // ── applyMarginsToProduct ────────────────────────────────────────────────

    @Nested
    class ApplyMarginsToProduct {

        @Test
        void withVariants_recalculatesSellPriceRange() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("100")).priority(0).active(true)
                    .build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            ProductDetailVariant v1 = new ProductDetailVariant();
            v1.setVid("v1");
            v1.setVariantSellPrice(Money.of(new BigDecimal("10.00")));

            ProductDetailVariant v2 = new ProductDetailVariant();
            v2.setVid("v2");
            v2.setVariantSellPrice(Money.of(new BigDecimal("20.00")));

            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("10.00 -- 20.00");
            product.setVariants(new ArrayList<>(List.of(v1, v2)));

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getSellPrice()).isEqualTo("20.00 -- 40.00");
            assertThat(product.getCostPrice()).isEqualTo("10.00 -- 20.00");
        }

        @Test
        void withVariants_singleVariant_noRangeString() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.FIXED).marginValue(new BigDecimal("5.00")).priority(0).active(true).build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            ProductDetailVariant v1 = new ProductDetailVariant();
            v1.setVid("v1");
            v1.setVariantSellPrice(Money.of(new BigDecimal("10.00")));

            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("10.00");
            product.setVariants(new ArrayList<>(List.of(v1)));

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getSellPrice()).isEqualTo("15.00");
        }

        @Test
        void withoutVariants_parsesRange() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("100")).priority(0).active(true)
                    .build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("5.00 -- 10.00");
            product.setVariants(null);

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getCostPrice()).isEqualTo("5.00 -- 10.00");
            assertThat(product.getSellPrice()).isEqualTo("10.00 -- 20.00");
        }

        @Test
        void withoutVariants_singlePrice() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.FIXED).marginValue(new BigDecimal("2.00")).priority(0).active(true).build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("8.00");
            product.setVariants(null);

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getSellPrice()).isEqualTo("10.00");
        }

        @Test
        void withoutVariants_nullSellPrice_noChange() {
            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice(null);
            product.setVariants(null);

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getSellPrice()).isNull();
        }

        @Test
        void withoutVariants_blankSellPrice_noChange() {
            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("  ");
            product.setVariants(null);

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getSellPrice()).isEqualTo("  ");
        }

        @Test
        void emptyVariantList_parsesRange() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("50")).priority(0).active(true)
                    .build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("10.00");
            product.setVariants(List.of());

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getSellPrice()).isEqualTo("15.00");
        }
    }

    // ── applyMarginsToProductDetail ──────────────────────────────────────────

    @Nested
    class ApplyMarginsToProductDetail {

        @Test
        void withVariants_recalculatesPriceRange() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("100")).priority(0).active(true)
                    .build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            ProductDetailVariant v1 = new ProductDetailVariant();
            v1.setVid("v1");
            v1.setVariantSellPrice(Money.of(new BigDecimal("5.00")));

            ProductDetail detail = new ProductDetail();
            detail.setPid("p1");
            detail.setCategoryId("c1");
            detail.setSellPrice("5.00");
            detail.setVariants(new ArrayList<>(List.of(v1)));

            pricingService.applyMarginsToProductDetail(detail);

            assertThat(detail.getSellPrice()).isEqualTo("10.00");
        }

        @Test
        void withoutVariants_parsesRange() {
            PriceRule globalRule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.FIXED).marginValue(new BigDecimal("3.00")).priority(0).active(true).build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(globalRule));

            ProductDetail detail = new ProductDetail();
            detail.setPid("p1");
            detail.setCategoryId("c1");
            detail.setSellPrice("7.00 -- 12.00");
            detail.setVariants(null);

            pricingService.applyMarginsToProductDetail(detail);

            assertThat(detail.getCostPrice()).isEqualTo("7.00 -- 12.00");
            assertThat(detail.getSellPrice()).isEqualTo("10.00 -- 15.00");
        }

        @Test
        void withoutVariants_nullSellPrice_noChange() {
            ProductDetail detail = new ProductDetail();
            detail.setPid("p1");
            detail.setCategoryId("c1");
            detail.setSellPrice(null);
            detail.setVariants(null);

            pricingService.applyMarginsToProductDetail(detail);

            assertThat(detail.getSellPrice()).isNull();
        }
    }

    // ── invalidateCache ──────────────────────────────────────────────────────

    @Nested
    class CacheManagement {

        @Test
        void invalidateCache_clearsCache() {
            PriceRule rule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("10")).priority(0).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(rule));

            // First call loads cache
            pricingService.resolveRule("v1", "p1", "c1");
            // Invalidate
            pricingService.invalidateCache();
            // Second call should reload
            pricingService.resolveRule("v1", "p1", "c1");

            verify(priceRuleRepository, times(2)).findAllActive();
        }

        @Test
        void cachedRulesReused_withinTtl() {
            PriceRule rule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null)
                    .marginType(MarginType.PERCENTAGE).marginValue(new BigDecimal("10")).priority(0).active(true)
                    .build();

            when(priceRuleRepository.findAllActive()).thenReturn(List.of(rule));

            pricingService.resolveRule("v1", "p1", "c1");
            pricingService.resolveRule("v1", "p1", "c1");

            verify(priceRuleRepository, times(1)).findAllActive();
        }
    }

    // ── Currency conversion ──────────────────────────────────────────────────

    @Nested
    class CurrencyConversion {

        @Test
        void usdCurrency_noConversion() {
            CurrencyHolder.set("USD");
            when(priceRuleRepository.findAllActive()).thenReturn(List.of());

            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("10.00");
            product.setVariants(null);

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getCurrencyCode()).isEqualTo("USD");
            assertThat(product.getCurrencySymbol()).isEqualTo("$");
        }

        @Test
        void eurCurrency_convertsUsingRate() {
            CurrencyHolder.set("EUR");
            when(currencyRateCache.getRate("EUR")).thenReturn(new BigDecimal("0.92"));

            PriceRule rule = PriceRule.builder().scope(PriceRuleScope.GLOBAL).scopeId(null).marginType(MarginType.FIXED)
                    .marginValue(new BigDecimal("0.00")).priority(0).active(true).build();
            when(priceRuleRepository.findAllActive()).thenReturn(List.of(rule));

            Product product = new Product();
            product.setId("p1");
            product.setCategoryId("c1");
            product.setSellPrice("100.00");
            product.setVariants(null);

            pricingService.applyMarginsToProduct(product);

            assertThat(product.getCurrencyCode()).isEqualTo("EUR");
            assertThat(product.getSellPrice()).isEqualTo("92.00");
        }
    }
}
