package com.backandwhite.infrastructure.message.kafka.consumer;

import com.backandwhite.application.usecase.InventoryUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.kafka.avro.StockDeductedEvent;
import com.backandwhite.core.kafka.avro.StockReleasedEvent;
import com.backandwhite.core.kafka.avro.StockRestoredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumes stock events published by mic-orderservice to update inventory in
 * the product catalog database.
 */
@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class StockEventConsumerService {

    private final InventoryUseCase inventoryUseCase;

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_STOCK_DEDUCTED, groupId = AppConstants.KAFKA_GROUP_INVENTORY, containerFactory = "avroKafkaListenerContainerFactory")
    public void onStockDeducted(StockDeductedEvent event) {
        log.info("::> Received stock.deducted: vid={}, orderId={}, qty={}", event.getVariantId(), event.getOrderId(),
                event.getQuantity());
        try {
            inventoryUseCase.deductStock(str(event.getVariantId()), str(event.getOrderId()), event.getQuantity(), null);
        } catch (Exception e) {
            log.error("::> Failed processing stock.deducted for vid={}: {}", event.getVariantId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_STOCK_RELEASED, groupId = AppConstants.KAFKA_GROUP_INVENTORY, containerFactory = "avroKafkaListenerContainerFactory")
    public void onStockReleased(StockReleasedEvent event) {
        log.info("::> Received stock.released: vid={}, orderId={}, qty={}", event.getVariantId(), event.getOrderId(),
                event.getQuantity());
        try {
            inventoryUseCase.releaseStock(str(event.getVariantId()), str(event.getOrderId()), event.getQuantity(),
                    null);
        } catch (Exception e) {
            log.error("::> Failed processing stock.released for vid={}: {}", event.getVariantId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = AppConstants.KAFKA_TOPIC_STOCK_RESTORED, groupId = AppConstants.KAFKA_GROUP_INVENTORY, containerFactory = "avroKafkaListenerContainerFactory")
    public void onStockRestored(StockRestoredEvent event) {
        log.info("::> Received stock.restored: vid={}, orderId={}, qty={}", event.getVariantId(), event.getOrderId(),
                event.getQuantity());
        try {
            inventoryUseCase.restoreStock(str(event.getVariantId()), str(event.getOrderId()), event.getQuantity(),
                    null);
        } catch (Exception e) {
            log.error("::> Failed processing stock.restored for vid={}: {}", event.getVariantId(), e.getMessage(), e);
        }
    }

    private String str(CharSequence cs) {
        return cs != null ? cs.toString() : null;
    }
}
