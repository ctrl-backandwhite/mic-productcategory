package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.BrandEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandJpaRepository extends JpaRepository<BrandEntity, String>, JpaSpecificationExecutor<BrandEntity> {

    Optional<BrandEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.brandId = :brandId")
    long countProductsByBrandId(@Param("brandId") String brandId);
}
