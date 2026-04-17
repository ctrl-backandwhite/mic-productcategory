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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DiscoveryByTimeStrategy implements DiscoveryStrategyExecutor {

    private final DropshippingPort dropshippingPort;
    private final DiscoveredPidRepository discoveredPidRepository;
    private final ProductDetailRepository productDetailRepository;
    private final DiscoveryStateRepository stateRepository;
    private final CjDropshippingProperties properties;

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
        Instant timeStart;
        if (state.getLastCrawledAt() != null) {
            timeStart = state.getLastCrawledAt();
        } else {
            // First run: look back 24 hours
            timeStart = now.minus(24, ChronoUnit.HOURS);
        }

        long timeStartMs = timeStart.toEpochMilli();
        long timeEndMs = now.toEpochMilli();

        log.info("BY_TIME discovery: scanning from {} to {}", timeStart, now);

        int pageSize = properties.getDiscovery().getPageSize();
        long waitMs = properties.getDiscovery().getRateLimitWaitMs();
        int page = 1;
        int totalNew = 0;
        int totalProcessed = 0;
        int maxPages = 1000;

        while (page <= maxPages) {
            try {
                CjProductListPageDto pageResult = dropshippingPort.getProductListFiltered(page, pageSize, null, null,
                        timeStartMs, timeEndMs, 3, "asc");

                if (pageResult == null || pageResult.getAllProducts().isEmpty()) {
                    break;
                }

                List<DiscoveredPid> batch = new ArrayList<>();
                for (CjProductListV2ItemDto item : pageResult.getAllProducts()) {
                    totalProcessed++;
                    String pid = item.getId();
                    if (pid == null || pid.isBlank())
                        continue;

                    if (!discoveredPidRepository.existsByPid(pid) && !productDetailRepository.existsByPid(pid)) {
                        batch.add(buildDiscoveredPid(item));
                        totalNew++;
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
            } catch (Exception e) {
                log.error("Error in BY_TIME discovery at page {}: {}", page, e.getMessage());
                break;
            }
        }

        // Update state with the time window we just processed
        state.setLastCrawledAt(now);
        state.setTotalDiscovered(state.getTotalDiscovered() + totalNew);
        state.setUpdatedAt(Instant.now());
        stateRepository.save(state);

        log.info("BY_TIME discovery completed: {} new PIDs, {} total processed, {} pages scanned", totalNew,
                totalProcessed, page);

        return DiscoveryResult.builder().newPidsDiscovered(totalNew).totalPidsProcessed(totalProcessed)
                .pagesScanned(page).completed(true).build();
    }

    private DiscoveredPid buildDiscoveredPid(CjProductListV2ItemDto item) {
        return DiscoveredPid.builder().id(UUID.randomUUID().toString()).pid(item.getId())
                .categoryId(item.getCategoryId()).strategy(DiscoveryStrategy.BY_TIME).status(DiscoveryStatus.NEW)
                .nameEn(item.getNameEn()).sellPrice(item.getSellPrice()).discoveredAt(Instant.now())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private void rateLimitWait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
