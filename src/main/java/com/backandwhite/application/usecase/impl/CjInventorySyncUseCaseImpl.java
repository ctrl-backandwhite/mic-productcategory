package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.application.usecase.CjInventorySyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.client.cj.dto.CjInventoryByPidItemDto;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import com.backandwhite.infrastructure.db.postgres.repository.InventoryJpaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class CjInventorySyncUseCaseImpl extends AbstractCjSyncUseCase implements CjInventorySyncUseCase {

    private static final int BATCH_SIZE = 100;
    private static final SyncJobConfig CONFIG = new SyncJobConfig(SyncType.INVENTORY, "PRODUCT_DETAIL",
            "INVENTORY_SYNC_ERROR", "CJ inventory sync");

    private final DropshippingPort cjClient;
    private final ProductDetailRepository productDetailRepository;
    private final InventoryJpaRepository inventoryJpaRepository;
    private final ProductSearchIndexPort productSearchIndexPort;

    public CjInventorySyncUseCaseImpl(SyncLogRepository syncLogRepository, SyncFailureRepository syncFailureRepository,
            DropshippingPort cjClient, ProductDetailRepository productDetailRepository,
            InventoryJpaRepository inventoryJpaRepository, ProductSearchIndexPort productSearchIndexPort) {
        super(syncLogRepository, syncFailureRepository);
        this.cjClient = cjClient;
        this.productDetailRepository = productDetailRepository;
        this.inventoryJpaRepository = inventoryJpaRepository;
        this.productSearchIndexPort = productSearchIndexPort;
    }

    @Override
    public CjSyncResult syncAll(boolean force) {
        log.info("Starting CJ inventory sync (force={})", force);
        return runSyncJob(CONFIG,
                () -> force
                        ? productDetailRepository.findPidsNeedingProductSync(BATCH_SIZE)
                        : productDetailRepository.findPidsNeedingInventorySync(BATCH_SIZE),
                this::syncInventoryForPid);
    }

    @Override
    public CjSyncResult syncByPid(String pid) {
        log.info("Syncing inventory for pid={}", pid);
        return runSingleItem(pid, () -> syncInventoryForPid(pid), CONFIG.logLabel());
    }

    @Transactional
    protected void syncInventoryForPid(String pid) {
        List<CjInventoryByPidItemDto> items = cjClient.getInventoryByPid(pid);
        Map<String, Integer> variantStock = new HashMap<>();

        for (CjInventoryByPidItemDto item : items) {
            if (item.getVid() == null)
                continue;

            upsertInventory(item);

            variantStock.merge(item.getVid(), item.getTotalInventory() != null ? item.getTotalInventory() : 0,
                    Integer::sum);
        }

        productDetailRepository.markInventorySynced(pid);
        productSearchIndexPort.updateStock(pid, variantStock);
        log.debug("Inventory synced for pid={}, items={}", pid, items.size());
    }

    private void upsertInventory(CjInventoryByPidItemDto item) {
        Optional<ProductDetailVariantInventoryEntity> existing = inventoryJpaRepository
                .findByVidAndCountryCode(item.getVid(), item.getCountryCode());

        if (existing.isPresent()) {
            ProductDetailVariantInventoryEntity entity = existing.get();
            entity.setTotalInventory(item.getTotalInventory());
            entity.setCjInventory(item.getCjInventory());
            entity.setFactoryInventory(item.getFactoryInventory());
            inventoryJpaRepository.save(entity);
        } else {
            inventoryJpaRepository.save(ProductDetailVariantInventoryEntity.builder().vid(item.getVid())
                    .countryCode(item.getCountryCode()).totalInventory(item.getTotalInventory())
                    .cjInventory(item.getCjInventory()).factoryInventory(item.getFactoryInventory()).build());
        }
    }
}
