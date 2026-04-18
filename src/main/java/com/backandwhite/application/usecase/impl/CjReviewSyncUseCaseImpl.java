package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.usecase.CjReviewSyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.ReviewRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncType;
import com.backandwhite.infrastructure.client.cj.dto.CjProductCommentsPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjReviewItemDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjReviewMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class CjReviewSyncUseCaseImpl extends AbstractCjSyncUseCase implements CjReviewSyncUseCase {

    private static final int BATCH_SIZE = 50;
    private static final int REVIEW_PAGE_SIZE = 50;
    /** Safety cap: max 500 reviews per product per sync. */
    private static final int MAX_REVIEW_PAGES = 10;
    private static final SyncJobConfig CONFIG = new SyncJobConfig(SyncType.REVIEWS, "PRODUCT_DETAIL",
            "REVIEW_SYNC_ERROR", "CJ review sync");

    private final DropshippingPort cjClient;
    private final ProductDetailRepository productDetailRepository;
    private final ReviewRepository reviewRepository;
    private final CjReviewMapper cjReviewMapper;

    public CjReviewSyncUseCaseImpl(SyncLogRepository syncLogRepository, SyncFailureRepository syncFailureRepository,
            DropshippingPort cjClient, ProductDetailRepository productDetailRepository,
            ReviewRepository reviewRepository, CjReviewMapper cjReviewMapper) {
        super(syncLogRepository, syncFailureRepository);
        this.cjClient = cjClient;
        this.productDetailRepository = productDetailRepository;
        this.reviewRepository = reviewRepository;
        this.cjReviewMapper = cjReviewMapper;
    }

    @Override
    public CjSyncResult syncAll(boolean force) {
        log.info("Starting CJ review sync (force={})", force);
        return runSyncJob(CONFIG, () -> productDetailRepository.findPidsNeedingReviewsSync(BATCH_SIZE), pid -> {
            int imported = syncReviewsForPid(pid);
            log.debug("Imported {} reviews for pid={}", imported, pid);
        });
    }

    @Override
    public CjSyncResult syncByPid(String pid) {
        long start = System.currentTimeMillis();
        try {
            int imported = syncReviewsForPid(pid);
            return CjSyncResult.builder().totalItems(imported).syncedItems(imported).failedItems(0)
                    .durationMs(System.currentTimeMillis() - start).build();
        } catch (Exception e) {
            log.error("{} failed for pid={}: {}", CONFIG.logLabel(), pid, e.getMessage(), e);
            return CjSyncResult.builder().totalItems(0).syncedItems(0).failedItems(1)
                    .durationMs(System.currentTimeMillis() - start).build();
        }
    }

    @Transactional
    protected int syncReviewsForPid(String pid) {
        List<Review> toSave = new ArrayList<>();
        int page = 1;

        while (page <= MAX_REVIEW_PAGES) {
            CjProductCommentsPageDto pageDto = cjClient.getProductComments(pid, 0, page, REVIEW_PAGE_SIZE);
            List<CjReviewItemDto> items = pageDto.getList();
            if (items == null || items.isEmpty()) {
                break;
            }
            collectNewReviews(items, toSave);
            if (items.size() < REVIEW_PAGE_SIZE) {
                break;
            }
            page++;
        }

        if (!toSave.isEmpty()) {
            reviewRepository.saveAll(toSave);
        }

        productDetailRepository.markReviewsSynced(pid);
        return toSave.size();
    }

    private void collectNewReviews(List<CjReviewItemDto> items, List<Review> target) {
        for (CjReviewItemDto item : items) {
            if (item.getId() == null || reviewRepository.existsByExternalReviewId(item.getId())) {
                continue;
            }
            Review review = cjReviewMapper.toDomain(item);
            review.setId(UUID.randomUUID().toString());
            target.add(review);
        }
    }
}
