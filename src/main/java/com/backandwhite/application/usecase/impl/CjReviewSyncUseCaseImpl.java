package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.usecase.CjReviewSyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.ReviewRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncStatus;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.client.cj.dto.CjProductCommentsPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjReviewItemDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjReviewMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CjReviewSyncUseCaseImpl implements CjReviewSyncUseCase {

    private static final int BATCH_SIZE = 50;
    private static final int REVIEW_PAGE_SIZE = 50;

    private final DropshippingPort cjClient;
    private final ProductDetailRepository productDetailRepository;
    private final ReviewRepository reviewRepository;
    private final CjReviewMapper cjReviewMapper;
    private final SyncLogRepository syncLogRepository;
    private final SyncFailureRepository syncFailureRepository;

    @Override
    public CjSyncResult syncAll(boolean force) {
        log.info("Starting CJ review sync (force={})", force);

        SyncLog syncLog = syncLogRepository.save(SyncLog.builder().syncType(SyncType.REVIEWS).status(SyncStatus.RUNNING)
                .startedAt(Instant.now()).build());

        long start = System.currentTimeMillis();
        int synced = 0;
        int failed = 0;
        int total = 0;

        try {
            List<String> pids = productDetailRepository.findPidsNeedingReviewsSync(BATCH_SIZE);
            total = pids.size();
            log.info("CJ review sync: {} products to process", total);

            for (String pid : pids) {
                try {
                    int imported = syncReviewsForPid(pid);
                    synced++;
                    log.debug("Imported {} reviews for pid={}", imported, pid);
                } catch (Exception e) {
                    failed++;
                    log.warn("Review sync failed for pid={}: {}", pid, e.getMessage());
                    syncFailureRepository
                            .save(SyncFailure.builder().syncLogId(syncLog.getId()).entityType("PRODUCT_DETAIL")
                                    .entityId(pid).errorCode("REVIEW_SYNC_ERROR").errorMessage(e.getMessage()).build());
                }
            }

            SyncStatus finalStatus = failed == 0
                    ? SyncStatus.SUCCESS
                    : (synced > 0 ? SyncStatus.PARTIAL : SyncStatus.FAILED);

            syncLogRepository.save(syncLog.toBuilder().status(finalStatus).finishedAt(Instant.now()).totalItems(total)
                    .syncedItems(synced).failedItems(failed).build());

            long duration = System.currentTimeMillis() - start;
            log.info("CJ review sync done: total={}, synced={}, failed={}, durationMs={}", total, synced, failed,
                    duration);

            return CjSyncResult.builder().totalItems(total).syncedItems(synced).failedItems(failed).durationMs(duration)
                    .syncLogId(syncLog.getId()).build();

        } catch (Exception e) {
            log.error("CJ review sync aborted: {}", e.getMessage(), e);
            syncLogRepository.save(syncLog.toBuilder().status(SyncStatus.FAILED).finishedAt(Instant.now())
                    .totalItems(total).syncedItems(synced).failedItems(failed).errorMessage(e.getMessage()).build());
            throw e;
        }
    }

    @Override
    public CjSyncResult syncByPid(String pid) {
        long start = System.currentTimeMillis();
        try {
            int imported = syncReviewsForPid(pid);
            return CjSyncResult.builder().totalItems(imported).syncedItems(imported).failedItems(0)
                    .durationMs(System.currentTimeMillis() - start).build();
        } catch (Exception e) {
            log.error("Review sync failed for pid={}: {}", pid, e.getMessage(), e);
            return CjSyncResult.builder().totalItems(0).syncedItems(0).failedItems(1)
                    .durationMs(System.currentTimeMillis() - start).build();
        }
    }

    @Transactional
    protected int syncReviewsForPid(String pid) {
        List<Review> toSave = new ArrayList<>();
        int page = 1;

        while (true) {
            CjProductCommentsPageDto pageDto = cjClient.getProductComments(pid, 0, page, REVIEW_PAGE_SIZE);

            if (pageDto.getList() == null || pageDto.getList().isEmpty())
                break;

            for (CjReviewItemDto item : pageDto.getList()) {
                if (item.getId() == null)
                    continue;
                if (reviewRepository.existsByExternalReviewId(item.getId()))
                    continue;

                Review review = cjReviewMapper.toDomain(item);
                review.setId(UUID.randomUUID().toString());
                toSave.add(review);
            }

            if (pageDto.getList().size() < REVIEW_PAGE_SIZE)
                break;
            if (page >= 10)
                break; // safety cap: max 500 reviews per product per sync
            page++;
        }

        if (!toSave.isEmpty()) {
            reviewRepository.saveAll(toSave);
        }

        productDetailRepository.markReviewsSynced(pid);
        return toSave.size();
    }
}
