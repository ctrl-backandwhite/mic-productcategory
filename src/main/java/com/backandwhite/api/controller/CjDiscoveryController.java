package com.backandwhite.api.controller;

import com.backandwhite.api.dto.out.DiscoveryResultDtoOut;
import com.backandwhite.api.dto.out.DiscoveryStatsDtoOut;
import com.backandwhite.api.dto.out.DiscoveryStatusDtoOut;
import com.backandwhite.application.strategy.DiscoveryResult;
import com.backandwhite.application.usecase.ProductDiscoveryUseCase;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discovery")
@Tag(name = "CJ Discovery", description = "Product discovery crawler management endpoints")
public class CjDiscoveryController {

    private final ProductDiscoveryUseCase productDiscoveryUseCase;
    private final DiscoveredPidRepository discoveredPidRepository;
    private final CjDropshippingProperties properties;

    @NxAdmin
    @PostMapping("/run/full")
    @Operation(summary = "Trigger full discovery (all strategies)")
    public ResponseEntity<DiscoveryResultDtoOut> runFull() {
        DiscoveryResult result = productDiscoveryUseCase.runFullDiscovery();
        return ResponseEntity.ok(toDto(result));
    }

    @NxAdmin
    @PostMapping("/run/incremental")
    @Operation(summary = "Trigger incremental discovery (BY_TIME only)")
    public ResponseEntity<DiscoveryResultDtoOut> runIncremental() {
        DiscoveryResult result = productDiscoveryUseCase.runIncremental();
        return ResponseEntity.ok(toDto(result));
    }

    @NxAdmin
    @PostMapping("/run/{strategy}")
    @Operation(summary = "Trigger a specific discovery strategy")
    public ResponseEntity<DiscoveryResultDtoOut> runStrategy(@PathVariable DiscoveryStrategy strategy) {
        DiscoveryResult result = productDiscoveryUseCase.runStrategy(strategy);
        return ResponseEntity.ok(toDto(result));
    }

    @NxAdmin
    @PostMapping("/enrich")
    @Operation(summary = "Trigger enrichment batch for discovered PIDs")
    public ResponseEntity<String> enrich(@RequestParam(defaultValue = "500") int batchSize) {
        int synced = productDiscoveryUseCase.enrichDiscoveredPids(batchSize);
        return ResponseEntity.ok("Enriched " + synced + " PIDs");
    }

    @NxAdmin
    @PostMapping("/pause/{strategy}")
    @Operation(summary = "Pause a running discovery strategy")
    public ResponseEntity<Void> pause(@PathVariable DiscoveryStrategy strategy) {
        productDiscoveryUseCase.pause(strategy);
        return ResponseEntity.ok().build();
    }

    @NxAdmin
    @PostMapping("/resume/{strategy}")
    @Operation(summary = "Resume a paused discovery strategy")
    public ResponseEntity<Void> resume(@PathVariable DiscoveryStrategy strategy) {
        productDiscoveryUseCase.resume(strategy);
        return ResponseEntity.ok().build();
    }

    @NxAdmin
    @GetMapping("/status")
    @Operation(summary = "Get status of all discovery strategies")
    public ResponseEntity<List<DiscoveryStatusDtoOut>> getStatus() {
        List<DiscoveryState> states = productDiscoveryUseCase.getStatus();
        List<DiscoveryStatusDtoOut> dtos = states.stream()
                .map(s -> DiscoveryStatusDtoOut.builder().strategy(s.getStrategy()).status(s.getStatus())
                        .totalDiscovered(s.getTotalDiscovered()).lastRunAt(s.getLastRunAt())
                        .lastCrawledAt(s.getLastCrawledAt()).lastCategoryId(s.getLastCategoryId())
                        .lastKeyword(s.getLastKeyword()).build())
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @NxAdmin
    @GetMapping("/stats")
    @Operation(summary = "Get discovery statistics")
    public ResponseEntity<DiscoveryStatsDtoOut> getStats() {
        DiscoveryStatsDtoOut stats = DiscoveryStatsDtoOut.builder().totalDiscovered(discoveredPidRepository.countAll())
                .statusNew(discoveredPidRepository.countByStatus(DiscoveryStatus.NEW))
                .statusQueued(discoveredPidRepository.countByStatus(DiscoveryStatus.QUEUED))
                .statusSynced(discoveredPidRepository.countByStatus(DiscoveryStatus.SYNCED))
                .statusFailed(discoveredPidRepository.countByStatus(DiscoveryStatus.FAILED))
                .statusSkipped(discoveredPidRepository.countByStatus(DiscoveryStatus.SKIPPED))
                .byCategory(discoveredPidRepository.countByStrategy(DiscoveryStrategy.BY_CATEGORY))
                .byKeyword(discoveredPidRepository.countByStrategy(DiscoveryStrategy.BY_KEYWORD))
                .byTime(discoveredPidRepository.countByStrategy(DiscoveryStrategy.BY_TIME)).build();
        return ResponseEntity.ok(stats);
    }

    private DiscoveryResultDtoOut toDto(DiscoveryResult result) {
        return DiscoveryResultDtoOut.builder().newPidsDiscovered(result.getNewPidsDiscovered())
                .totalPidsProcessed(result.getTotalPidsProcessed()).pagesScanned(result.getPagesScanned())
                .completed(result.isCompleted()).errorMessage(result.getErrorMessage()).build();
    }
}
