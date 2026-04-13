package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.strategy.DiscoveryByCategoryStrategy;
import com.backandwhite.application.strategy.DiscoveryByKeywordStrategy;
import com.backandwhite.application.strategy.DiscoveryByTimeStrategy;
import com.backandwhite.application.strategy.DiscoveryResult;
import com.backandwhite.application.strategy.DiscoveryStrategyExecutor;
import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import com.backandwhite.application.usecase.ProductDiscoveryUseCase;
import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
import com.backandwhite.domain.valueobject.DiscoveryStateStatus;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ProductDiscoveryUseCaseImpl implements ProductDiscoveryUseCase {

    private final Map<DiscoveryStrategy, DiscoveryStrategyExecutor> strategyMap;
    private final DiscoveryStateRepository stateRepository;
    private final DiscoveredPidRepository discoveredPidRepository;
    private final CjProductFullSyncUseCase fullSyncUseCase;

    public ProductDiscoveryUseCaseImpl(
            DiscoveryByCategoryStrategy byCategoryStrategy,
            DiscoveryByKeywordStrategy byKeywordStrategy,
            DiscoveryByTimeStrategy byTimeStrategy,
            DiscoveryStateRepository stateRepository,
            DiscoveredPidRepository discoveredPidRepository,
            CjProductFullSyncUseCase fullSyncUseCase) {

        this.stateRepository = stateRepository;
        this.discoveredPidRepository = discoveredPidRepository;
        this.fullSyncUseCase = fullSyncUseCase;

        this.strategyMap = List.of(byCategoryStrategy, byKeywordStrategy, byTimeStrategy)
                .stream()
                .collect(Collectors.toMap(DiscoveryStrategyExecutor::getStrategy, Function.identity()));
    }

    @Override
    public DiscoveryResult runFullDiscovery() {
        log.info("Starting FULL discovery (all strategies)...");
        int totalNew = 0;
        int totalProcessed = 0;
        int totalPages = 0;

        for (DiscoveryStrategy strategy : List.of(
                DiscoveryStrategy.BY_CATEGORY, DiscoveryStrategy.BY_KEYWORD, DiscoveryStrategy.BY_TIME)) {
            try {
                var result = runStrategy(strategy);
                totalNew += result.getNewPidsDiscovered();
                totalProcessed += result.getTotalPidsProcessed();
                totalPages += result.getPagesScanned();
            } catch (Exception e) {
                log.error("Strategy {} failed: {}", strategy, e.getMessage(), e);
            }
        }

        log.info("FULL discovery completed: {} new PIDs, {} processed, {} pages",
                totalNew, totalProcessed, totalPages);

        return DiscoveryResult.builder()
                .newPidsDiscovered(totalNew)
                .totalPidsProcessed(totalProcessed)
                .pagesScanned(totalPages)
                .completed(true)
                .build();
    }

    @Override
    public DiscoveryResult runStrategy(DiscoveryStrategy strategy) {
        DiscoveryStrategyExecutor executor = strategyMap.get(strategy);
        if (executor == null) {
            return DiscoveryResult.builder()
                    .errorMessage("Unknown strategy: " + strategy)
                    .build();
        }

        DiscoveryState state = stateRepository.findByStrategy(strategy)
                .orElse(DiscoveryState.builder()
                        .strategy(strategy)
                        .status(DiscoveryStateStatus.IDLE)
                        .totalDiscovered(0)
                        .build());

        if (state.getStatus() == DiscoveryStateStatus.RUNNING) {
            log.warn("Strategy {} is already running — skipping", strategy);
            return DiscoveryResult.builder()
                    .errorMessage("Strategy already running")
                    .build();
        }

        // Mark as running
        state.setStatus(DiscoveryStateStatus.RUNNING);
        state.setLastRunAt(Instant.now());
        state.setUpdatedAt(Instant.now());
        stateRepository.save(state);

        try {
            DiscoveryResult result = executor.execute(state);

            // Mark completed
            state.setStatus(DiscoveryStateStatus.COMPLETED);
            state.setUpdatedAt(Instant.now());
            stateRepository.save(state);

            return result;
        } catch (Exception e) {
            state.setStatus(DiscoveryStateStatus.IDLE);
            state.setUpdatedAt(Instant.now());
            stateRepository.save(state);
            throw e;
        }
    }

    @Override
    public DiscoveryResult runIncremental() {
        log.info("Starting INCREMENTAL discovery (BY_TIME only)...");
        return runStrategy(DiscoveryStrategy.BY_TIME);
    }

    @Override
    public int enrichDiscoveredPids(int batchSize) {
        log.info("Starting enrichment of discovered PIDs (batch size: {})...", batchSize);

        List<DiscoveredPid> pidsToEnrich = discoveredPidRepository.findByStatus(DiscoveryStatus.NEW, batchSize);
        if (pidsToEnrich.isEmpty()) {
            log.info("No NEW discovered PIDs to enrich");
            return 0;
        }

        int synced = 0;
        int failed = 0;

        for (DiscoveredPid dp : pidsToEnrich) {
            try {
                fullSyncUseCase.syncByPid(dp.getPid());
                discoveredPidRepository.markSynced(dp.getId());
                synced++;
            } catch (Exception e) {
                log.warn("Failed to enrich pid={}: {}", dp.getPid(), e.getMessage());
                discoveredPidRepository.markFailed(dp.getId(), e.getMessage());
                failed++;
            }
        }

        log.info("Enrichment batch completed: {} synced, {} failed out of {} total", synced, failed,
                pidsToEnrich.size());
        return synced;
    }

    @Override
    public List<DiscoveryState> getStatus() {
        return stateRepository.findAll();
    }

    @Override
    public void pause(DiscoveryStrategy strategy) {
        stateRepository.findByStrategy(strategy).ifPresent(state -> {
            state.setStatus(DiscoveryStateStatus.PAUSED);
            state.setUpdatedAt(Instant.now());
            stateRepository.save(state);
            log.info("Strategy {} paused", strategy);
        });
    }

    @Override
    public void resume(DiscoveryStrategy strategy) {
        stateRepository.findByStrategy(strategy).ifPresent(state -> {
            state.setStatus(DiscoveryStateStatus.IDLE);
            state.setUpdatedAt(Instant.now());
            stateRepository.save(state);
            log.info("Strategy {} resumed (set to IDLE)", strategy);
        });
    }
}
