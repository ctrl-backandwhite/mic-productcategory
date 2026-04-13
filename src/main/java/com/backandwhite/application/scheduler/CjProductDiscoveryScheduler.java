package com.backandwhite.application.scheduler;

import com.backandwhite.application.usecase.ProductDiscoveryUseCase;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cj-dropshipping.discovery.enabled", havingValue = "true")
public class CjProductDiscoveryScheduler {

    private final ProductDiscoveryUseCase productDiscoveryUseCase;
    private final CjDropshippingProperties properties;

    @Scheduled(cron = "${cj-dropshipping.discovery.cron-full:0 0 2 * * SUN}")
    public void fullDiscovery() {
        log.info("Scheduled FULL product discovery triggered");
        try {
            var result = productDiscoveryUseCase.runFullDiscovery();
            log.info("Scheduled full discovery finished: newPids={}, pagesScanned={}",
                    result.getNewPidsDiscovered(), result.getPagesScanned());
        } catch (Exception e) {
            log.error("Scheduled full discovery failed: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${cj-dropshipping.discovery.cron-incremental:0 0 4 * * *}")
    public void incrementalDiscovery() {
        log.info("Scheduled INCREMENTAL product discovery triggered");
        try {
            var result = productDiscoveryUseCase.runIncremental();
            log.info("Scheduled incremental discovery finished: newPids={}, pagesScanned={}",
                    result.getNewPidsDiscovered(), result.getPagesScanned());
        } catch (Exception e) {
            log.error("Scheduled incremental discovery failed: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "${cj-dropshipping.discovery.cron-enrich:0 0 */6 * * *}")
    public void enrichDiscoveredPids() {
        int batchSize = properties.getDiscovery().getBatchSizeEnrich();
        log.info("Scheduled enrichment triggered (batch size: {})", batchSize);
        try {
            int synced = productDiscoveryUseCase.enrichDiscoveredPids(batchSize);
            log.info("Scheduled enrichment finished: {} PIDs synced", synced);
        } catch (Exception e) {
            log.error("Scheduled enrichment failed: {}", e.getMessage(), e);
        }
    }
}
