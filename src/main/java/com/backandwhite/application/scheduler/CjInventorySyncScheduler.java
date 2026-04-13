package com.backandwhite.application.scheduler;

import com.backandwhite.application.usecase.CjInventorySyncUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class CjInventorySyncScheduler {

    private final CjInventorySyncUseCase cjInventorySyncUseCase;

    /**
     * Runs every 4 hours to refresh variant inventory levels from CJ.
     * Cron: 0 0 *\/4 * * * — at minute 0 of every 4th hour.
     */
    @Scheduled(cron = "0 0 */4 * * *")
    public void syncInventory() {
        log.info("Scheduled CJ inventory sync triggered");
        try {
            var result = cjInventorySyncUseCase.syncAll(false);
            log.info("Scheduled inventory sync finished: total={}, synced={}, failed={}",
                    result.getTotalItems(), result.getSyncedItems(), result.getFailedItems());
        } catch (Exception e) {
            log.error("Scheduled inventory sync failed: {}", e.getMessage(), e);
        }
    }
}
