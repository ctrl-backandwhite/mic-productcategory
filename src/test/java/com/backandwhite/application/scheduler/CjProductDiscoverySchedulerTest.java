package com.backandwhite.application.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.strategy.DiscoveryResult;
import com.backandwhite.application.usecase.ProductDiscoveryUseCase;
import com.backandwhite.infrastructure.configuration.CjDropshippingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CjProductDiscoverySchedulerTest {

    @Mock
    private ProductDiscoveryUseCase productDiscoveryUseCase;

    private CjDropshippingProperties properties;
    private CjProductDiscoveryScheduler scheduler;

    @BeforeEach
    void setUp() {
        properties = new CjDropshippingProperties();
        properties.getDiscovery().setBatchSizeEnrich(100);
        scheduler = new CjProductDiscoveryScheduler(productDiscoveryUseCase, properties);
    }

    @Test
    void fullDiscovery_callsUseCase() {
        when(productDiscoveryUseCase.runFullDiscovery()).thenReturn(DiscoveryResult.builder().completed(true).build());
        scheduler.fullDiscovery();
        verify(productDiscoveryUseCase).runFullDiscovery();
    }

    @Test
    void fullDiscovery_exception_swallowed() {
        when(productDiscoveryUseCase.runFullDiscovery()).thenThrow(new RuntimeException("fail"));
        scheduler.fullDiscovery();
        verify(productDiscoveryUseCase).runFullDiscovery();
    }

    @Test
    void incrementalDiscovery_callsUseCase() {
        when(productDiscoveryUseCase.runIncremental()).thenReturn(DiscoveryResult.builder().completed(true).build());
        scheduler.incrementalDiscovery();
        verify(productDiscoveryUseCase).runIncremental();
    }

    @Test
    void incrementalDiscovery_exception_swallowed() {
        when(productDiscoveryUseCase.runIncremental()).thenThrow(new RuntimeException("fail"));
        scheduler.incrementalDiscovery();
        verify(productDiscoveryUseCase).runIncremental();
    }

    @Test
    void enrichDiscoveredPids_callsUseCase() {
        when(productDiscoveryUseCase.enrichDiscoveredPids(100)).thenReturn(10);
        scheduler.enrichDiscoveredPids();
        verify(productDiscoveryUseCase).enrichDiscoveredPids(100);
    }

    @Test
    void enrichDiscoveredPids_exception_swallowed() {
        when(productDiscoveryUseCase.enrichDiscoveredPids(100)).thenThrow(new RuntimeException("fail"));
        scheduler.enrichDiscoveredPids();
        verify(productDiscoveryUseCase).enrichDiscoveredPids(100);
    }
}
