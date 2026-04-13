package com.backandwhite.application.strategy;

import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;

public interface DiscoveryStrategyExecutor {

    DiscoveryResult execute(DiscoveryState state);

    DiscoveryStrategy getStrategy();

    boolean supportsResume();
}
