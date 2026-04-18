package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.infrastructure.client.cj.dto.CjInventoryByPidItemDto;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import com.backandwhite.infrastructure.db.postgres.repository.InventoryJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjInventorySyncUseCaseImplTest {

    @Mock
    private DropshippingPort cjClient;
    @Mock
    private ProductDetailRepository productDetailRepository;
    @Mock
    private SyncLogRepository syncLogRepository;
    @Mock
    private SyncFailureRepository syncFailureRepository;
    @Mock
    private InventoryJpaRepository inventoryJpaRepository;
    @Mock
    private ProductSearchIndexPort productSearchIndexPort;

    @InjectMocks
    private CjInventorySyncUseCaseImpl useCase;

    private static CjInventoryByPidItemDto inv(String vid, Integer total) {
        CjInventoryByPidItemDto i = new CjInventoryByPidItemDto();
        i.setVid(vid);
        i.setCountryCode("US");
        i.setTotalInventory(total);
        i.setCjInventory(total);
        i.setFactoryInventory(0);
        return i;
    }

    @Test
    void syncAll_noPids_successSyncLog() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingInventorySync(100)).thenReturn(List.of());

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getTotalItems()).isZero();
        verify(syncLogRepository, org.mockito.Mockito.times(2)).save(any(SyncLog.class));
    }

    @Test
    void syncAll_withForce_usesProductSyncList() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingProductSync(100)).thenReturn(List.of());

        useCase.syncAll(true);
        verify(productDetailRepository).findPidsNeedingProductSync(100);
    }

    @Test
    void syncAll_success_syncsAndCompletes() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingInventorySync(100)).thenReturn(List.of("pid-1"));
        when(cjClient.getInventoryByPid("pid-1")).thenReturn(List.of(inv("v1", 5)));
        when(inventoryJpaRepository.findByVidAndCountryCode(eq("v1"), eq("US"))).thenReturn(Optional.empty());

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getSyncedItems()).isEqualTo(1);
        verify(inventoryJpaRepository).save(any(ProductDetailVariantInventoryEntity.class));
        verify(productDetailRepository).markInventorySynced("pid-1");
        verify(productSearchIndexPort).updateStock(eq("pid-1"), any());
    }

    @Test
    void syncAll_existingInventory_updates() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingInventorySync(100)).thenReturn(List.of("pid-1"));
        when(cjClient.getInventoryByPid("pid-1")).thenReturn(List.of(inv("v1", 5)));
        ProductDetailVariantInventoryEntity existing = ProductDetailVariantInventoryEntity.builder().vid("v1")
                .countryCode("US").build();
        when(inventoryJpaRepository.findByVidAndCountryCode("v1", "US")).thenReturn(Optional.of(existing));

        useCase.syncAll(false);
        verify(inventoryJpaRepository).save(existing);
    }

    @Test
    void syncAll_nullVid_skipped() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingInventorySync(100)).thenReturn(List.of("pid-1"));
        when(cjClient.getInventoryByPid("pid-1")).thenReturn(List.of(inv(null, 5)));

        useCase.syncAll(false);
        verify(inventoryJpaRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void syncAll_exceptionPerPid_recordsFailure() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingInventorySync(100)).thenReturn(List.of("pid-1"));
        when(cjClient.getInventoryByPid("pid-1")).thenThrow(new RuntimeException("CJ"));

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getFailedItems()).isEqualTo(1);
        verify(syncFailureRepository).save(any(SyncFailure.class));
    }

    @Test
    void syncAll_globalException_rethrows() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingInventorySync(100)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> useCase.syncAll(false)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void syncByPid_success() {
        when(cjClient.getInventoryByPid("pid-1")).thenReturn(List.of(inv("v1", 5)));
        when(inventoryJpaRepository.findByVidAndCountryCode("v1", "US")).thenReturn(Optional.empty());
        CjSyncResult result = useCase.syncByPid("pid-1");
        assertThat(result.getSyncedItems()).isEqualTo(1);
    }

    @Test
    void syncByPid_failure_returnsFailedResult() {
        when(cjClient.getInventoryByPid(anyString())).thenThrow(new RuntimeException("fail"));
        CjSyncResult result = useCase.syncByPid("pid-1");
        assertThat(result.getFailedItems()).isEqualTo(1);
        assertThat(result.getSyncedItems()).isZero();
    }
}
