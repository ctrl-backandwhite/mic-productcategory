package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductDetailVariantJpaRepository extends JpaRepository<ProductDetailVariantEntity, String>,
        JpaSpecificationExecutor<ProductDetailVariantEntity> {

    List<ProductDetailVariantEntity> findByPid(String pid);

    Page<ProductDetailVariantEntity> findAll(Pageable pageable);

    @Modifying
    @Query("UPDATE ProductDetailVariantEntity v SET v.status = :status WHERE v.vid IN :vids")
    int bulkUpdateStatus(@Param("vids") List<String> vids,
            @Param("status") com.backandwhite.domain.valueobject.ProductStatus status);
}
