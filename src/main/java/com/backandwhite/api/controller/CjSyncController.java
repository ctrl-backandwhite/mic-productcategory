package com.backandwhite.api.controller;

import com.backandwhite.api.dto.out.CjSyncResultDtoOut;
import com.backandwhite.api.dto.out.SyncLogDtoOut;
import com.backandwhite.application.usecase.CjInventorySyncUseCase;
import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import com.backandwhite.application.usecase.CjReviewSyncUseCase;
import com.backandwhite.application.usecase.ProductSearchReindexUseCase;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sync")
@Tag(name = "CJ Sync", description = "Manual sync endpoints with CJ Dropshipping")
public class CjSyncController {

    private final CjInventorySyncUseCase cjInventorySyncUseCase;
    private final CjProductFullSyncUseCase cjProductFullSyncUseCase;
    private final CjReviewSyncUseCase cjReviewSyncUseCase;
    private final ProductSearchReindexUseCase productSearchReindexUseCase;
    private final SyncLogRepository syncLogRepository;

    // ── Inventory ─────────────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping("/inventory/all")
    @Operation(summary = "Sync inventory for all products", description = "Launches an inventory sync for all products with outdated data (>4h)")
    public ResponseEntity<CjSyncResultDtoOut> syncAllInventory(
            @Parameter(description = "Force re-sync even if inventory is recent") @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(toDto(cjInventorySyncUseCase.syncAll(force)));
    }

    @NxAdmin
    @PostMapping("/inventory/product/{pid}")
    @Operation(summary = "Sync inventory for a specific product")
    public ResponseEntity<CjSyncResultDtoOut> syncInventoryByPid(@PathVariable String pid) {
        return ResponseEntity.ok(toDto(cjInventorySyncUseCase.syncByPid(pid)));
    }

    // ── Product full sync ─────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping("/product/all")
    @Operation(summary = "Sync full data for all products", description = "Launches a full sync (name, description, images, variants) for outdated products")
    public ResponseEntity<CjSyncResultDtoOut> syncAllProducts(
            @Parameter(description = "Force re-sync even if product is recent") @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(toDto(cjProductFullSyncUseCase.syncAll(force)));
    }

    @NxAdmin
    @PostMapping("/product/{pid}")
    @Operation(summary = "Sync full data for a specific product")
    public ResponseEntity<CjSyncResultDtoOut> syncProductByPid(@PathVariable String pid) {
        return ResponseEntity.ok(toDto(cjProductFullSyncUseCase.syncByPid(pid)));
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping("/reviews/all")
    @Operation(summary = "Sync CJ reviews for all products", description = "Imports new reviews from CJ for products without a sync today")
    public ResponseEntity<CjSyncResultDtoOut> syncAllReviews(
            @Parameter(description = "Force re-sync even if product was already synced today") @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(toDto(cjReviewSyncUseCase.syncAll(force)));
    }

    @NxAdmin
    @PostMapping("/reviews/product/{pid}")
    @Operation(summary = "Sync CJ reviews for a specific product")
    public ResponseEntity<CjSyncResultDtoOut> syncReviewsByPid(@PathVariable String pid) {
        return ResponseEntity.ok(toDto(cjReviewSyncUseCase.syncByPid(pid)));
    }

    // ── Monitoring ────────────────────────────────────────────────────────────

    @NxAdmin
    @GetMapping("/log/{syncType}")
    @Operation(summary = "View recent sync history", description = "Returns the last 10 sync records for the specified type")
    public ResponseEntity<List<SyncLogDtoOut>> getSyncLog(
            @Parameter(description = "Sync type: INVENTORY, PRODUCT_FULL, REVIEWS, CATEGORIES") @PathVariable String syncType) {
        List<SyncLog> logs = syncLogRepository.findRecentByType(SyncType.valueOf(syncType.toUpperCase()), 10);
        return ResponseEntity.ok(logs.stream().map(this::toLogDto).toList());
    }

    // ── Elasticsearch ───────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping("/reindex/elasticsearch")
    @Operation(summary = "Reindex all products in Elasticsearch", description = "Drops the index, recreates it and reindexes all products from PostgreSQL")
    public ResponseEntity<java.util.Map<String, Object>> reindexElasticsearch() {
        long indexed = productSearchReindexUseCase.reindexAll();
        return ResponseEntity.accepted()
                .body(java.util.Map.of("status", "completed", "operation", "full-reindex", "totalIndexed", indexed));
    }

    @NxAdmin
    @PostMapping("/reindex/elasticsearch/from-db")
    @Operation(summary = "Reindex products from DB (incremental)", description = "Bulk upsert from PostgreSQL without dropping the index. "
            + "Useful for syncing without losing existing documents.")
    public ResponseEntity<java.util.Map<String, Object>> reindexFromDb() {
        long indexed = productSearchReindexUseCase.reindexFromDb();
        return ResponseEntity.accepted().body(
                java.util.Map.of("status", "completed", "operation", "incremental-reindex", "totalIndexed", indexed));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private CjSyncResultDtoOut toDto(CjSyncResult result) {
        return CjSyncResultDtoOut.builder().totalItems(result.getTotalItems()).syncedItems(result.getSyncedItems())
                .failedItems(result.getFailedItems()).skippedItems(result.getSkippedItems())
                .durationMs(result.getDurationMs()).syncLogId(result.getSyncLogId()).build();
    }

    private SyncLogDtoOut toLogDto(SyncLog log) {
        return SyncLogDtoOut.builder().id(log.getId())
                .syncType(log.getSyncType() != null ? log.getSyncType().name() : null)
                .status(log.getStatus() != null ? log.getStatus().name() : null).startedAt(log.getStartedAt())
                .finishedAt(log.getFinishedAt()).totalItems(log.getTotalItems()).syncedItems(log.getSyncedItems())
                .failedItems(log.getFailedItems()).skippedItems(log.getSkippedItems())
                .errorMessage(log.getErrorMessage()).build();
    }
}
