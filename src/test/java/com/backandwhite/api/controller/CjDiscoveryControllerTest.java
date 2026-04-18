package com.backandwhite.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.out.DiscoveryResultDtoOut;
import com.backandwhite.api.dto.out.DiscoveryStatsDtoOut;
import com.backandwhite.api.dto.out.DiscoveryStatusDtoOut;
import com.backandwhite.application.strategy.DiscoveryResult;
import com.backandwhite.application.usecase.ProductDiscoveryUseCase;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.valueobject.DiscoveryStateStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CjDiscoveryControllerTest {

    @Mock
    private ProductDiscoveryUseCase productDiscoveryUseCase;
    @Mock
    private DiscoveredPidRepository discoveredPidRepository;
    @Mock
    private CjDropshippingProperties properties;

    @InjectMocks
    private CjDiscoveryController controller;

    @Test
    void runFull_returnsResult() {
        when(productDiscoveryUseCase.runFullDiscovery())
                .thenReturn(DiscoveryResult.builder().newPidsDiscovered(5).completed(true).build());
        ResponseEntity<DiscoveryResultDtoOut> response = controller.runFull();
        assertThat(response.getBody().getNewPidsDiscovered()).isEqualTo(5);
    }

    @Test
    void runIncremental_returnsResult() {
        when(productDiscoveryUseCase.runIncremental())
                .thenReturn(DiscoveryResult.builder().newPidsDiscovered(2).build());
        ResponseEntity<DiscoveryResultDtoOut> response = controller.runIncremental();
        assertThat(response.getBody().getNewPidsDiscovered()).isEqualTo(2);
    }

    @Test
    void runStrategy_returnsResult() {
        when(productDiscoveryUseCase.runStrategy(DiscoveryStrategy.BY_CATEGORY))
                .thenReturn(DiscoveryResult.builder().build());
        ResponseEntity<DiscoveryResultDtoOut> response = controller.runStrategy(DiscoveryStrategy.BY_CATEGORY);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void enrich_returnsSyncedCount() {
        when(productDiscoveryUseCase.enrichDiscoveredPids(10)).thenReturn(5);
        ResponseEntity<String> response = controller.enrich(10);
        assertThat(response.getBody()).contains("5");
    }

    @Test
    void pause_callsUseCase() {
        ResponseEntity<Void> response = controller.pause(DiscoveryStrategy.BY_TIME);
        verify(productDiscoveryUseCase).pause(DiscoveryStrategy.BY_TIME);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void resume_callsUseCase() {
        ResponseEntity<Void> response = controller.resume(DiscoveryStrategy.BY_TIME);
        verify(productDiscoveryUseCase).resume(DiscoveryStrategy.BY_TIME);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void getStatus_returnsStates() {
        DiscoveryState state = DiscoveryState.builder().strategy(DiscoveryStrategy.BY_CATEGORY)
                .status(DiscoveryStateStatus.IDLE).totalDiscovered(10).build();
        when(productDiscoveryUseCase.getStatus()).thenReturn(List.of(state));
        ResponseEntity<List<DiscoveryStatusDtoOut>> response = controller.getStatus();
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getStats_returnsStats() {
        when(discoveredPidRepository.countAll()).thenReturn(100L);
        ResponseEntity<DiscoveryStatsDtoOut> response = controller.getStats();
        assertThat(response.getBody().getTotalDiscovered()).isEqualTo(100L);
    }
}
