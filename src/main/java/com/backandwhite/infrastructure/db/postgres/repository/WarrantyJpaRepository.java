package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.WarrantyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarrantyJpaRepository extends JpaRepository<WarrantyEntity, String>,
        JpaSpecificationExecutor<WarrantyEntity> {

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.warrantyId = :warrantyId")
    long countProductsByWarrantyId(@Param("warrantyId") String warrantyId);
}
