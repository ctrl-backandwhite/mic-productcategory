package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository
        extends
            JpaRepository<ProductEntity, String>,
            JpaSpecificationExecutor<ProductEntity> {

    @Query("SELECT p.id FROM ProductEntity p ORDER BY p.createdAt")
    Page<String> findAllIds(Pageable pageable);

    @Query("SELECT p.id FROM ProductEntity p WHERE p.categoryId IN :categoryIds ORDER BY p.createdAt")
    Page<String> findIdsByCategoryIds(@Param("categoryIds") List<String> categoryIds, Pageable pageable);

    List<ProductEntity> findAllByIdIn(List<String> ids);

    @Modifying
    @Query("UPDATE ProductEntity p SET p.status = :status WHERE p.id IN :ids")
    int bulkUpdateStatus(@Param("ids") List<String> ids, @Param("status") ProductStatus status);
}
