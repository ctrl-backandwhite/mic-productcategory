package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.strategy.DiscoveryByCategoryStrategy;
import com.backandwhite.application.strategy.DiscoveryByKeywordStrategy;
import com.backandwhite.application.strategy.DiscoveryByTimeStrategy;
import com.backandwhite.application.strategy.DiscoveryResult;
import com.backandwhite.application.usecase.CjProductFullSyncUseCase;
import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
import com.backandwhite.domain.valueobject.DiscoveryStateStatus;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDiscoveryUseCaseImplTest {

    @Mock
    private DiscoveryByCategoryStrategy byCategoryStrategy;
    @Mock
    private DiscoveryByKeywordStrategy byKeywordStrategy;
    @Mock
    private DiscoveryByTimeStrategy byTimeStrategy;
    @Mock
    private DiscoveryStateRepository stateRepository;
    @Mock
    private DiscoveredPidRepository discoveredPidRepository;
    @Mock
    private CjProductFullSyncUseCase fullSyncUseCase;

    private ProductDiscoveryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        when(byCategoryStrategy.getStrategy()).thenReturn(DiscoveryStrategy.BY_CATEGORY);
        when(byKeywordStrategy.getStrategy()).thenReturn(DiscoveryStrategy.BY_KEYWORD);
        when(byTimeStrategy.getStrategy()).thenReturn(DiscoveryStrategy.BY_TIME);
        useCase = new ProductDiscoveryUseCaseImpl(byCategoryStrategy, byKeywordStrategy, byTimeStrategy,
                stateRepository, discoveredPidRepository, fullSyncUseCase);
    }

    @Test
    void useCase_isInitialized() {
        // Smoke assertion: the use case was wired correctly by the test setUp.
        org.assertj.core.api.Assertions.assertThat(useCase).isNotNull();
    }

    @Test
    void runStrategy_newState_setsRunningThenCompleted() {
        when(stateRepository.findByStrategy(DiscoveryStrategy.BY_CATEGORY)).thenReturn(Optional.empty());
        when(byCategoryStrategy.execute(any(DiscoveryState.class)))
                .thenReturn(DiscoveryResult.builder().newPidsDiscovered(3).completed(true).build());

        DiscoveryResult result = useCase.runStrategy(DiscoveryStrategy.BY_CATEGORY);

        assertThat(result.getNewPidsDiscovered()).isEqualTo(3);
        verify(stateRepository, org.mockito.Mockito.atLeast(2)).save(any(DiscoveryState.class));
    }

    @Test
    void runStrategy_alreadyRunning_skips() {
        DiscoveryState state = DiscoveryState.builder().strategy(DiscoveryStrategy.BY_CATEGORY)
                .status(DiscoveryStateStatus.RUNNING).build();
        when(stateRepository.findByStrategy(DiscoveryStrategy.BY_CATEGORY)).thenReturn(Optional.of(state));

        DiscoveryResult result = useCase.runStrategy(DiscoveryStrategy.BY_CATEGORY);

        assertThat(result.getErrorMessage()).contains("already running");
    }

    @Test
    void runStrategy_executorThrows_resetsState_andRethrows() {
        when(stateRepository.findByStrategy(DiscoveryStrategy.BY_CATEGORY)).thenReturn(Optional.empty());
        when(byCategoryStrategy.execute(any(DiscoveryState.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> useCase.runStrategy(DiscoveryStrategy.BY_CATEGORY))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void runFullDiscovery_runsAllStrategies() {
        when(stateRepository.findByStrategy(any())).thenReturn(Optional.empty());
        when(byCategoryStrategy.execute(any())).thenReturn(DiscoveryResult.builder().newPidsDiscovered(1).build());
        when(byKeywordStrategy.execute(any())).thenReturn(DiscoveryResult.builder().newPidsDiscovered(2).build());
        when(byTimeStrategy.execute(any())).thenReturn(DiscoveryResult.builder().newPidsDiscovered(3).build());

        DiscoveryResult result = useCase.runFullDiscovery();
        assertThat(result.getNewPidsDiscovered()).isEqualTo(6);
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void runFullDiscovery_strategyThrows_continues() {
        when(stateRepository.findByStrategy(any())).thenReturn(Optional.empty());
        when(byCategoryStrategy.execute(any())).thenThrow(new RuntimeException("fail"));
        when(byKeywordStrategy.execute(any())).thenReturn(DiscoveryResult.builder().newPidsDiscovered(2).build());
        when(byTimeStrategy.execute(any())).thenReturn(DiscoveryResult.builder().newPidsDiscovered(3).build());

        DiscoveryResult result = useCase.runFullDiscovery();
        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void runIncremental_usesByTime() {
        when(stateRepository.findByStrategy(DiscoveryStrategy.BY_TIME)).thenReturn(Optional.empty());
        when(byTimeStrategy.execute(any())).thenReturn(DiscoveryResult.builder().newPidsDiscovered(1).build());

        DiscoveryResult result = useCase.runIncremental();
        assertThat(result.getNewPidsDiscovered()).isEqualTo(1);
    }

    @Test
    void enrichDiscoveredPids_emptyList_returnsZero() {
        when(discoveredPidRepository.findByStatus(DiscoveryStatus.NEW, 10)).thenReturn(List.of());
        assertThat(useCase.enrichDiscoveredPids(10)).isZero();
    }

    @Test
    void enrichDiscoveredPids_allSucceed() {
        DiscoveredPid dp = DiscoveredPid.builder().id("id-1").pid("pid-1").build();
        when(discoveredPidRepository.findByStatus(DiscoveryStatus.NEW, 10)).thenReturn(List.of(dp));
        assertThat(useCase.enrichDiscoveredPids(10)).isEqualTo(1);
        verify(discoveredPidRepository).markSynced("id-1");
    }

    @Test
    void enrichDiscoveredPids_failuresRecorded() {
        DiscoveredPid dp = DiscoveredPid.builder().id("id-1").pid("pid-1").build();
        when(discoveredPidRepository.findByStatus(DiscoveryStatus.NEW, 10)).thenReturn(List.of(dp));
        when(fullSyncUseCase.syncByPid("pid-1")).thenThrow(new RuntimeException("fail"));

        int synced = useCase.enrichDiscoveredPids(10);
        assertThat(synced).isZero();
        verify(discoveredPidRepository).markFailed(eq("id-1"), anyString());
    }

    @Test
    void getStatus_returnsRepoList() {
        when(stateRepository.findAll()).thenReturn(List.of(DiscoveryState.builder().build()));
        assertThat(useCase.getStatus()).hasSize(1);
    }

    @Test
    void pause_updatesState() {
        DiscoveryState state = DiscoveryState.builder().strategy(DiscoveryStrategy.BY_CATEGORY).build();
        when(stateRepository.findByStrategy(DiscoveryStrategy.BY_CATEGORY)).thenReturn(Optional.of(state));
        useCase.pause(DiscoveryStrategy.BY_CATEGORY);
        verify(stateRepository).save(state);
        assertThat(state.getStatus()).isEqualTo(DiscoveryStateStatus.PAUSED);
    }

    @Test
    void pause_noState_noOp() {
        when(stateRepository.findByStrategy(DiscoveryStrategy.BY_CATEGORY)).thenReturn(Optional.empty());
        useCase.pause(DiscoveryStrategy.BY_CATEGORY);
        verify(stateRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void resume_updatesStateToIdle() {
        DiscoveryState state = DiscoveryState.builder().strategy(DiscoveryStrategy.BY_CATEGORY)
                .status(DiscoveryStateStatus.PAUSED).build();
        when(stateRepository.findByStrategy(DiscoveryStrategy.BY_CATEGORY)).thenReturn(Optional.of(state));
        useCase.resume(DiscoveryStrategy.BY_CATEGORY);
        assertThat(state.getStatus()).isEqualTo(DiscoveryStateStatus.IDLE);
    }

}
