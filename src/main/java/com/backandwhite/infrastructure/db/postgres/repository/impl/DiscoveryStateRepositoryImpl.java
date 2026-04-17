package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.DiscoveryState;
import com.backandwhite.domain.repository.DiscoveryStateRepository;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.db.postgres.mapper.DiscoveryInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.DiscoveryStateJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscoveryStateRepositoryImpl implements DiscoveryStateRepository {

    private final DiscoveryStateJpaRepository jpaRepository;
    private final DiscoveryInfraMapper mapper;

    @Override
    public Optional<DiscoveryState> findByStrategy(DiscoveryStrategy strategy) {
        return jpaRepository.findById(strategy.name()).map(mapper::stateToDomain);
    }

    @Override
    public List<DiscoveryState> findAll() {
        return jpaRepository.findAll().stream().map(mapper::stateToDomain).toList();
    }

    @Override
    public DiscoveryState save(DiscoveryState state) {
        var entity = mapper.stateToEntity(state);
        return mapper.stateToDomain(jpaRepository.save(entity));
    }
}
