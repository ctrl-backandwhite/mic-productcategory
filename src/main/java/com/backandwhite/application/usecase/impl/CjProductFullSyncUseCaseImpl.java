package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public CjProductFullSyncUseCaseImpl(SyncLogRepository syncLogRepository,
            SyncFailureRepository syncFailureRepository, DropshippingPort cjClient,
            ProductDetailRepository productDetailRepository, CjProductDetailMapper cjProductDetailMapper,
            ProductSearchIndexPort productSearchIndexPort) {
        super(syncLogRepository, syncFailureRepository);
        this.cjClient = cjClient;
        this.productDetailRepository = productDetailRepository;
        this.cjProductDetailMapper = cjProductDetailMapper;
        this.productSearchIndexPort = productSearchIndexPort;
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
