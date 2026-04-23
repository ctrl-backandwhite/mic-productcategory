package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.application.usecase.CjInventorySyncUseCase;
import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Log4j2
@Service
public class CjProductFullSyncUseCaseImpl extends AbstractCjSyncUseCase implements CjProductFullSyncUseCase {

    private static final int BATCH_SIZE = 50;
    private static final SyncJobConfig CONFIG = new SyncJobConfig(SyncType.PRODUCT_FULL, "PRODUCT_DETAIL",
            "PRODUCT_SYNC_ERROR", "CJ product full sync");

    private final DropshippingPort cjClient;
    private final ProductDetailRepository productDetailRepository;
    private final CjProductDetailMapper cjProductDetailMapper;
    private final ProductSearchIndexPort productSearchIndexPort;
    /**
     * Injected lazily because both sync use cases share the same infrastructure
     * graph (sync log / failure repository, transaction template) and Spring would
     * otherwise see a potential cycle at wiring time.
     */
    private final CjInventorySyncUseCase cjInventorySyncUseCase;

    public CjProductFullSyncUseCaseImpl(SyncLogRepository syncLogRepository,
            SyncFailureRepository syncFailureRepository, TransactionTemplate transactionTemplate,
            DropshippingPort cjClient, ProductDetailRepository productDetailRepository,
            CjProductDetailMapper cjProductDetailMapper, ProductSearchIndexPort productSearchIndexPort,
            @Lazy CjInventorySyncUseCase cjInventorySyncUseCase) {
        super(syncLogRepository, syncFailureRepository, transactionTemplate);
        this.cjClient = cjClient;
        this.productDetailRepository = productDetailRepository;
        this.cjProductDetailMapper = cjProductDetailMapper;
        this.productSearchIndexPort = productSearchIndexPort;
        this.cjInventorySyncUseCase = cjInventorySyncUseCase;
    }

    @Override
    public CjSyncResult syncAll(boolean force) {
        log.info("Starting CJ product full sync (force={})", force);
        return runSyncJob(CONFIG, () -> productDetailRepository.findPidsNeedingProductSync(BATCH_SIZE),
                this::syncProductForPid);
    }

    @Override
    public CjSyncResult syncByPid(String pid) {
        return runSingleItem(pid, () -> syncProductForPid(pid), CONFIG.logLabel());
    }

    void syncProductForPid(String pid) {
        CjProductDetailDto dto = cjClient.getProductDetail(pid);
        ProductDetail domain = cjProductDetailMapper.toDomain(dto);
        productDetailRepository.save(domain);
        productDetailRepository.markProductSynced(pid);
        productSearchIndexPort.indexProductDetail(domain);
        // Chain the inventory sync so the product lands in the catalog already
        // carrying real per-variant stock. CJ's /product/query payload doesn't
        // include inventory (that's why warehouseInventoryNum is always 0), so
        // without this follow-up the product would appear as out-of-stock until
        // the 4h inventory scheduler picked it up. Failures here don't fail the
        // product sync — the scheduler will retry.
        try {
            cjInventorySyncUseCase.syncByPid(pid);
        } catch (RuntimeException e) {
            log.warn("::> Chained inventory sync failed for pid={} — scheduler will retry. reason={}", pid,
                    e.getMessage());
        }
        log.debug("Product full sync done for pid={}", pid);
    }
}
