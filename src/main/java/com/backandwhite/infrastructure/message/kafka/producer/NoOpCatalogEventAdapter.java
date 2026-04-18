package com.backandwhite.infrastructure.message.kafka.producer;

import com.backandwhite.application.port.out.CatalogEventPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of {@link CatalogEventPort} used when Kafka is disabled.
 * All methods intentionally do nothing — events are silently dropped.
 */
@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpCatalogEventAdapter implements CatalogEventPort {

    @Override
    public void publishProductCreated(String productId, String name, String sku, String price, String categoryId,
            String brandId) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishProductUpdated(String productId, String name, String price, String categoryId, String brandId,
            boolean enabled) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishProductStateChanged(String productId, String name, boolean enabled) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishCategoryUpdated(String categoryId, String name, String parentId, boolean enabled) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishStockReserved(String productId, String variantId, String orderId, int quantity,
            int remainingStock) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishStockDeducted(String productId, String variantId, String orderId, int quantity,
            int remainingStock) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishStockReleased(String productId, String variantId, String orderId, int quantity,
            int remainingStock) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishStockDepleted(String productId, String variantId, String productName) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishStockRestored(String productId, String variantId, String orderId, int quantity,
            int remainingStock) {
        // No-op: Kafka disabled
    }

    @Override
    public void publishStockLowAlert(String productId, String variantId, String productName, int currentStock,
            int threshold) {
        // No-op: Kafka disabled
    }
}
