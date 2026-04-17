package com.backandwhite.application.scheduler;

import com.backandwhite.application.usecase.CjReviewSyncUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class CjReviewSyncScheduler {

    private final CjReviewSyncUseCase cjReviewSyncUseCase;

    /**
     * Runs daily at 05:00 to import new CJ product reviews. Cron: 0 0 5 * * * — at
     * 5 AM every day.
     */
    @Scheduled(cron = "0 0 5 * * *")
    public void syncReviews() {
        log.info("Scheduled CJ review sync triggered");
        try {
            var result = cjReviewSyncUseCase.syncAll(false);
            log.info("Scheduled review sync finished: total={}, synced={}, failed={}", result.getTotalItems(),
                    result.getSyncedItems(), result.getFailedItems());
        } catch (Exception e) {
            log.error("Scheduled review sync failed: {}", e.getMessage(), e);
        }
    }
}
