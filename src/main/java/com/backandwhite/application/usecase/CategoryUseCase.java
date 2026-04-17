package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.valueobject.CategoryStatus;
import java.util.List;
import org.springframework.data.domain.Page;

public interface CategoryUseCase {

    List<Category> findCategories(String locale, CategoryStatus status, Boolean active);

    Page<Category> findCategoriesPaged(String locale, CategoryStatus status, Boolean active, String name, Integer level,
            int page, int size, String sortBy, boolean ascending);

    Category findById(String categoryId, String locale);

    Category create(Category category);

    Category update(String categoryId, Category category);

    void delete(String categoryId);

    void publishCategory(String categoryId);

    void toggleActive(String categoryId, boolean active);

    void toggleFeatured(String categoryId, boolean featured);

    List<Category> findFeatured(String locale);

    void deleteAll(List<String> ids);

    void bulkUpdateStatus(List<String> ids, String status);

    int publishAllDrafts();

    /**
     * Creates categories in bulk from rows with up to 3 hierarchy levels. Existing
     * categories (matched by name+locale+level+parent) are skipped.
     *
     * @param rows
     *            each row contains translations for level 1, optionally 2 and 3
     * @return result with created, skipped, and totalRows counts
     */
    BulkCategoryResult bulkCreate(List<BulkCategoryRow> rows);

    /** Represents a single row in the bulk creation request. */
    record BulkCategoryRow(List<CategoryTranslation> level1Translations, List<CategoryTranslation> level2Translations,
            List<CategoryTranslation> level3Translations) {
    }
}
