package com.backandwhite.application.strategy;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListV2ItemDto;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class DiscoveryByCategoryStrategy implements DiscoveryStrategyExecutor {

    private final DropshippingPort dropshippingPort;
    private final DiscoveredPidRepository discoveredPidRepository;
    private final ProductDetailRepository productDetailRepository;
    private final CategoryRepository categoryRepository;
    private final DiscoveryStateRepository stateRepository;
    private final CjDropshippingProperties properties;

    @Override
    public DiscoveryStrategy getStrategy() {
        return DiscoveryStrategy.BY_CATEGORY;
    }

    @Override
    public boolean supportsResume() {
        return true;
    }

    @Override
    public DiscoveryResult execute(DiscoveryState state) {
        List<String> categoryIds = categoryRepository.findAllLevel3Ids();
        if (categoryIds.isEmpty()) {
            log.warn("No L3 categories found in DB — skipping BY_CATEGORY discovery");
            return DiscoveryResult.builder().completed(true).build();
        }

        int startIdx = findResumeIndex(categoryIds, state.getLastCategoryId());
        int totalNew = 0;
        int totalProcessed = 0;
        int totalPages = 0;

        log.info("BY_CATEGORY discovery starting from index {} of {} categories", startIdx, categoryIds.size());

        for (int i = startIdx; i < categoryIds.size(); i++) {
            String catId = categoryIds.get(i);

            try {
                var catResult = crawlCategory(catId);
                totalNew += catResult.getNewPidsDiscovered();
                totalProcessed += catResult.getTotalPidsProcessed();
                totalPages += catResult.getPagesScanned();

                // Checkpoint after each category
                state.setLastCategoryId(catId);
                state.setTotalDiscovered(state.getTotalDiscovered() + catResult.getNewPidsDiscovered());
                state.setUpdatedAt(Instant.now());
                stateRepository.save(state);

                if (catResult.getNewPidsDiscovered() > 0) {
                    log.info("Category {}: {} new PIDs (total processed: {})", catId,
                            catResult.getNewPidsDiscovered(), catResult.getTotalPidsProcessed());
                }
            } catch (Exception e) {
                log.error("Error crawling category {}: {}", catId, e.getMessage());
                // Continue to next category instead of failing
            }
        }

        log.info("BY_CATEGORY discovery completed: {} new PIDs, {} total processed, {} pages scanned",
                totalNew, totalProcessed, totalPages);

        return DiscoveryResult.builder()
                .newPidsDiscovered(totalNew)
                .totalPidsProcessed(totalProcessed)
                .pagesScanned(totalPages)
                .completed(true)
                .build();
    }

    private DiscoveryResult crawlCategory(String categoryId) {
        int maxPages = properties.getDiscovery().getMaxPagesPerCategory();
        int pageSize = properties.getDiscovery().getPageSize();
        long waitMs = properties.getDiscovery().getRateLimitWaitMs();

        int page = 1;
        int newPids = 0;
        int totalProcessed = 0;

        while (page <= maxPages) {
            CjProductListPageDto pageResult = dropshippingPort.getProductListFiltered(
                    page, pageSize, categoryId, null, null, null, 3, "desc");

            List<CjProductListV2ItemDto> products = (pageResult != null)
                    ? pageResult.getAllProducts()
                    : List.of();

            if (products.isEmpty()) {
                break;
            }

            List<DiscoveredPid> batch = new ArrayList<>();
            for (CjProductListV2ItemDto item : products) {
                totalProcessed++;
                String pid = item.getId();
                if (pid == null || pid.isBlank())
                    continue;

                if (!discoveredPidRepository.existsByPid(pid) && !productDetailRepository.existsByPid(pid)) {
                    batch.add(buildDiscoveredPid(item, categoryId, null));
                    newPids++;
                }
            }

            if (!batch.isEmpty()) {
                discoveredPidRepository.saveAll(batch);
            }

            int totalPages = pageResult.getTotalRecords() != null
                    ? (int) Math.ceil((double) pageResult.getTotalRecords() / pageSize)
                    : page;
            if (page >= totalPages)
                break;

            page++;
            rateLimitWait(waitMs);
        }

        return DiscoveryResult.builder()
                .newPidsDiscovered(newPids)
                .totalPidsProcessed(totalProcessed)
                .pagesScanned(page)
                .build();
    }

    private int findResumeIndex(List<String> categoryIds, String lastCategoryId) {
        if (lastCategoryId == null || lastCategoryId.isBlank())
            return 0;
        int idx = categoryIds.indexOf(lastCategoryId);
        return idx >= 0 ? idx + 1 : 0;
    }

    private DiscoveredPid buildDiscoveredPid(CjProductListV2ItemDto item, String categoryId, String keyword) {
        return DiscoveredPid.builder()
                .id(UUID.randomUUID().toString())
                .pid(item.getId())
                .categoryId(categoryId)
                .keyword(keyword)
                .strategy(DiscoveryStrategy.BY_CATEGORY)
                .status(DiscoveryStatus.NEW)
                .nameEn(item.getNameEn())
                .sellPrice(item.getSellPrice())
                .discoveredAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private void rateLimitWait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
