package com.backandwhite.application.service;

import com.backandwhite.common.currency.CurrencyHolder;
import com.backandwhite.common.currency.CurrencyRateCache;
import com.backandwhite.common.domain.valueobject.Currency;
import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.repository.PriceRuleRepository;
import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pricing engine that resolves and applies margin rules to product prices.
 * <p>
 * Resolution order (most specific wins):
 * VARIANT → PRODUCT → CATEGORY → GLOBAL
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PriceRuleRepository priceRuleRepository;
    private final CurrencyRateCache currencyRateCache;

    // Simple in-memory cache (refreshed per request batch)
    private volatile List<PriceRule> cachedRules;
    private volatile long cacheTimestamp;
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Resolve the applicable margin rule for a given variant in context.
     * Supports price-range-based rules: if a rule has min/max price defined,
     * the cost price must fall within that range for the rule to match.
     */
    public Optional<PriceRule> resolveRule(String variantId, String productId, String categoryId) {
        return resolveRule(variantId, productId, categoryId, null);
    }

    /**
     * Resolve the applicable margin rule considering the cost price for range
     * matching.
     * Resolution order (most specific wins): VARIANT → PRODUCT → CATEGORY → GLOBAL.
     * Within each scope, a range-matching rule takes priority over a no-range
     * fallback.
     */
    public Optional<PriceRule> resolveRule(String variantId, String productId, String categoryId,
            BigDecimal costPrice) {
        List<PriceRule> rules = getActiveRules();

        // 1. VARIANT-level rule
        Optional<PriceRule> rule = findRuleWithRange(rules, PriceRuleScope.VARIANT, variantId, costPrice);
        if (rule.isPresent())
            return rule;

        // 2. PRODUCT-level rule
        rule = findRuleWithRange(rules, PriceRuleScope.PRODUCT, productId, costPrice);
        if (rule.isPresent())
            return rule;

        // 3. CATEGORY-level rule
        rule = findRuleWithRange(rules, PriceRuleScope.CATEGORY, categoryId, costPrice);
        if (rule.isPresent())
            return rule;

        // 4. GLOBAL rule
        return findRuleWithRange(rules, PriceRuleScope.GLOBAL, null, costPrice);
    }

    /**
     * Calculate the retail price by applying the margin to a cost price.
     */
    public Money applyMargin(Money costPrice, PriceRule rule) {
        if (costPrice == null || rule == null)
            return costPrice;

        if (rule.getMarginType() == MarginType.PERCENTAGE) {
            // retailPrice = costPrice × (1 + marginValue / 100)
            BigDecimal multiplier = BigDecimal.ONE.add(
                    rule.getMarginValue().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            return Money.of(costPrice.getAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP));
        } else {
            // FIXED: retailPrice = costPrice + marginValue
            return costPrice.add(Money.of(rule.getMarginValue()));
        }
    }

    /**
     * Apply margin to a variant's sell price, returning the retail price.
     * The original variantSellPrice is the cost price from CJ.
     * Uses cost price for range-based rule resolution.
     */
    public Money calculateRetailPrice(ProductDetailVariant variant, String productId, String categoryId) {
        if (variant.getVariantSellPrice() == null)
            return null;
        BigDecimal costAmount = variant.getVariantSellPrice().getAmount();
        Optional<PriceRule> rule = resolveRule(variant.getVid(), productId, categoryId, costAmount);
        return rule.map(r -> applyMargin(variant.getVariantSellPrice(), r)).orElse(variant.getVariantSellPrice());
    }

    /**
     * Apply margins to all variants of a product and recalculate the product-level
     * sellPrice range string based on the min/max retail prices.
     * After margins, converts all prices to the currency from X-Currency header.
     */
    public void applyMarginsToProduct(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            // For products without loaded variants, apply margin to the sellPrice string
            applyMarginToSellPriceString(product);
        } else {
            BigDecimal minRetail = null;
            BigDecimal maxRetail = null;

            for (ProductDetailVariant variant : product.getVariants()) {
                Money retailPrice = calculateRetailPrice(variant, product.getId(), product.getCategoryId());
                if (retailPrice != null) {
                    variant.setRetailPrice(retailPrice);

                    BigDecimal amount = retailPrice.getAmount();
                    if (minRetail == null || amount.compareTo(minRetail) < 0) {
                        minRetail = amount;
                    }
                    if (maxRetail == null || amount.compareTo(maxRetail) > 0) {
                        maxRetail = amount;
                    }
                }
            }

            // Recalculate product-level sellPrice as a range
            if (minRetail != null) {
                String costPrice = product.getSellPrice(); // keep original for reference
                product.setCostPrice(costPrice);

                if (maxRetail.compareTo(minRetail) == 0) {
                    product.setSellPrice(minRetail.toPlainString());
                } else {
                    product.setSellPrice(minRetail.toPlainString() + " -- " + maxRetail.toPlainString());
                }
            }
        }

        // Convert to requested currency (X-Currency header)
        convertProductToRequestCurrency(product);
    }

    /**
     * For products without variants loaded, parse the range string and apply the
     * matching rule (considering price range) to each part.
     */
    private void applyMarginToSellPriceString(Product product) {
        String raw = product.getSellPrice();
        if (raw == null || raw.isBlank())
            return;

        product.setCostPrice(raw);

        // Parse range: "0.78 -- 0.81" or "6.11" or "0.84-2.94"
        String[] parts = raw.split("\\s*--\\s*|\\s*-\\s*");
        StringBuilder retailRange = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            try {
                BigDecimal cost = new BigDecimal(parts[i].trim());
                Optional<PriceRule> rule = resolveRule(null, product.getId(), product.getCategoryId(), cost);
                if (i > 0)
                    retailRange.append(" -- ");
                if (rule.isPresent()) {
                    Money retailMoney = applyMargin(Money.of(cost), rule.get());
                    retailRange.append(retailMoney.toPlainString());
                } else {
                    retailRange.append(cost.toPlainString());
                }
            } catch (NumberFormatException e) {
                if (i > 0)
                    retailRange.append(" -- ");
                retailRange.append(parts[i].trim());
            }
        }

        product.setSellPrice(retailRange.toString());
    }

    /**
     * Invalidate the cache (called when rules are created/updated/deleted).
     */
    public void invalidateCache() {
        cachedRules = null;
        cacheTimestamp = 0;
    }

    // ── Currency conversion ─────────────────────────────────────────────────

    /**
     * Convert all prices in a product to the currency requested via X-Currency header.
     * If the header is absent or "USD", prices stay in USD (backward compatible).
     */
    private void convertProductToRequestCurrency(Product product) {
        String currencyCode = CurrencyHolder.get();
        Currency currency;
        try {
            currency = Currency.fromCode(currencyCode);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown currency '{}', defaulting to USD", currencyCode);
            currency = Currency.USD;
            currencyCode = "USD";
        }

        product.setCurrencyCode(currencyCode);
        product.setCurrencySymbol(currency.getSymbol());

        if ("USD".equals(currencyCode)) {
            // No conversion needed — set raw values from current prices
            setRawPricesUsd(product);
            return;
        }

        BigDecimal rate = currencyRateCache.getRate(currencyCode);

        // Convert product-level price strings
        product.setSellPrice(convertPriceString(product.getSellPrice(), rate));
        product.setCostPrice(convertPriceString(product.getCostPrice(), rate));

        // Set numeric raw values
        product.setSellPriceRaw(parseMinPrice(product.getSellPrice()));
        product.setCostPriceRaw(parseMinPrice(product.getCostPrice()));

        // Convert variant-level prices
        if (product.getVariants() != null) {
            for (ProductDetailVariant variant : product.getVariants()) {
                variant.setCurrencyCode(currencyCode);
                if (variant.getVariantSellPrice() != null) {
                    variant.setVariantSellPrice(variant.getVariantSellPrice().convertTo(currency, rate));
                }
                if (variant.getVariantSugSellPrice() != null) {
                    variant.setVariantSugSellPrice(variant.getVariantSugSellPrice().convertTo(currency, rate));
                }
                if (variant.getRetailPrice() != null) {
                    variant.setRetailPrice(variant.getRetailPrice().convertTo(currency, rate));
                }
            }
        }
    }

    /**
     * Set raw price BigDecimals from USD sellPrice/costPrice strings (no conversion).
     */
    private void setRawPricesUsd(Product product) {
        product.setSellPriceRaw(parseMinPrice(product.getSellPrice()));
        product.setCostPriceRaw(parseMinPrice(product.getCostPrice()));
        if (product.getVariants() != null) {
            for (ProductDetailVariant variant : product.getVariants()) {
                variant.setCurrencyCode("USD");
            }
        }
    }

    /**
     * Convert a price string like "12.50" or "12.50 -- 25.88" using the given rate.
     */
    private String convertPriceString(String priceString, BigDecimal rate) {
        if (priceString == null || priceString.isBlank()) return priceString;

        String[] parts = priceString.split("\\s*--\\s*|\\s*-\\s*");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append(" -- ");
            try {
                BigDecimal val = new BigDecimal(parts[i].trim());
                BigDecimal converted = val.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                result.append(converted.toPlainString());
            } catch (NumberFormatException e) {
                result.append(parts[i].trim());
            }
        }
        return result.toString();
    }

    /**
     * Parse the minimum (first) price from a price string.
     */
    private BigDecimal parseMinPrice(String priceString) {
        if (priceString == null || priceString.isBlank()) return null;
        String[] parts = priceString.split("\\s*--\\s*|\\s*-\\s*");
        try {
            return new BigDecimal(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private List<PriceRule> getActiveRules() {
        long now = System.currentTimeMillis();
        if (cachedRules == null || (now - cacheTimestamp) > CACHE_TTL_MS) {
            cachedRules = priceRuleRepository.findAllActive();
            cacheTimestamp = now;
        }
        return cachedRules;
    }

    private Optional<PriceRule> findRuleWithRange(List<PriceRule> rules, PriceRuleScope scope,
            String scopeId, BigDecimal costPrice) {
        // Filter rules matching scope + scopeId
        List<PriceRule> scopeRules = rules.stream()
                .filter(r -> r.getScope() == scope)
                .filter(r -> {
                    if (scope == PriceRuleScope.GLOBAL)
                        return r.getScopeId() == null;
                    return scopeId != null && scopeId.equals(r.getScopeId());
                })
                .toList();

        if (scopeRules.isEmpty())
            return Optional.empty();

        // If we have a cost price, try to find a range-matching rule first
        if (costPrice != null) {
            Optional<PriceRule> rangeMatch = scopeRules.stream()
                    .filter(r -> r.getMinPrice() != null || r.getMaxPrice() != null)
                    .filter(r -> {
                        boolean aboveMin = r.getMinPrice() == null
                                || costPrice.compareTo(r.getMinPrice()) >= 0;
                        boolean belowMax = r.getMaxPrice() == null
                                || costPrice.compareTo(r.getMaxPrice()) <= 0;
                        return aboveMin && belowMax;
                    })
                    .max(java.util.Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : 0));

            if (rangeMatch.isPresent())
                return rangeMatch;
        }

        // Fallback: rule without price range (min/max both null)
        return scopeRules.stream()
                .filter(r -> r.getMinPrice() == null && r.getMaxPrice() == null)
                .max(java.util.Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : 0));
    }
}
