package com.backandwhite.application.scheduler;

import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class CjProductFullSyncScheduler {

    private final CjProductFullSyncUseCase cjProductFullSyncUseCase;

    /**
     * Runs daily at 03:00 to refresh product data (names, descriptions, images,
     * variants).
     * Cron: 0 0 3 * * * — at 3 AM every day.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void syncProducts() {
        log.info("Scheduled CJ product full sync triggered");
        try {
            var result = cjProductFullSyncUseCase.syncAll(false);
            log.info("Scheduled product full sync finished: total={}, synced={}, failed={}",
                    result.getTotalItems(), result.getSyncedItems(), result.getFailedItems());
        } catch (Exception e) {
            log.error("Scheduled product full sync failed: {}", e.getMessage(), e);
        }
    }
}
