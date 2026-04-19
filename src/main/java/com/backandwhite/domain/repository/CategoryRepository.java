package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.valueobject.CategoryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryRepository {

    List<Category> findCategories(String locale, CategoryStatus status, Boolean active);

    Page<Category> findCategoriesPaged(String locale, CategoryStatus status, Boolean active, String name, Integer level,
            Pageable pageable);

    Optional<Category> findById(String categoryId, String locale);

    Category save(Category category);

    Category update(String categoryId, Category category);

    void delete(String categoryId);

    void updateStatus(String categoryId, CategoryStatus status);

    void toggleActive(String categoryId, boolean active);

    void toggleFeatured(String categoryId, boolean featured);

    List<Category> findFeatured(String locale);

    void deleteAll(List<String> ids);

    void bulkUpdateStatus(List<String> ids, CategoryStatus status);

    Optional<String> findCategoryIdByNameAndLocaleAndLevelAndParent(String name, String locale, int level,
            String parentId);

    Optional<String> findCategoryIdById(String categoryId);

    String upsertCategory(String id, String parentId, int level, String name, String locale);

    /**
     * Saves a category and returns only its generated ID (lightweight, no tree
     * building).
     */
    String saveAndReturnId(Category category);

    List<String> findAllLevel3Ids();

    /**
     * Returns the transitive descendant ids of the given category (children,
     * grandchildren, …). Used to expand a parent category into the full set of leaf
     * categories that actually hold products.
     */
    List<String> findDescendantIds(String categoryId);

    int publishAllDrafts();

    void updateLastDiscoveredAt(String categoryId);
}
