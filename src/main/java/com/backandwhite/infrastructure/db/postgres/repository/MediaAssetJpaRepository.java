package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.MediaAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaAssetJpaRepository extends JpaRepository<MediaAssetEntity, String>,
        JpaSpecificationExecutor<MediaAssetEntity> {

    Optional<MediaAssetEntity> findByFilename(String filename);

    boolean existsByFilename(String filename);
}
