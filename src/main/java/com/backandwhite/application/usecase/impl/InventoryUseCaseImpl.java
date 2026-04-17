package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.CatalogEventPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.application.usecase.InventoryUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.repository.InventoryRepository;
import com.backandwhite.domain.repository.ProductDetailRepository;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class InventoryUseCaseImpl implements InventoryUseCase {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final String DEFAULT_COUNTRY = "GLOBAL";

    private final InventoryRepository inventoryRepository;
    private final ProductDetailRepository productDetailRepository;
    private final CatalogEventPort catalogEventPort;
    private final ProductSearchIndexPort productSearchIndexPort;

    @Override
    @Transactional
    public int reserveStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        int updated = inventoryRepository.decrementStock(variantId, country, quantity);
        if (updated == 0) {
            throw Message.VALIDATION_ERROR
                    .toArgumentException("Insufficient stock for variant " + variantId + " in " + country);
        }

        int remaining = inventoryRepository.getTotalStockByVid(variantId);
        String pid = getProductIdForVariant(variantId);

        catalogEventPort.publishStockReserved(pid, variantId, orderId, quantity, remaining);
        checkLowStock(pid, variantId, remaining);
        updateEsStock(pid);

        log.info("Stock reserved: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional
    public int deductStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        int updated = inventoryRepository.decrementStock(variantId, country, quantity);
        if (updated == 0) {
            throw Message.VALIDATION_ERROR
                    .toArgumentException("Insufficient stock for variant " + variantId + " in " + country);
        }

        int remaining = inventoryRepository.getTotalStockByVid(variantId);
        String pid = getProductIdForVariant(variantId);

        // Note: We do NOT re-publish stock.deducted here — the orderservice already
        // published the command event. Re-publishing would create an infinite Kafka
        // loop
        // since StockEventConsumerService listens on the same topic.
        checkLowStock(pid, variantId, remaining);

        if (remaining == 0) {
            String productName = getProductNameForVariant(variantId);
            catalogEventPort.publishStockDepleted(pid, variantId, productName);
        }

        updateEsStock(pid);
        log.info("Stock deducted: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional
    public int releaseStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        inventoryRepository.incrementStock(variantId, country, quantity);
        int remaining = inventoryRepository.getTotalStockByVid(variantId);
        String pid = getProductIdForVariant(variantId);

        // Note: We do NOT re-publish stock.released — the orderservice already
        // published the command event. Re-publishing would create an infinite Kafka
        // loop.

        updateEsStock(pid);
        log.info("Stock released: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional
    public int restoreStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        inventoryRepository.incrementStock(variantId, country, quantity);
        int remaining = inventoryRepository.getTotalStockByVid(variantId);
        String pid = getProductIdForVariant(variantId);

        // Note: We do NOT re-publish stock.restored — the orderservice already
        // published the command event. Re-publishing would create an infinite Kafka
        // loop.

        updateEsStock(pid);
        log.info("Stock restored: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableStock(String variantId) {
        return inventoryRepository.getTotalStockByVid(variantId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStock(String variantId, int quantity) {
        return inventoryRepository.getTotalStockByVid(variantId) >= quantity;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void ensureVariantExists(String variantId) {
        if (!productDetailRepository.existsVariantByVid(variantId)) {
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetailVariant", variantId);
        }
    }

    private String getProductIdForVariant(String variantId) {
        return productDetailRepository.findVariantByVid(variantId, null).map(ProductDetailVariant::getPid)
                .orElse(variantId);
    }

    private String getProductNameForVariant(String variantId) {
        return productDetailRepository.findVariantByVid(variantId, null)
                .flatMap(v -> v.getTranslations().stream().findFirst()).map(t -> t.getVariantName()).orElse("Unknown");
    }

    private void checkLowStock(String productId, String variantId, int remaining) {
        if (remaining > 0 && remaining <= LOW_STOCK_THRESHOLD) {
            String productName = getProductNameForVariant(variantId);
            catalogEventPort.publishStockLowAlert(productId, variantId, productName, remaining, LOW_STOCK_THRESHOLD);
        }
    }

    private String resolveCountry(String countryCode) {
        return (countryCode != null && !countryCode.isBlank()) ? countryCode : DEFAULT_COUNTRY;
    }

    private void updateEsStock(String pid) {
        try {
            Map<String, Integer> variantStockMap = productDetailRepository.findVariantsByPid(pid, null).stream()
                    .collect(Collectors.toMap(ProductDetailVariant::getVid,
                            v -> inventoryRepository.getTotalStockByVid(v.getVid())));
            productSearchIndexPort.updateStock(pid, variantStockMap);
            log.debug("ES stock updated for pid={}, variants={}", pid, variantStockMap.size());
        } catch (Exception e) {
            log.warn("Failed to update ES stock for pid={}: {}", pid, e.getMessage());
        }
    }
}
