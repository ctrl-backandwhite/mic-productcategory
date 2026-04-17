package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncStatus;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class CjProductFullSyncUseCaseImpl implements CjProductFullSyncUseCase {

    private static final int BATCH_SIZE = 50;

    private final DropshippingPort cjClient;
    private final ProductDetailRepository productDetailRepository;
    private final CjProductDetailMapper cjProductDetailMapper;
    private final SyncLogRepository syncLogRepository;
    private final SyncFailureRepository syncFailureRepository;
    private final ProductSearchIndexPort productSearchIndexPort;

    @Override
    public CjSyncResult syncAll(boolean force) {
        log.info("Starting CJ product full sync (force={})", force);

        SyncLog syncLog = syncLogRepository.save(SyncLog.builder()
                .syncType(SyncType.PRODUCT_FULL)
                .status(SyncStatus.RUNNING)
                .startedAt(Instant.now())
                .build());

        long start = System.currentTimeMillis();
        int synced = 0;
        int failed = 0;
        int total = 0;

        try {
            List<String> pids = productDetailRepository.findPidsNeedingProductSync(BATCH_SIZE);
            total = pids.size();

            log.info("CJ product full sync: {} products to process", total);

            for (String pid : pids) {
                try {
                    syncProductForPid(pid);
                    synced++;
                } catch (Exception e) {
                    failed++;
                    log.warn("Product full sync failed for pid={}: {}", pid, e.getMessage());
                    syncFailureRepository.save(SyncFailure.builder()
                            .syncLogId(syncLog.getId())
                            .entityType("PRODUCT_DETAIL")
                            .entityId(pid)
                            .errorCode("PRODUCT_SYNC_ERROR")
                            .errorMessage(e.getMessage())
                            .build());
                }
            }

            SyncStatus finalStatus = failed == 0 ? SyncStatus.SUCCESS
                    : (synced > 0 ? SyncStatus.PARTIAL : SyncStatus.FAILED);

            syncLogRepository.save(syncLog.toBuilder()
                    .status(finalStatus)
                    .finishedAt(Instant.now())
                    .totalItems(total)
                    .syncedItems(synced)
                    .failedItems(failed)
                    .build());

            long duration = System.currentTimeMillis() - start;
            log.info("CJ product full sync done: total={}, synced={}, failed={}, durationMs={}", total, synced, failed,
                    duration);

            return CjSyncResult.builder()
                    .totalItems(total).syncedItems(synced).failedItems(failed)
                    .durationMs(duration).syncLogId(syncLog.getId())
                    .build();

        } catch (Exception e) {
            log.error("CJ product full sync aborted: {}", e.getMessage(), e);
            syncLogRepository.save(syncLog.toBuilder()
                    .status(SyncStatus.FAILED)
                    .finishedAt(Instant.now())
                    .totalItems(total).syncedItems(synced).failedItems(failed)
                    .errorMessage(e.getMessage())
                    .build());
            throw e;
        }
    }

    @Override
    public CjSyncResult syncByPid(String pid) {
        long start = System.currentTimeMillis();
        try {
            syncProductForPid(pid);
            return CjSyncResult.builder()
                    .totalItems(1).syncedItems(1).failedItems(0)
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            log.error("Product full sync failed for pid={}: {}", pid, e.getMessage(), e);
            return CjSyncResult.builder()
                    .totalItems(1).syncedItems(0).failedItems(1)
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    @Transactional
    protected void syncProductForPid(String pid) {
        CjProductDetailDto dto = cjClient.getProductDetail(pid);
        ProductDetail domain = cjProductDetailMapper.toDomain(dto);
        productDetailRepository.save(domain);
        productDetailRepository.markProductSynced(pid);
        productSearchIndexPort.indexProductDetail(domain);
        log.debug("Product full sync done for pid={}", pid);
    }
}
