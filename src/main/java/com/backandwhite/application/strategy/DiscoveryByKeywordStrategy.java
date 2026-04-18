package com.backandwhite.application.strategy;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DiscoveryByKeywordStrategy extends AbstractDiscoveryStrategy {

    private final DiscoveryStateRepository stateRepository;

    public DiscoveryByKeywordStrategy(DropshippingPort dropshippingPort,
            DiscoveredPidRepository discoveredPidRepository, ProductDetailRepository productDetailRepository,
            DiscoveryStateRepository stateRepository, CjDropshippingProperties properties) {
        super(dropshippingPort, discoveredPidRepository, productDetailRepository, properties);
        this.stateRepository = stateRepository;
    }

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
                log.error("Error crawling keyword '{}': {}", keyword, e.getMessage(), e);
            }
        }

        log.info("BY_KEYWORD discovery completed: {} new PIDs, {} total processed, {} pages scanned", totalNew,
                totalProcessed, totalPages);

        return DiscoveryResult.builder().newPidsDiscovered(totalNew).totalPidsProcessed(totalProcessed)
                .pagesScanned(totalPages).completed(true).build();
    }

    private DiscoveryResult crawlKeyword(String keyword) {
        int maxPages = properties.getDiscovery().getMaxPagesPerKeyword();
        return crawlPages(maxPages,
                (page, pageSize) -> dropshippingPort.getProductListFiltered(page, pageSize, null, keyword, null, null,
                        3, "desc"),
                item -> buildBaseDiscoveredPid(item, item.getCategoryId(), keyword, DiscoveryStrategy.BY_KEYWORD));
    }

    private int findResumeIndex(List<String> keywords, String lastKeyword) {
        if (lastKeyword == null || lastKeyword.isBlank())
            return 0;
        int idx = keywords.indexOf(lastKeyword);
        return idx >= 0 ? idx + 1 : 0;
    }
}
