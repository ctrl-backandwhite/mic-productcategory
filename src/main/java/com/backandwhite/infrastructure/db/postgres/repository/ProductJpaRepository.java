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

    @Modifying
    @Query("UPDATE ProductEntity p SET p.status = com.backandwhite.domain.valueobject.ProductStatus.PUBLISHED WHERE p.status = com.backandwhite.domain.valueobject.ProductStatus.DRAFT")
    int publishAllDrafts();

    /**
     * Random sample across the whole matching set using PostgreSQL's ORDER BY
     * random(). Accepts optional filters via :status (null = any) and :categoryIds
     * (null or empty list = any category).
     */
    @Query(value = """
            SELECT p.id FROM products p
            WHERE (:status IS NULL OR p.status = :status)
              AND (CAST(:categoryIds AS text) IS NULL
                   OR p.category_id IN (:categoryIds))
            ORDER BY random()
            LIMIT :size
            """, nativeQuery = true)
    List<String> findRandomIds(@Param("status") String status, @Param("categoryIds") List<String> categoryIds,
            @Param("size") int size);

    long countByCategoryId(String categoryId);

    /**
     * Returns [productId, locale] pairs for the given product IDs across every
     * locale they have a translation in. Used by the admin to render coverage
     * badges; the main listing query uses a fetch-join that strips translations to
     * the requested locale, so we need this side query to know what's there.
     */
    @Query("SELECT pt.id.productId, pt.id.locale FROM ProductTranslationEntity pt WHERE pt.id.productId IN :ids")
    List<Object[]> findLocalesByProductIds(@Param("ids") List<String> ids);
}
