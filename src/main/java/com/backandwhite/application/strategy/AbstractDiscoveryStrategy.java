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
import java.util.function.Function;
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
     */
    protected DiscoveryResult crawlPages(int maxPages, PageFetcher pageFetcher,
            Function<CjProductListV2ItemDto, DiscoveredPid> itemToPid) {
        int pageSize = properties.getDiscovery().getPageSize();
        long waitMs = properties.getDiscovery().getRateLimitWaitMs();

        CrawlState crawlState = new CrawlState();
        int page = 1;
        while (page <= maxPages && fetchAndProcessPage(page, pageSize, pageFetcher, itemToPid, crawlState, waitMs)) {
            page++;
        }
        return DiscoveryResult.builder().newPidsDiscovered(crawlState.newPids)
                .totalPidsProcessed(crawlState.totalProcessed).pagesScanned(crawlState.pagesScanned).build();
    }

    /**
     * Fetches one page and ingests its items. Returns {@code true} when the next
     * page should be requested, {@code false} when the crawl must stop (last page
     * reached or page empty).
     */
    @SuppressWarnings("java:S107")
    private boolean fetchAndProcessPage(int page, int pageSize, PageFetcher pageFetcher,
            Function<CjProductListV2ItemDto, DiscoveredPid> itemToPid, CrawlState crawlState, long waitMs) {
        CjProductListPageDto pageResult = pageFetcher.fetch(page, pageSize);
        List<CjProductListV2ItemDto> products = (pageResult != null) ? pageResult.getAllProducts() : List.of();
        if (products.isEmpty()) {
            crawlState.pagesScanned = page;
            return false;
        }
        processPageItems(products, itemToPid, crawlState);
        crawlState.pagesScanned = page;
        if (isLastPage(pageResult, page, pageSize)) {
            return false;
        }
        rateLimitWait(waitMs);
        return true;
    }

    private void processPageItems(List<CjProductListV2ItemDto> products,
            Function<CjProductListV2ItemDto, DiscoveredPid> itemToPid, CrawlState crawlState) {
        List<DiscoveredPid> batch = new ArrayList<>();
        for (CjProductListV2ItemDto item : products) {
            crawlState.totalProcessed++;
            String pid = item.getId();
            if (pid == null || pid.isBlank()) {
                continue;
            }
            if (!discoveredPidRepository.existsByPid(pid) && !productDetailRepository.existsByPid(pid)) {
                batch.add(itemToPid.apply(item));
                crawlState.newPids++;
            }
        }
        if (!batch.isEmpty()) {
            discoveredPidRepository.saveAll(batch);
        }
    }

    private boolean isLastPage(CjProductListPageDto pageResult, int currentPage, int pageSize) {
        int totalPages = pageResult.getTotalRecords() != null
                ? (int) Math.ceil((double) pageResult.getTotalRecords() / pageSize)
                : currentPage;
        return currentPage >= totalPages;
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
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    protected interface PageFetcher {
        CjProductListPageDto fetch(int page, int pageSize);
    }

    private static final class CrawlState {
        int newPids;
        int totalProcessed;
        int pagesScanned;
    }
}
