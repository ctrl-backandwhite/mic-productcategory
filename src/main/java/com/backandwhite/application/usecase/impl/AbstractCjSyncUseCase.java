package com.backandwhite.application.usecase.impl;

import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncStatus;
import com.backandwhite.domain.valueobject.SyncType;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Template with the common CJ sync job lifecycle:
 * <ul>
 * <li>Create a {@link SyncLog} marked as RUNNING</li>
 * <li>Process each PID inside its own transaction, recording per-PID failures
 * into {@link SyncFailureRepository}</li>
 * <li>Finalize the sync log with the aggregate status (SUCCESS / PARTIAL /
 * FAILED)</li>
 * </ul>
 * Concrete use cases provide the list of PIDs to process and the per-PID
 * action. Per-PID transactions are opened by the injected
 * {@link TransactionTemplate}, so no {@code @Transactional} proxy is needed for
 * the per-PID handlers.
 */
@Log4j2
@RequiredArgsConstructor
public abstract class AbstractCjSyncUseCase {

    protected final SyncLogRepository syncLogRepository;
    protected final SyncFailureRepository syncFailureRepository;
    protected final TransactionTemplate transactionTemplate;

    protected CjSyncResult runSyncJob(SyncJobConfig config, Supplier<List<String>> pidsProvider,
            Consumer<String> pidHandler) {
        SyncLog syncLog = syncLogRepository.save(SyncLog.builder().syncType(config.syncType())
                .status(SyncStatus.RUNNING).startedAt(Instant.now()).build());

        long start = System.currentTimeMillis();
        JobCounters counters = new JobCounters();

        try {
            List<String> pids = pidsProvider.get();
            counters.total = pids.size();
            log.info("{}: {} products to process", config.logLabel(), counters.total);

            for (String pid : pids) {
                if (processSinglePid(pid, pidHandler, syncLog, config)) {
                    counters.synced++;
                } else {
                    counters.failed++;
                }
            }

            SyncStatus finalStatus = resolveFinalStatus(counters.synced, counters.failed);

            syncLogRepository.save(syncLog.toBuilder().status(finalStatus).finishedAt(Instant.now())
                    .totalItems(counters.total).syncedItems(counters.synced).failedItems(counters.failed).build());

            long duration = System.currentTimeMillis() - start;
            log.info("{} completed: total={}, synced={}, failed={}, durationMs={}", config.logLabel(), counters.total,
                    counters.synced, counters.failed, duration);

            return CjSyncResult.builder().totalItems(counters.total).syncedItems(counters.synced)
                    .failedItems(counters.failed).durationMs(duration).syncLogId(syncLog.getId()).build();

        } catch (Exception e) {
            log.error("{} aborted: {}", config.logLabel(), e.getMessage(), e);
            syncLogRepository.save(syncLog.toBuilder().status(SyncStatus.FAILED).finishedAt(Instant.now())
                    .totalItems(counters.total).syncedItems(counters.synced).failedItems(counters.failed)
                    .errorMessage(e.getMessage()).build());
            throw e;
        }
    }

    private static SyncStatus resolveFinalStatus(int synced, int failed) {
        if (failed == 0) {
            return SyncStatus.SUCCESS;
        }
        return synced > 0 ? SyncStatus.PARTIAL : SyncStatus.FAILED;
    }

    private boolean processSinglePid(String pid, Consumer<String> pidHandler, SyncLog syncLog, SyncJobConfig config) {
        try {
            transactionTemplate.executeWithoutResult(status -> pidHandler.accept(pid));
            return true;
        } catch (Exception e) {
            log.warn("{} failed for pid={}: {}", config.logLabel(), pid, e.getMessage());
            syncFailureRepository.save(SyncFailure.builder().syncLogId(syncLog.getId()).entityType(config.entityType())
                    .entityId(pid).errorCode(config.errorCode()).errorMessage(e.getMessage()).build());
            return false;
        }
    }

    /**
     * Wrap a single-PID invocation (used by the {@code syncByPid} endpoints) so the
     * timing, transaction and error handling are shared.
     */
    protected CjSyncResult runSingleItem(String pid, Runnable action, String logLabel) {
        long start = System.currentTimeMillis();
        try {
            transactionTemplate.executeWithoutResult(status -> action.run());
            return CjSyncResult.builder().totalItems(1).syncedItems(1).failedItems(0)
                    .durationMs(System.currentTimeMillis() - start).build();
        } catch (Exception e) {
            log.error("{} failed for pid={}: {}", logLabel, pid, e.getMessage(), e);
            return CjSyncResult.builder().totalItems(1).syncedItems(0).failedItems(1)
                    .durationMs(System.currentTimeMillis() - start).build();
        }
    }

    public record SyncJobConfig(SyncType syncType, String entityType, String errorCode, String logLabel) {
    }

    private static final class JobCounters {
        int synced;
        int failed;
        int total;
    }
}
