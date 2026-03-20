package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, String>,
        JpaSpecificationExecutor<CategoryEntity> {

    /**
     * Recursively finds all descendant category IDs for a given parent category,
     * including the parent itself. Uses PostgreSQL WITH RECURSIVE.
     */
    @Query(nativeQuery = true, value = """
            WITH RECURSIVE cat_tree AS (
                SELECT id FROM categories WHERE id = :parentId
                UNION ALL
                SELECT c.id FROM categories c
                INNER JOIN cat_tree ct ON c.parent_id = ct.id
            )
            SELECT id FROM cat_tree
            """)
    List<String> findDescendantIds(@Param("parentId") String parentId);

    @Modifying
    @Query("UPDATE CategoryEntity c SET c.status = :status WHERE c.id IN :ids")
    int bulkUpdateStatus(@Param("ids") List<String> ids,
            @Param("status") com.backandwhite.domain.valureobject.CategoryStatus status);
}