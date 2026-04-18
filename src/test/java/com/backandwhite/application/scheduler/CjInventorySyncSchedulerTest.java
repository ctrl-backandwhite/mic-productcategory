package com.backandwhite.application.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.usecase.CjInventorySyncUseCase;
import com.backandwhite.domain.model.CjSyncResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjInventorySyncSchedulerTest {

    @Mock
    private CjInventorySyncUseCase cjInventorySyncUseCase;

    @InjectMocks
    private CjInventorySyncScheduler scheduler;

    @Test
    void syncInventory_callsUseCase() {
        when(cjInventorySyncUseCase.syncAll(false))
                .thenReturn(CjSyncResult.builder().totalItems(1).syncedItems(1).build());
        scheduler.syncInventory();
        verify(cjInventorySyncUseCase).syncAll(false);
    }

    @Test
    void syncInventory_exception_loggedAndSwallowed() {
        when(cjInventorySyncUseCase.syncAll(false)).thenThrow(new RuntimeException("fail"));
        scheduler.syncInventory();
        verify(cjInventorySyncUseCase).syncAll(false);
    }
}
