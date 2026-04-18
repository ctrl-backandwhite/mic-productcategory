package com.backandwhite.application.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjProductFullSyncSchedulerTest {

    @Mock
    private CjProductFullSyncUseCase cjProductFullSyncUseCase;

    @InjectMocks
    private CjProductFullSyncScheduler scheduler;

    @Test
    void syncProducts_callsUseCase() {
        when(cjProductFullSyncUseCase.syncAll(false))
                .thenReturn(CjSyncResult.builder().totalItems(1).syncedItems(1).build());
        scheduler.syncProducts();
        verify(cjProductFullSyncUseCase).syncAll(false);
    }

    @Test
    void syncProducts_exception_loggedAndSwallowed() {
        when(cjProductFullSyncUseCase.syncAll(false)).thenThrow(new RuntimeException("fail"));
        scheduler.syncProducts();
        verify(cjProductFullSyncUseCase).syncAll(false);
    }
}
