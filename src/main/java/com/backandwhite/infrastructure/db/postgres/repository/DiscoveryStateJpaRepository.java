package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.DiscoveryStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscoveryStateJpaRepository extends JpaRepository<DiscoveryStateEntity, String> {
}
