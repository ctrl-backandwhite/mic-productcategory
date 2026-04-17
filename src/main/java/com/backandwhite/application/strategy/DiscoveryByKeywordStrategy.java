package com.backandwhite.application.strategy;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
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
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DiscoveryByKeywordStrategy implements DiscoveryStrategyExecutor {

    private final DropshippingPort dropshippingPort;
    private final DiscoveredPidRepository discoveredPidRepository;
    private final ProductDetailRepository productDetailRepository;
    private final DiscoveryStateRepository stateRepository;
    private final CjDropshippingProperties properties;

    @Override
    public DiscoveryStrategy getStrategy() {
        return DiscoveryStrategy.BY_KEYWORD;
    }

    @Override
    public boolean supportsResume() {
        return true;
    }

    @Override
    public DiscoveryResult execute(DiscoveryState state) {
        List<String> keywords = properties.getDiscovery().getKeywords();
        if (keywords == null || keywords.isEmpty()) {
            log.warn("No keywords configured — skipping BY_KEYWORD discovery");
            return DiscoveryResult.builder().completed(true).build();
        }

        int startIdx = findResumeIndex(keywords, state.getLastKeyword());
        int totalNew = 0;
        int totalProcessed = 0;
        int totalPages = 0;

        log.info("BY_KEYWORD discovery starting from index {} of {} keywords", startIdx, keywords.size());

        for (int i = startIdx; i < keywords.size(); i++) {
            String keyword = keywords.get(i);

            try {
                var kwResult = crawlKeyword(keyword);
                totalNew += kwResult.getNewPidsDiscovered();
                totalProcessed += kwResult.getTotalPidsProcessed();
                totalPages += kwResult.getPagesScanned();

                state.setLastKeyword(keyword);
                state.setTotalDiscovered(state.getTotalDiscovered() + kwResult.getNewPidsDiscovered());
                state.setUpdatedAt(Instant.now());
                stateRepository.save(state);

                if (kwResult.getNewPidsDiscovered() > 0) {
                    log.info("Keyword '{}': {} new PIDs (total processed: {})", keyword,
                            kwResult.getNewPidsDiscovered(), kwResult.getTotalPidsProcessed());
                }
            } catch (Exception e) {
                log.error("Error crawling keyword '{}': {}", keyword, e.getMessage());
            }
        }

        log.info("BY_KEYWORD discovery completed: {} new PIDs, {} total processed, {} pages scanned", totalNew,
                totalProcessed, totalPages);

        return DiscoveryResult.builder().newPidsDiscovered(totalNew).totalPidsProcessed(totalProcessed)
                .pagesScanned(totalPages).completed(true).build();
    }

    private DiscoveryResult crawlKeyword(String keyword) {
        int maxPages = properties.getDiscovery().getMaxPagesPerKeyword();
        int pageSize = properties.getDiscovery().getPageSize();
        long waitMs = properties.getDiscovery().getRateLimitWaitMs();

        int page = 1;
        int newPids = 0;
        int totalProcessed = 0;

        while (page <= maxPages) {
            CjProductListPageDto pageResult = dropshippingPort.getProductListFiltered(page, pageSize, null, keyword,
                    null, null, 3, "desc");

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
                    batch.add(buildDiscoveredPid(item, keyword));
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

    private int findResumeIndex(List<String> keywords, String lastKeyword) {
        if (lastKeyword == null || lastKeyword.isBlank())
            return 0;
        int idx = keywords.indexOf(lastKeyword);
        return idx >= 0 ? idx + 1 : 0;
    }

    private DiscoveredPid buildDiscoveredPid(CjProductListV2ItemDto item, String keyword) {
        return DiscoveredPid.builder().id(UUID.randomUUID().toString()).pid(item.getId())
                .categoryId(item.getCategoryId()).keyword(keyword).strategy(DiscoveryStrategy.BY_KEYWORD)
                .status(DiscoveryStatus.NEW).nameEn(item.getNameEn()).sellPrice(item.getSellPrice())
                .discoveredAt(Instant.now()).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private void rateLimitWait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
