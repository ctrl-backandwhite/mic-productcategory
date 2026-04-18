package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.domain.model.CjSyncResult;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.SyncLog;
import com.backandwhite.domain.repository.ProductDetailRepository;
import com.backandwhite.domain.repository.ReviewRepository;
import com.backandwhite.domain.repository.SyncFailureRepository;
import com.backandwhite.domain.repository.SyncLogRepository;
import com.backandwhite.infrastructure.client.cj.dto.CjProductCommentsPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjReviewItemDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjReviewMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjReviewSyncUseCaseImplTest {

    @Mock
    private DropshippingPort cjClient;
    @Mock
    private ProductDetailRepository productDetailRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private CjReviewMapper cjReviewMapper;
    @Mock
    private SyncLogRepository syncLogRepository;
    @Mock
    private SyncFailureRepository syncFailureRepository;

    @InjectMocks
    private CjReviewSyncUseCaseImpl useCase;

    private static CjProductCommentsPageDto pageWith(List<CjReviewItemDto> list) {
        CjProductCommentsPageDto page = new CjProductCommentsPageDto();
        page.setList(list);
        return page;
    }

    private static CjReviewItemDto item(String id) {
        CjReviewItemDto i = new CjReviewItemDto();
        i.setId(id);
        return i;
    }

    @Test
    void syncAll_noPids_success() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingReviewsSync(50)).thenReturn(List.of());
        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getTotalItems()).isZero();
    }

    @Test
    void syncAll_success_savesNewReviews() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingReviewsSync(50)).thenReturn(List.of("pid-1"));
        when(cjClient.getProductComments(eq("pid-1"), eq(0), anyInt(), anyInt()))
                .thenReturn(pageWith(List.of(item("r1"))));
        when(reviewRepository.existsByExternalReviewId("r1")).thenReturn(false);
        when(cjReviewMapper.toDomain(any(CjReviewItemDto.class))).thenReturn(new Review());

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getSyncedItems()).isEqualTo(1);
        verify(reviewRepository).saveAll(any());
        verify(productDetailRepository).markReviewsSynced("pid-1");
    }

    @Test
    void syncAll_existingReviewSkipped() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingReviewsSync(50)).thenReturn(List.of("pid-1"));
        when(cjClient.getProductComments(eq("pid-1"), eq(0), anyInt(), anyInt()))
                .thenReturn(pageWith(List.of(item("r1"))));
        when(reviewRepository.existsByExternalReviewId("r1")).thenReturn(true);

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getSyncedItems()).isEqualTo(1);
        verify(reviewRepository, org.mockito.Mockito.never()).saveAll(any());
    }

    @Test
    void syncAll_emptyList_breaksLoop() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingReviewsSync(50)).thenReturn(List.of("pid-1"));
        when(cjClient.getProductComments(eq("pid-1"), eq(0), anyInt(), anyInt()))
                .thenReturn(pageWith(new ArrayList<>()));

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getSyncedItems()).isEqualTo(1);
    }

    @Test
    void syncAll_exception_recordsFailure() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingReviewsSync(50)).thenReturn(List.of("pid-1"));
        when(cjClient.getProductComments(eq("pid-1"), eq(0), anyInt(), anyInt())).thenThrow(new RuntimeException("CJ"));

        CjSyncResult result = useCase.syncAll(false);
        assertThat(result.getFailedItems()).isEqualTo(1);
    }

    @Test
    void syncAll_globalException_rethrows() {
        when(syncLogRepository.save(any(SyncLog.class)))
                .thenAnswer(i -> ((SyncLog) i.getArgument(0)).toBuilder().id("log-1").build());
        when(productDetailRepository.findPidsNeedingReviewsSync(50)).thenThrow(new RuntimeException("boom"));
        assertThatThrownBy(() -> useCase.syncAll(false)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void syncByPid_success() {
        when(cjClient.getProductComments(eq("pid-1"), eq(0), anyInt(), anyInt()))
                .thenReturn(pageWith(List.of(item("r1"))));
        when(reviewRepository.existsByExternalReviewId("r1")).thenReturn(false);
        when(cjReviewMapper.toDomain(any(CjReviewItemDto.class))).thenReturn(new Review());
        CjSyncResult result = useCase.syncByPid("pid-1");
        assertThat(result.getSyncedItems()).isEqualTo(1);
    }

    @Test
    void syncByPid_failure_returnsFailed() {
        when(cjClient.getProductComments(anyString(), anyInt(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("fail"));
        CjSyncResult result = useCase.syncByPid("pid-1");
        assertThat(result.getFailedItems()).isEqualTo(1);
    }

    @Test
    void syncByPid_nullId_skipped() {
        when(cjClient.getProductComments(eq("pid-1"), eq(0), anyInt(), anyInt()))
                .thenReturn(pageWith(List.of(item(null))));
        CjSyncResult result = useCase.syncByPid("pid-1");
        assertThat(result.getSyncedItems()).isZero();
    }
}
