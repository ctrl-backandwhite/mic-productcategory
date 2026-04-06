package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.InventoryUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import com.backandwhite.infrastructure.db.postgres.repository.InventoryJpaRepository;
import com.backandwhite.infrastructure.db.postgres.repository.ProductDetailVariantJpaRepository;
import com.backandwhite.infrastructure.message.kafka.producer.CatalogEventProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class InventoryUseCaseImpl implements InventoryUseCase {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final String DEFAULT_COUNTRY = "GLOBAL";

    private final InventoryJpaRepository inventoryJpaRepository;
    private final ProductDetailVariantJpaRepository variantJpaRepository;
    private final Optional<CatalogEventProducerService> catalogEventProducer;

    @Override
    @Transactional
    public int reserveStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        int updated = inventoryJpaRepository.decrementStock(variantId, country, quantity);
        if (updated == 0) {
            throw Message.VALIDATION_ERROR.toArgumentException(
                    "Insufficient stock for variant " + variantId + " in " + country);
        }

        int remaining = inventoryJpaRepository.getTotalStockByVid(variantId);
        String pid = getProductIdForVariant(variantId);

        catalogEventProducer.ifPresent(p -> p.publishStockReserved(pid, variantId, orderId, quantity, remaining));
        checkLowStock(pid, variantId, remaining);

        log.info("Stock reserved: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional
    public int deductStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        int updated = inventoryJpaRepository.decrementStock(variantId, country, quantity);
        if (updated == 0) {
            throw Message.VALIDATION_ERROR.toArgumentException(
                    "Insufficient stock for variant " + variantId + " in " + country);
        }

        int remaining = inventoryJpaRepository.getTotalStockByVid(variantId);
        String pid = getProductIdForVariant(variantId);

        // Note: We do NOT re-publish stock.deducted here — the orderservice already
        // published the command event. Re-publishing would create an infinite Kafka
        // loop
        // since StockEventConsumerService listens on the same topic.
        checkLowStock(pid, variantId, remaining);

        if (remaining == 0) {
            String productName = getProductNameForVariant(variantId);
            catalogEventProducer.ifPresent(p -> p.publishStockDepleted(pid, variantId, productName));
        }

        log.info("Stock deducted: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional
    public int releaseStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        inventoryJpaRepository.incrementStock(variantId, country, quantity);
        int remaining = inventoryJpaRepository.getTotalStockByVid(variantId);

        // Note: We do NOT re-publish stock.released — the orderservice already
        // published the command event. Re-publishing would create an infinite Kafka
        // loop.

        log.info("Stock released: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional
    public int restoreStock(String variantId, String orderId, int quantity, String countryCode) {
        String country = resolveCountry(countryCode);
        ensureVariantExists(variantId);

        inventoryJpaRepository.incrementStock(variantId, country, quantity);
        int remaining = inventoryJpaRepository.getTotalStockByVid(variantId);

        // Note: We do NOT re-publish stock.restored — the orderservice already
        // published the command event. Re-publishing would create an infinite Kafka
        // loop.

        log.info("Stock restored: vid={}, orderId={}, qty={}, remaining={}", variantId, orderId, quantity, remaining);
        return remaining;
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableStock(String variantId) {
        return inventoryJpaRepository.getTotalStockByVid(variantId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStock(String variantId, int quantity) {
        return inventoryJpaRepository.getTotalStockByVid(variantId) >= quantity;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void ensureVariantExists(String variantId) {
        if (!variantJpaRepository.existsById(variantId)) {
            throw Message.ENTITY_NOT_FOUND.toEntityNotFound("ProductDetailVariant", variantId);
        }
    }

    private String getProductIdForVariant(String variantId) {
        return variantJpaRepository.findById(variantId)
                .map(ProductDetailVariantEntity::getPid)
                .orElse(variantId);
    }

    private String getProductNameForVariant(String variantId) {
        return variantJpaRepository.findById(variantId)
                .flatMap(v -> v.getTranslations().stream().findFirst())
                .map(t -> t.getVariantName())
                .orElse("Unknown");
    }

    private void checkLowStock(String productId, String variantId, int remaining) {
        if (remaining > 0 && remaining <= LOW_STOCK_THRESHOLD) {
            String productName = getProductNameForVariant(variantId);
            catalogEventProducer.ifPresent(
                    p -> p.publishStockLowAlert(productId, variantId, productName, remaining, LOW_STOCK_THRESHOLD));
        }
    }

    private String resolveCountry(String countryCode) {
        return (countryCode != null && !countryCode.isBlank()) ? countryCode : DEFAULT_COUNTRY;
    }
}
