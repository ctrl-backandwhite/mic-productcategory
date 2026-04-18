package com.backandwhite.application.strategy;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListV2ItemDto;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Shared infrastructure for {@link DiscoveryStrategyExecutor} implementations:
 * dependency injection wiring, CJ dropshipping paging loop, PID deduplication
 * and rate-limit pacing. Subclasses only need to provide strategy-specific
 * information (page source, total pages configuration and discovered-PID
 * factory).
 */
@Log4j2
@RequiredArgsConstructor
public abstract class AbstractDiscoveryStrategy implements DiscoveryStrategyExecutor {

    protected final DropshippingPort dropshippingPort;
    protected final DiscoveredPidRepository discoveredPidRepository;
    protected final ProductDetailRepository productDetailRepository;
    protected final CjDropshippingProperties properties;

    /**
     * Execute the paginated CJ listing fetch/ingest loop for a single discovery
     * unit (a category, a keyword or a time window).
     *
     * @param maxPages
     *            max pages to scan for this unit
     * @param pageFetcher
     *            given the current page, returns the CJ page response
     * @param itemToPid
     *            factory that turns a CJ item into a {@link DiscoveredPid}
     * @return aggregate result for this unit
     */
    protected DiscoveryResult crawlPages(int maxPages, PageFetcher pageFetcher,
            java.util.function.Function<CjProductListV2ItemDto, DiscoveredPid> itemToPid) {
        int pageSize = properties.getDiscovery().getPageSize();
        long waitMs = properties.getDiscovery().getRateLimitWaitMs();

        int page = 1;
        int newPids = 0;
        int totalProcessed = 0;

        while (page <= maxPages) {
            CjProductListPageDto pageResult = pageFetcher.fetch(page, pageSize);

            List<CjProductListV2ItemDto> products = (pageResult != null) ? pageResult.getAllProducts() : List.of();

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
                    batch.add(itemToPid.apply(item));
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

        return DiscoveryResult.builder().newPidsDiscovered(newPids).totalPidsProcessed(totalProcessed)
                .pagesScanned(page).build();
    }

    /**
     * Build the common {@link DiscoveredPid} fields for all strategies.
     */
    protected DiscoveredPid buildBaseDiscoveredPid(CjProductListV2ItemDto item, String categoryId, String keyword,
            DiscoveryStrategy strategy) {
        return DiscoveredPid.builder().id(UUID.randomUUID().toString()).pid(item.getId()).categoryId(categoryId)
                .keyword(keyword).strategy(strategy).status(DiscoveryStatus.NEW).nameEn(item.getNameEn())
                .sellPrice(item.getSellPrice()).discoveredAt(Instant.now()).createdAt(Instant.now())
                .updatedAt(Instant.now()).build();
    }

    protected void rateLimitWait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    protected interface PageFetcher {
        CjProductListPageDto fetch(int page, int pageSize);
    }
}
