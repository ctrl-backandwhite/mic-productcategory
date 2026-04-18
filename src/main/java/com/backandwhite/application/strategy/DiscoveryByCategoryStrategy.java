package com.backandwhite.application.strategy;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.CategoryRepository;
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
public class DiscoveryByCategoryStrategy extends AbstractDiscoveryStrategy {

    private final CategoryRepository categoryRepository;
    private final DiscoveryStateRepository stateRepository;

    public DiscoveryByCategoryStrategy(DropshippingPort dropshippingPort,
            DiscoveredPidRepository discoveredPidRepository, ProductDetailRepository productDetailRepository,
            CategoryRepository categoryRepository, DiscoveryStateRepository stateRepository,
            CjDropshippingProperties properties) {
        super(dropshippingPort, discoveredPidRepository, productDetailRepository, properties);
        this.categoryRepository = categoryRepository;
        this.stateRepository = stateRepository;
    }

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
                    log.info("Category {}: {} new PIDs (total processed: {})", catId, catResult.getNewPidsDiscovered(),
                            catResult.getTotalPidsProcessed());
                }
            } catch (Exception e) {
                log.error("Error crawling category {}: {}", catId, e.getMessage(), e);
                // Continue to next category instead of failing
            }
        }

        log.info("BY_CATEGORY discovery completed: {} new PIDs, {} total processed, {} pages scanned", totalNew,
                totalProcessed, totalPages);

        return DiscoveryResult.builder().newPidsDiscovered(totalNew).totalPidsProcessed(totalProcessed)
                .pagesScanned(totalPages).completed(true).build();
    }

    private DiscoveryResult crawlCategory(String categoryId) {
        int maxPages = properties.getDiscovery().getMaxPagesPerCategory();
        return crawlPages(maxPages,
                (page, pageSize) -> dropshippingPort.getProductListFiltered(page, pageSize, categoryId, null, null,
                        null, 3, "desc"),
                item -> buildBaseDiscoveredPid(item, categoryId, null, DiscoveryStrategy.BY_CATEGORY));
    }

    private int findResumeIndex(List<String> categoryIds, String lastCategoryId) {
        if (lastCategoryId == null || lastCategoryId.isBlank())
            return 0;
        int idx = categoryIds.indexOf(lastCategoryId);
        return idx >= 0 ? idx + 1 : 0;
    }
}
