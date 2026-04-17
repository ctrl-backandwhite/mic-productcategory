package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeJpaRepository
        extends
            JpaRepository<AttributeEntity, String>,
            JpaSpecificationExecutor<AttributeEntity> {

    boolean existsBySlug(String slug);
}
