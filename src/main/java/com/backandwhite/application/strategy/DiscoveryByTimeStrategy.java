package com.backandwhite.application.strategy;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DiscoveryByTimeStrategy extends AbstractDiscoveryStrategy {

    private static final int MAX_PAGES = 1000;

    private final DiscoveryStateRepository stateRepository;

    public DiscoveryByTimeStrategy(DropshippingPort dropshippingPort, DiscoveredPidRepository discoveredPidRepository,
            ProductDetailRepository productDetailRepository, DiscoveryStateRepository stateRepository,
            CjDropshippingProperties properties) {
        super(dropshippingPort, discoveredPidRepository, productDetailRepository, properties);
        this.stateRepository = stateRepository;
    }

    @Override
    public DiscoveryStrategy getStrategy() {
        return DiscoveryStrategy.BY_TIME;
    }

    @Override
    public boolean supportsResume() {
        return true;
    }

    @Override
    public DiscoveryResult execute(DiscoveryState state) {
        Instant now = Instant.now();

        // Determine time window
        Instant timeStart = (state.getLastCrawledAt() != null)
                ? state.getLastCrawledAt()
                : now.minus(24, ChronoUnit.HOURS);

        long timeStartMs = timeStart.toEpochMilli();
        long timeEndMs = now.toEpochMilli();

        log.info("BY_TIME discovery: scanning from {} to {}", timeStart, now);

        DiscoveryResult result;
        try {
            result = crawlPages(MAX_PAGES,
                    (page, pageSize) -> dropshippingPort.getProductListFiltered(page, pageSize, null, null, timeStartMs,
                            timeEndMs, 3, "asc"),
                    item -> buildBaseDiscoveredPid(item, item.getCategoryId(), null, DiscoveryStrategy.BY_TIME));
        } catch (Exception e) {
            log.error("Error in BY_TIME discovery: {}", e.getMessage(), e);
            result = DiscoveryResult.builder().build();
        }

        // Update state with the time window we just processed
        state.setLastCrawledAt(now);
        state.setTotalDiscovered(state.getTotalDiscovered() + result.getNewPidsDiscovered());
        state.setUpdatedAt(Instant.now());
        stateRepository.save(state);

        log.info("BY_TIME discovery completed: {} new PIDs, {} total processed, {} pages scanned",
                result.getNewPidsDiscovered(), result.getTotalPidsProcessed(), result.getPagesScanned());

        return DiscoveryResult.builder().newPidsDiscovered(result.getNewPidsDiscovered())
                .totalPidsProcessed(result.getTotalPidsProcessed()).pagesScanned(result.getPagesScanned())
                .completed(true).build();
    }
}
