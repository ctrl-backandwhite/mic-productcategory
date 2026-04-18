package com.backandwhite.application.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.usecase.CjReviewSyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjReviewSyncSchedulerTest {

    @Mock
    private CjReviewSyncUseCase cjReviewSyncUseCase;

    @InjectMocks
    private CjReviewSyncScheduler scheduler;

    @Test
    void syncReviews_callsUseCase() {
        when(cjReviewSyncUseCase.syncAll(false))
                .thenReturn(CjSyncResult.builder().totalItems(1).syncedItems(1).build());
        scheduler.syncReviews();
        verify(cjReviewSyncUseCase).syncAll(false);
    }

    @Test
    void syncReviews_exception_loggedAndSwallowed() {
        when(cjReviewSyncUseCase.syncAll(false)).thenThrow(new RuntimeException("fail"));
        scheduler.syncReviews();
        verify(cjReviewSyncUseCase).syncAll(false);
    }
}
