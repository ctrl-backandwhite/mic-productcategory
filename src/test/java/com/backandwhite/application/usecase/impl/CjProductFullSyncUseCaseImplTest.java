package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.SyncFailure;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjProductFullSyncUseCaseImplTest {

    @Mock
    private DropshippingPort cjClient;
    @Mock
    private ProductDetailRepository productDetailRepository;
    @Mock
    private CjProductDetailMapper cjProductDetailMapper;
    @Mock
    private SyncLogRepository syncLogRepository;
    @Mock
    private SyncFailureRepository syncFailureRepository;
    @Mock
    private ProductSearchIndexPort productSearchIndexPort;

    @InjectMocks
    private CjProductFullSyncUseCaseImpl useCase;

    @Test
    void syncAll_noPids_successSyncLog() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingProductSync(50)).thenReturn(List.of());

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getTotalItems()).isZero();
    }

    @Test
    void syncAll_success_syncsOne() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingProductSync(50)).thenReturn(List.of("pid-1"));
        CjProductDetailDto dto = new CjProductDetailDto();
        when(cjClient.getProductDetail("pid-1")).thenReturn(dto);
        ProductDetail domain = ProductDetail.builder().pid("pid-1").build();
        when(cjProductDetailMapper.toDomain(dto)).thenReturn(domain);

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getSyncedItems()).isEqualTo(1);
        verify(productDetailRepository).save(domain);
        verify(productSearchIndexPort).indexProductDetail(domain);
        verify(productDetailRepository).markProductSynced("pid-1");
    }

    @Test
    void syncAll_pidFails_recordsFailure() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingProductSync(50)).thenReturn(List.of("pid-1"));
        when(cjClient.getProductDetail("pid-1")).thenThrow(new RuntimeException("CJ"));

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getFailedItems()).isEqualTo(1);
        verify(syncFailureRepository).save(any(SyncFailure.class));
    }

    @Test
    void syncAll_globalException_rethrows() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingProductSync(50)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> useCase.syncAll(false)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void syncByPid_success() {
        CjProductDetailDto dto = new CjProductDetailDto();
        ProductDetail domain = ProductDetail.builder().pid("pid-1").build();
        when(cjClient.getProductDetail("pid-1")).thenReturn(dto);
        when(cjProductDetailMapper.toDomain(dto)).thenReturn(domain);
        CjSyncResult result = useCase.syncByPid("pid-1");
        assertThat(result.getSyncedItems()).isEqualTo(1);
    }

    @Test
    void syncByPid_failure_returnsFailed() {
        when(cjClient.getProductDetail(anyString())).thenThrow(new RuntimeException("fail"));
        CjSyncResult result = useCase.syncByPid("pid-1");
        assertThat(result.getFailedItems()).isEqualTo(1);
    }
}
