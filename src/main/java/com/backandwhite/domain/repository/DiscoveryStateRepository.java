package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import java.util.List;
import java.util.Optional;

public interface DiscoveryStateRepository {

    Optional<DiscoveryState> findByStrategy(DiscoveryStrategy strategy);

    List<DiscoveryState> findAll();

    DiscoveryState save(DiscoveryState state);
}
