package com.backandwhite.application.service;

import com.backandwhite.common.currency.CurrencyHolder;
import com.backandwhite.common.currency.CurrencyRateCache;
import com.backandwhite.common.domain.valueobject.Currency;
import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.PriceRule;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.repository.PriceRuleRepository;
import com.backandwhite.domain.valueobject.MarginType;
import com.backandwhite.domain.valueobject.PriceRuleScope;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Pricing engine that resolves and applies margin rules to product prices.
 * <p>
 * Resolution order (most specific wins): VARIANT → PRODUCT → CATEGORY → GLOBAL
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class PricingService {

    private static final String PRICE_RANGE_SEPARATOR = "\\s*+-{1,2}\\s*+";
    private static final String RANGE_JOINER = " -- ";
    private static final String USD = "USD";
    private static final long CACHE_TTL_MS = 5L * 60 * 1000;

    private final PriceRuleRepository priceRuleRepository;
    private final CurrencyRateCache currencyRateCache;

    private List<PriceRule> cachedRules;
    private long cacheTimestamp;

    public Optional<PriceRule> resolveRule(String variantId, String productId, String categoryId) {
        return resolveRule(variantId, productId, categoryId, null);
    }

    public Optional<PriceRule> resolveRule(String variantId, String productId, String categoryId,
            BigDecimal costPrice) {
        List<PriceRule> rules = getActiveRules();

        Optional<PriceRule> rule = findRuleWithRange(rules, PriceRuleScope.VARIANT, variantId, costPrice);
        if (rule.isPresent())
            return rule;

        rule = findRuleWithRange(rules, PriceRuleScope.PRODUCT, productId, costPrice);
        if (rule.isPresent())
            return rule;

        rule = findRuleWithRange(rules, PriceRuleScope.CATEGORY, categoryId, costPrice);
        if (rule.isPresent())
            return rule;

        return findRuleWithRange(rules, PriceRuleScope.GLOBAL, null, costPrice);
    }

    public Money applyMargin(Money costPrice, PriceRule rule) {
        if (costPrice == null || rule == null)
            return costPrice;

        if (rule.getMarginType() == MarginType.PERCENTAGE) {
            BigDecimal multiplier = BigDecimal.ONE
                    .add(rule.getMarginValue().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            return Money.of(costPrice.getAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP));
        }
        return costPrice.add(Money.of(rule.getMarginValue()));
    }

    public Money calculateRetailPrice(ProductDetailVariant variant, String productId, String categoryId) {
        if (variant.getVariantSellPrice() == null)
            return null;
        BigDecimal costAmount = variant.getVariantSellPrice().getAmount();
        Optional<PriceRule> rule = resolveRule(variant.getVid(), productId, categoryId, costAmount);
        return rule.map(r -> applyMargin(variant.getVariantSellPrice(), r)).orElse(variant.getVariantSellPrice());
    }

    public void applyMarginsToProduct(Product product) {
        applyMarginsToVariants(product.getVariants(), product.getId(), product.getCategoryId(), product::getSellPrice,
                product::setSellPrice, product::setCostPrice, () -> applyMarginToSellPriceString(product));
        convertProductToRequestCurrency(product);
    }

    public void applyMarginsToProductDetail(ProductDetail detail) {
        applyMarginsToVariants(detail.getVariants(), detail.getPid(), detail.getCategoryId(), detail::getSellPrice,
                detail::setSellPrice, detail::setCostPrice, () -> applyMarginToSellPriceStringDetail(detail));
        convertProductDetailToRequestCurrency(detail);
    }

    public void invalidateCache() {
        cachedRules = null;
        cacheTimestamp = 0;
    }

    // java:S107 (too many parameters) suppressed: this helper unifies the
    // Product/ProductDetail margin application flow which needs the entity's
    // sell/cost price accessors and a no-variants fallback.
    @SuppressWarnings("java:S107")
    private void applyMarginsToVariants(List<ProductDetailVariant> variants, String entityId, String categoryId,
            java.util.function.Supplier<String> sellPriceGetter, java.util.function.Consumer<String> sellPriceSetter,
            java.util.function.Consumer<String> costPriceSetter, Runnable noVariantsFallback) {
        if (variants == null || variants.isEmpty()) {
            noVariantsFallback.run();
            return;
        }

        BigDecimal[] range = computeVariantRange(variants, entityId, categoryId);
        BigDecimal minRetail = range[0];
        BigDecimal maxRetail = range[1];

        if (minRetail != null) {
            costPriceSetter.accept(sellPriceGetter.get());
            sellPriceSetter.accept(minRetail.compareTo(maxRetail) == 0
                    ? minRetail.toPlainString()
                    : minRetail.toPlainString() + RANGE_JOINER + maxRetail.toPlainString());
        }
    }

    private BigDecimal[] computeVariantRange(List<ProductDetailVariant> variants, String entityId, String categoryId) {
        BigDecimal minRetail = null;
        BigDecimal maxRetail = null;
        for (ProductDetailVariant variant : variants) {
            Money retailPrice = calculateRetailPrice(variant, entityId, categoryId);
            if (retailPrice == null) {
                continue;
            }
            variant.setRetailPrice(retailPrice);
            BigDecimal amount = retailPrice.getAmount();
            if (minRetail == null || amount.compareTo(minRetail) < 0) {
                minRetail = amount;
            }
            if (maxRetail == null || amount.compareTo(maxRetail) > 0) {
                maxRetail = amount;
            }
        }
        return new BigDecimal[]{minRetail, maxRetail};
    }

    private void applyMarginToSellPriceStringDetail(ProductDetail detail) {
        String raw = detail.getSellPrice();
        if (raw == null || raw.isBlank())
            return;
        detail.setCostPrice(raw);
        detail.setSellPrice(applyMarginToRangeString(raw, null, detail.getPid(), detail.getCategoryId()));
    }

    private void applyMarginToSellPriceString(Product product) {
        String raw = product.getSellPrice();
        if (raw == null || raw.isBlank())
            return;
        product.setCostPrice(raw);
        product.setSellPrice(applyMarginToRangeString(raw, null, product.getId(), product.getCategoryId()));
    }

    private String applyMarginToRangeString(String raw, String variantId, String productId, String categoryId) {
        String[] parts = raw.split(PRICE_RANGE_SEPARATOR);
        StringBuilder retailRange = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0)
                retailRange.append(RANGE_JOINER);
            String token = parts[i].trim();
            BigDecimal cost = parseDecimal(token);
            if (cost == null) {
                retailRange.append(token);
                continue;
            }
            Optional<PriceRule> rule = resolveRule(variantId, productId, categoryId, cost);
            retailRange.append(
                    rule.map(r -> applyMargin(Money.of(cost), r).toPlainString()).orElseGet(cost::toPlainString));
        }
        return retailRange.toString();
    }

    private void convertProductToRequestCurrency(Product product) {
        CurrencyResolution res = resolveCurrency();
        product.setCurrencyCode(res.code());
        product.setCurrencySymbol(res.currency().getSymbol());

        if (USD.equals(res.code())) {
            setRawPricesUsd(product);
            return;
        }
        product.setSellPrice(convertPriceString(product.getSellPrice(), res.rate()));
        product.setCostPrice(convertPriceString(product.getCostPrice(), res.rate()));
        product.setSellPriceRaw(parseMinPrice(product.getSellPrice()));
        product.setCostPriceRaw(parseMinPrice(product.getCostPrice()));
        convertVariantsTo(product.getVariants(), res.currency(), res.code(), res.rate());
    }

    private void convertProductDetailToRequestCurrency(ProductDetail detail) {
        CurrencyResolution res = resolveCurrency();
        detail.setCurrencyCode(res.code());
        detail.setCurrencySymbol(res.currency().getSymbol());

        if (USD.equals(res.code())) {
            setRawPricesUsd(detail);
            return;
        }
        detail.setSellPrice(convertPriceString(detail.getSellPrice(), res.rate()));
        detail.setCostPrice(convertPriceString(detail.getCostPrice(), res.rate()));
        detail.setSuggestSellPrice(convertPriceString(detail.getSuggestSellPrice(), res.rate()));
        detail.setSellPriceRaw(parseMinPrice(detail.getSellPrice()));
        detail.setCostPriceRaw(parseMinPrice(detail.getCostPrice()));
        convertVariantsTo(detail.getVariants(), res.currency(), res.code(), res.rate());
    }

    private CurrencyResolution resolveCurrency() {
        String currencyCode = CurrencyHolder.get();
        Currency currency;
        try {
            currency = Currency.fromCode(currencyCode);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown currency '{}' ({}); defaulting to USD", currencyCode, ex.getMessage());
            currency = Currency.USD;
            currencyCode = USD;
        }
        BigDecimal rate = USD.equals(currencyCode) ? BigDecimal.ONE : currencyRateCache.getRate(currencyCode);
        return new CurrencyResolution(currency, currencyCode, rate);
    }

    private void convertVariantsTo(List<ProductDetailVariant> variants, Currency currency, String currencyCode,
            BigDecimal rate) {
        if (variants == null)
            return;
        for (ProductDetailVariant variant : variants) {
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

    private void setRawPricesUsd(Product product) {
        product.setSellPriceRaw(parseMinPrice(product.getSellPrice()));
        product.setCostPriceRaw(parseMinPrice(product.getCostPrice()));
        setVariantsUsd(product.getVariants());
    }

    private void setRawPricesUsd(ProductDetail detail) {
        detail.setSellPriceRaw(parseMinPrice(detail.getSellPrice()));
        detail.setCostPriceRaw(parseMinPrice(detail.getCostPrice()));
        setVariantsUsd(detail.getVariants());
    }

    private void setVariantsUsd(List<ProductDetailVariant> variants) {
        if (variants == null)
            return;
        for (ProductDetailVariant variant : variants) {
            variant.setCurrencyCode(USD);
        }
    }

    private String convertPriceString(String priceString, BigDecimal rate) {
        if (priceString == null || priceString.isBlank())
            return priceString;

        String[] parts = priceString.split(PRICE_RANGE_SEPARATOR);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0)
                result.append(RANGE_JOINER);
            String token = parts[i].trim();
            BigDecimal val = parseDecimal(token);
            if (val == null) {
                result.append(token);
            } else {
                result.append(val.multiply(rate).setScale(2, RoundingMode.HALF_UP).toPlainString());
            }
        }
        return result.toString();
    }

    private BigDecimal parseMinPrice(String priceString) {
        if (priceString == null || priceString.isBlank())
            return null;
        String[] parts = priceString.split(PRICE_RANGE_SEPARATOR);
        return parseDecimal(parts[0].trim());
    }

    private BigDecimal parseDecimal(String token) {
        try {
            return new BigDecimal(token);
        } catch (NumberFormatException ex) {
            log.trace("Not a decimal: '{}' ({})", token, ex.getMessage());
            return null;
        }
    }

    private synchronized List<PriceRule> getActiveRules() {
        long now = System.currentTimeMillis();
        if (cachedRules == null || (now - cacheTimestamp) > CACHE_TTL_MS) {
            cachedRules = priceRuleRepository.findAllActive();
            cacheTimestamp = now;
        }
        return cachedRules;
    }

    private Optional<PriceRule> findRuleWithRange(List<PriceRule> rules, PriceRuleScope scope, String scopeId,
            BigDecimal costPrice) {
        List<PriceRule> scopeRules = rules.stream().filter(r -> r.getScope() == scope).filter(r -> {
            if (scope == PriceRuleScope.GLOBAL)
                return r.getScopeId() == null;
            return scopeId != null && scopeId.equals(r.getScopeId());
        }).toList();

        if (scopeRules.isEmpty())
            return Optional.empty();

        if (costPrice != null) {
            Optional<PriceRule> rangeMatch = scopeRules.stream()
                    .filter(r -> r.getMinPrice() != null || r.getMaxPrice() != null)
                    .filter(r -> priceInRange(r, costPrice))
                    .max(Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : 0));
            if (rangeMatch.isPresent())
                return rangeMatch;
        }

        return scopeRules.stream().filter(r -> r.getMinPrice() == null && r.getMaxPrice() == null)
                .max(Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : 0));
    }

    private boolean priceInRange(PriceRule rule, BigDecimal costPrice) {
        boolean aboveMin = rule.getMinPrice() == null || costPrice.compareTo(rule.getMinPrice()) >= 0;
        boolean belowMax = rule.getMaxPrice() == null || costPrice.compareTo(rule.getMaxPrice()) <= 0;
        return aboveMin && belowMax;
    }

    private record CurrencyResolution(Currency currency, String code, BigDecimal rate) {
    }
}
