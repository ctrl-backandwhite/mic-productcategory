package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.model.DiscoveredPid;
import com.backandwhite.domain.repository.DiscoveredPidRepository;
import com.backandwhite.domain.valueobject.DiscoveryStatus;
import com.backandwhite.domain.valueobject.DiscoveryStrategy;
import com.backandwhite.infrastructure.db.postgres.entity.DiscoveredPidEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.DiscoveryInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.DiscoveredPidJpaRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscoveredPidRepositoryImpl implements DiscoveredPidRepository {

    private final DiscoveredPidJpaRepository jpaRepository;
    private final DiscoveryInfraMapper mapper;

    @Override
    public boolean existsByPid(String pid) {
        return jpaRepository.existsByPid(pid);
    }

    @Override
    public DiscoveredPid save(DiscoveredPid discoveredPid) {
        DiscoveredPidEntity entity = mapper.toEntity(discoveredPid);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<DiscoveredPid> saveAll(List<DiscoveredPid> pids) {
        List<DiscoveredPidEntity> entities = pids.stream().map(mapper::toEntity).toList();
        return jpaRepository.saveAll(entities).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<DiscoveredPid> findByStatus(DiscoveryStatus status, int limit) {
        return jpaRepository.findByStatusOrderByDiscoveredAtAsc(status.name(), PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public Page<DiscoveredPid> findByStatusPaged(DiscoveryStatus status, Pageable pageable) {
        return jpaRepository.findByStatus(status.name(), pageable).map(mapper::toDomain);
    }

    @Override
    public Page<DiscoveredPid> findAll(DiscoveryStatus status, DiscoveryStrategy strategy, Pageable pageable) {
        if (status != null && strategy != null) {
            return jpaRepository.findByStatusAndStrategy(status.name(), strategy.name(), pageable)
                    .map(mapper::toDomain);
        } else if (status != null) {
            return jpaRepository.findByStatus(status.name(), pageable).map(mapper::toDomain);
        } else {
            return jpaRepository.findAll(pageable).map(mapper::toDomain);
        }
    }

    @Override
    public long countByStatus(DiscoveryStatus status) {
        return jpaRepository.countByStatus(status.name());
    }

    @Override
    public long countByStrategy(DiscoveryStrategy strategy) {
        return jpaRepository.countByStrategy(strategy.name());
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public void updateStatus(String id, DiscoveryStatus status) {
        jpaRepository.updateStatus(id, status.name(), Instant.now());
    }

    @Override
    public void markSynced(String id) {
        jpaRepository.markSynced(id, Instant.now());
    }

    @Override
    public void markFailed(String id, String error) {
        jpaRepository.markFailed(id, error, Instant.now());
    }

    @Override
    public int bulkUpdateStatus(DiscoveryStatus fromStatus, DiscoveryStatus toStatus, int limit) {
        List<DiscoveredPidEntity> entities = jpaRepository.findByStatusOrderByDiscoveredAtAsc(fromStatus.name(),
                PageRequest.of(0, limit));
        Instant now = Instant.now();
        for (DiscoveredPidEntity entity : entities) {
            entity.setStatus(toStatus.name());
            entity.setUpdatedAt(now);
        }
        jpaRepository.saveAll(entities);
        return entities.size();
    }
}
