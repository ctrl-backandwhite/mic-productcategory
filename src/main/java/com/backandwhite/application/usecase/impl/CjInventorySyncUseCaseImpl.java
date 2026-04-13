package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.usecase.CjInventorySyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncStatus;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.client.cj.dto.CjInventoryByPidItemDto;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import com.backandwhite.infrastructure.db.postgres.repository.InventoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CjInventorySyncUseCaseImpl implements CjInventorySyncUseCase {

    private static final int BATCH_SIZE = 100;

    private final DropshippingPort cjClient;
    private final ProductDetailRepository productDetailRepository;
    private final SyncLogRepository syncLogRepository;
    private final SyncFailureRepository syncFailureRepository;
    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public CjSyncResult syncAll(boolean force) {
        log.info("Starting CJ inventory sync (force={})", force);

        SyncLog syncLog = syncLogRepository.save(SyncLog.builder()
                .syncType(SyncType.INVENTORY)
                .status(SyncStatus.RUNNING)
                .startedAt(Instant.now())
                .build());

        long start = System.currentTimeMillis();
        int synced = 0;
        int failed = 0;
        int total = 0;

        try {
            List<String> pids = force
                    ? productDetailRepository.findPidsNeedingProductSync(BATCH_SIZE)
                    : productDetailRepository.findPidsNeedingInventorySync(BATCH_SIZE);

            total = pids.size();
            log.info("CJ inventory sync: {} products to process", total);

            for (String pid : pids) {
                try {
                    syncInventoryForPid(pid);
                    synced++;
                } catch (Exception e) {
                    failed++;
                    log.warn("Inventory sync failed for pid={}: {}", pid, e.getMessage());
                    syncFailureRepository.save(SyncFailure.builder()
                            .syncLogId(syncLog.getId())
                            .entityType("PRODUCT_DETAIL")
                            .entityId(pid)
                            .errorCode("INVENTORY_SYNC_ERROR")
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
            log.info("CJ inventory sync completed: total={}, synced={}, failed={}, durationMs={}", total, synced,
                    failed, duration);

            return CjSyncResult.builder()
                    .totalItems(total)
                    .syncedItems(synced)
                    .failedItems(failed)
                    .durationMs(duration)
                    .syncLogId(syncLog.getId())
                    .build();

        } catch (Exception e) {
            log.error("CJ inventory sync aborted with unexpected error: {}", e.getMessage(), e);
            syncLogRepository.save(syncLog.toBuilder()
                    .status(SyncStatus.FAILED)
                    .finishedAt(Instant.now())
                    .totalItems(total)
                    .syncedItems(synced)
                    .failedItems(failed)
                    .errorMessage(e.getMessage())
                    .build());
            throw e;
        }
    }

    @Override
    public CjSyncResult syncByPid(String pid) {
        log.info("Syncing inventory for pid={}", pid);
        long start = System.currentTimeMillis();
        try {
            syncInventoryForPid(pid);
            return CjSyncResult.builder()
                    .totalItems(1).syncedItems(1).failedItems(0)
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            log.error("Inventory sync failed for pid={}: {}", pid, e.getMessage(), e);
            return CjSyncResult.builder()
                    .totalItems(1).syncedItems(0).failedItems(1)
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    @Transactional
    protected void syncInventoryForPid(String pid) {
        List<CjInventoryByPidItemDto> items = cjClient.getInventoryByPid(pid);

        for (CjInventoryByPidItemDto item : items) {
            if (item.getVid() == null)
                continue;

            Optional<ProductDetailVariantInventoryEntity> existing = inventoryJpaRepository
                    .findByVidAndCountryCode(item.getVid(), item.getCountryCode());

            if (existing.isPresent()) {
                ProductDetailVariantInventoryEntity entity = existing.get();
                entity.setTotalInventory(item.getTotalInventory());
                entity.setCjInventory(item.getCjInventory());
                entity.setFactoryInventory(item.getFactoryInventory());
                inventoryJpaRepository.save(entity);
            } else {
                inventoryJpaRepository.save(ProductDetailVariantInventoryEntity.builder()
                        .vid(item.getVid())
                        .countryCode(item.getCountryCode())
                        .totalInventory(item.getTotalInventory())
                        .cjInventory(item.getCjInventory())
                        .factoryInventory(item.getFactoryInventory())
                        .build());
            }
        }

        productDetailRepository.markInventorySynced(pid);
        log.debug("Inventory synced for pid={}, items={}", pid, items.size());
    }
}
