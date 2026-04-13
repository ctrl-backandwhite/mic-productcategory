package com.backandwhite.application.usecase;

import com.backandwhite.application.strategy.DiscoveryResult;
import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;

import java.util.List;

public interface ProductDiscoveryUseCase {

    DiscoveryResult runFullDiscovery();

    DiscoveryResult runStrategy(DiscoveryStrategy strategy);

    DiscoveryResult runIncremental();

    int enrichDiscoveredPids(int batchSize);

    List<DiscoveryState> getStatus();

    void pause(DiscoveryStrategy strategy);

    void resume(DiscoveryStrategy strategy);
}
