package com.backandwhite.infrastructure.message.kafka.producer;

import com.backandwhite.application.port.out.CatalogEventPort;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.kafka.avro.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaCatalogEventAdapter implements CatalogEventPort {

        private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

        // ── Product events ──────────────────────────────────────────────────────

        public void publishProductCreated(String productId, String name, String sku,
                        String price, String categoryId, String brandId) {
                ProductCreatedEvent event = ProductCreatedEvent.newBuilder()
                                .setProductId(productId)
                                .setName(name)
                                .setSku(sku)
                                .setPrice(price != null ? price : "0")
                                .setCategoryId(categoryId)
                                .setBrandId(brandId)
                                .setEnabled(false)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_PRODUCT_CREATED, productId, event);
        }

        public void publishProductUpdated(String productId, String name, String price,
                        String categoryId, String brandId, boolean enabled) {
                ProductUpdatedEvent event = ProductUpdatedEvent.newBuilder()
                                .setProductId(productId)
                                .setName(name)
                                .setPrice(price != null ? price : "0")
                                .setCategoryId(categoryId)
                                .setBrandId(brandId)
                                .setEnabled(enabled)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_PRODUCT_UPDATED, productId, event);
        }

        public void publishProductStateChanged(String productId, String name, boolean enabled) {
                ProductStateChangedEvent event = ProductStateChangedEvent.newBuilder()
                                .setProductId(productId)
                                .setName(name)
                                .setEnabled(enabled)
                                .setTimestamp(Instant.now().toString())
                                .build();
                String topic = enabled ? AppConstants.KAFKA_TOPIC_PRODUCT_ENABLED
                                : AppConstants.KAFKA_TOPIC_PRODUCT_DISABLED;
                send(topic, productId, event);
        }

        // ── Category events ─────────────────────────────────────────────────────

        public void publishCategoryUpdated(String categoryId, String name, String parentId, boolean enabled) {
                CategoryUpdatedEvent event = CategoryUpdatedEvent.newBuilder()
                                .setCategoryId(categoryId)
                                .setName(name)
                                .setParentId(parentId)
                                .setEnabled(enabled)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_CATEGORY_UPDATED, categoryId, event);
        }

        // ── Stock events ────────────────────────────────────────────────────────

        public void publishStockReserved(String productId, String variantId, String orderId,
                        int quantity, int remainingStock) {
                StockReservedEvent event = StockReservedEvent.newBuilder()
                                .setProductId(productId)
                                .setVariantId(variantId)
                                .setOrderId(orderId)
                                .setQuantity(quantity)
                                .setRemainingStock(remainingStock)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_STOCK_RESERVED, variantId, event);
        }

        public void publishStockDeducted(String productId, String variantId, String orderId,
                        int quantity, int remainingStock) {
                StockDeductedEvent event = StockDeductedEvent.newBuilder()
                                .setProductId(productId)
                                .setVariantId(variantId)
                                .setOrderId(orderId)
                                .setQuantity(quantity)
                                .setRemainingStock(remainingStock)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_STOCK_DEDUCTED, variantId, event);
        }

        public void publishStockReleased(String productId, String variantId, String orderId,
                        int quantity, int remainingStock) {
                StockReleasedEvent event = StockReleasedEvent.newBuilder()
                                .setProductId(productId)
                                .setVariantId(variantId)
                                .setOrderId(orderId)
                                .setQuantity(quantity)
                                .setRemainingStock(remainingStock)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_STOCK_RELEASED, variantId, event);
        }

        public void publishStockDepleted(String productId, String variantId, String productName) {
                StockDepletedEvent event = StockDepletedEvent.newBuilder()
                                .setProductId(productId)
                                .setVariantId(variantId)
                                .setProductName(productName)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_STOCK_DEPLETED, variantId, event);
        }

        public void publishStockRestored(String productId, String variantId, String orderId,
                        int quantity, int remainingStock) {
                StockRestoredEvent event = StockRestoredEvent.newBuilder()
                                .setProductId(productId)
                                .setVariantId(variantId)
                                .setOrderId(orderId)
                                .setQuantity(quantity)
                                .setRemainingStock(remainingStock)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_STOCK_RESTORED, variantId, event);
        }

        public void publishStockLowAlert(String productId, String variantId, String productName,
                        int currentStock, int threshold) {
                StockLowAlertEvent event = StockLowAlertEvent.newBuilder()
                                .setProductId(productId)
                                .setVariantId(variantId)
                                .setProductName(productName)
                                .setCurrentStock(currentStock)
                                .setThreshold(threshold)
                                .setTimestamp(Instant.now().toString())
                                .build();
                send(AppConstants.KAFKA_TOPIC_STOCK_LOW_ALERT, variantId, event);
        }

        // ── Common send ─────────────────────────────────────────────────────────

        private void send(String topic, String key, SpecificRecord event) {
                log.debug("::> Publishing event to topic '{}', key='{}'", topic, key);
                kafkaTemplate.send(topic, key, event)
                                .whenComplete((result, ex) -> {
                                        if (ex != null) {
                                                log.error("::> Failed to publish to '{}': {}", topic, ex.getMessage(),
                                                                ex);
                                        } else {
                                                log.debug("::> Event published to '{}', offset: {}",
                                                                topic, result.getRecordMetadata().offset());
                                        }
                                });
        }
}
