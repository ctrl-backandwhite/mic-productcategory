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

/**
 * Template with the common CJ sync job lifecycle:
 * <ul>
 * <li>Create a {@link SyncLog} marked as RUNNING</li>
 * <li>Process each PID recording per-PID failures into
 * {@link SyncFailureRepository}</li>
 * <li>Finalize the sync log with the aggregate status (SUCCESS / PARTIAL /
 * FAILED)</li>
 * </ul>
 * Concrete use cases provide the list of PIDs to process and the per-PID
 * action. The per-PID action is expected to be annotated with
 * {@code @Transactional} in the subclass when transactional semantics are
 * required.
 */
@Log4j2
@RequiredArgsConstructor
public abstract class AbstractCjSyncUseCase {

    protected final SyncLogRepository syncLogRepository;
    protected final SyncFailureRepository syncFailureRepository;

    protected CjSyncResult runSyncJob(SyncJobConfig config, Supplier<List<String>> pidsProvider,
            Consumer<String> pidHandler) {
        SyncLog syncLog = syncLogRepository.save(SyncLog.builder().syncType(config.syncType())
                .status(SyncStatus.RUNNING).startedAt(Instant.now()).build());

        long start = System.currentTimeMillis();
        int synced = 0;
        int failed = 0;
        int total = 0;

        try {
            List<String> pids = pidsProvider.get();
            total = pids.size();
            log.info("{}: {} products to process", config.logLabel(), total);

            for (String pid : pids) {
                if (processSinglePid(pid, pidHandler, syncLog, config)) {
                    synced++;
                } else {
                    failed++;
                }
            }

            SyncStatus finalStatus = failed == 0
                    ? SyncStatus.SUCCESS
                    : (synced > 0 ? SyncStatus.PARTIAL : SyncStatus.FAILED);

            syncLogRepository.save(syncLog.toBuilder().status(finalStatus).finishedAt(Instant.now()).totalItems(total)
                    .syncedItems(synced).failedItems(failed).build());

            long duration = System.currentTimeMillis() - start;
            log.info("{} completed: total={}, synced={}, failed={}, durationMs={}", config.logLabel(), total, synced,
                    failed, duration);

            return CjSyncResult.builder().totalItems(total).syncedItems(synced).failedItems(failed).durationMs(duration)
                    .syncLogId(syncLog.getId()).build();

        } catch (Exception e) {
            log.error("{} aborted: {}", config.logLabel(), e.getMessage(), e);
            syncLogRepository.save(syncLog.toBuilder().status(SyncStatus.FAILED).finishedAt(Instant.now())
                    .totalItems(total).syncedItems(synced).failedItems(failed).errorMessage(e.getMessage()).build());
            throw e;
        }
    }

    private boolean processSinglePid(String pid, Consumer<String> pidHandler, SyncLog syncLog, SyncJobConfig config) {
        try {
            pidHandler.accept(pid);
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
     * timing and error handling are shared.
     */
    protected CjSyncResult runSingleItem(String pid, Runnable action, String logLabel) {
        long start = System.currentTimeMillis();
        try {
            action.run();
            return CjSyncResult.builder().totalItems(1).syncedItems(1).failedItems(0)
                    .durationMs(System.currentTimeMillis() - start).build();
        } catch (Exception e) {
            log.error("{} failed for pid={}: {}", logLabel, pid, e.getMessage(), e);
            return CjSyncResult.builder().totalItems(1).syncedItems(0).failedItems(1)
                    .durationMs(System.currentTimeMillis() - start).build();
        }
    }

    /**
     * Aggregates the static labels used to configure a sync job.
     */
    public record SyncJobConfig(SyncType syncType, String entityType, String errorCode, String logLabel) {
    }
}
