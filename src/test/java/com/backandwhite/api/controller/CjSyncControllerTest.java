package com.backandwhite.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.out.CjSyncResultDtoOut;
import com.backandwhite.api.dto.out.SyncLogDtoOut;
import com.backandwhite.application.usecase.CjInventorySyncUseCase;
import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import com.backandwhite.application.usecase.CjReviewSyncUseCase;
import com.backandwhite.application.usecase.ProductSearchReindexUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.domain.valueobject.SyncStatus;
import com.backandwhite.domain.valueobject.SyncType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CjSyncControllerTest {

    @Mock
    private CjInventorySyncUseCase cjInventorySyncUseCase;
    @Mock
    private CjProductFullSyncUseCase cjProductFullSyncUseCase;
    @Mock
    private CjReviewSyncUseCase cjReviewSyncUseCase;
    @Mock
    private ProductSearchReindexUseCase productSearchReindexUseCase;
    @Mock
    private SyncLogRepository syncLogRepository;

    @InjectMocks
    private CjSyncController controller;

    @Test
    void syncAllInventory_returnsResult() {
        when(cjInventorySyncUseCase.syncAll(false)).thenReturn(CjSyncResult.builder().syncedItems(5).build());
        ResponseEntity<CjSyncResultDtoOut> response = controller.syncAllInventory(false);
        assertThat(response.getBody().getSyncedItems()).isEqualTo(5);
    }

    @Test
    void syncInventoryByPid_returnsResult() {
        when(cjInventorySyncUseCase.syncByPid("pid")).thenReturn(CjSyncResult.builder().syncedItems(1).build());
        ResponseEntity<CjSyncResultDtoOut> response = controller.syncInventoryByPid("pid");
        assertThat(response.getBody().getSyncedItems()).isEqualTo(1);
    }

    @Test
    void syncAllProducts_returnsResult() {
        when(cjProductFullSyncUseCase.syncAll(true)).thenReturn(CjSyncResult.builder().syncedItems(10).build());
        ResponseEntity<CjSyncResultDtoOut> response = controller.syncAllProducts(true);
        assertThat(response.getBody().getSyncedItems()).isEqualTo(10);
    }

    @Test
    void syncProductByPid_returnsResult() {
        when(cjProductFullSyncUseCase.syncByPid("pid")).thenReturn(CjSyncResult.builder().syncedItems(1).build());
        ResponseEntity<CjSyncResultDtoOut> response = controller.syncProductByPid("pid");
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void syncAllReviews_returnsResult() {
        when(cjReviewSyncUseCase.syncAll(false)).thenReturn(CjSyncResult.builder().syncedItems(3).build());
        ResponseEntity<CjSyncResultDtoOut> response = controller.syncAllReviews(false);
        assertThat(response.getBody().getSyncedItems()).isEqualTo(3);
    }

    @Test
    void syncReviewsByPid_returnsResult() {
        when(cjReviewSyncUseCase.syncByPid("pid")).thenReturn(CjSyncResult.builder().syncedItems(1).build());
        ResponseEntity<CjSyncResultDtoOut> response = controller.syncReviewsByPid("pid");
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getSyncLog_returnsList() {
        SyncLog log = SyncLog.builder().id("1").syncType(SyncType.INVENTORY).status(SyncStatus.SUCCESS).build();
        when(syncLogRepository.findRecentByType(SyncType.INVENTORY, 10)).thenReturn(List.of(log));
        ResponseEntity<List<SyncLogDtoOut>> response = controller.getSyncLog("inventory");
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getSyncLog_nullSyncType_nullStatus_handled() {
        SyncLog log = SyncLog.builder().id("1").syncType(null).status(null).build();
        when(syncLogRepository.findRecentByType(SyncType.INVENTORY, 10)).thenReturn(List.of(log));
        ResponseEntity<List<SyncLogDtoOut>> response = controller.getSyncLog("INVENTORY");
        assertThat(response.getBody().getFirst().getSyncType()).isNull();
        assertThat(response.getBody().getFirst().getStatus()).isNull();
    }

    @Test
    void reindexElasticsearch_returnsAccepted() {
        when(productSearchReindexUseCase.reindexAll()).thenReturn(10L);
        ResponseEntity<Map<String, Object>> response = controller.reindexElasticsearch();
        assertThat(response.getBody().get("totalIndexed")).isEqualTo(10L);
    }

    @Test
    void reindexFromDb_returnsAccepted() {
        when(productSearchReindexUseCase.reindexFromDb()).thenReturn(7L);
        ResponseEntity<Map<String, Object>> response = controller.reindexFromDb();
        assertThat(response.getBody().get("totalIndexed")).isEqualTo(7L);
    }
}
