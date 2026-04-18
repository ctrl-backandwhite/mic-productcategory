package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.valueobject.CategoryStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryUseCaseImpl implements CategoryUseCase {

    private static final String ENTITY_NAME = "Category";

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> findCategories(String locale, CategoryStatus status, Boolean active) {
        List<Category> allCategories = categoryRepository.findCategories(locale, status, active);
        return buildTree(allCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findCategoriesPaged(String locale, CategoryStatus status, Boolean active, String name,
            Integer level, int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return categoryRepository.findCategoriesPaged(locale, status, active, name, level, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(String categoryId, String locale) {
        return categoryRepository.findById(categoryId, locale)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, categoryId));
    }

    @Override
    @Transactional
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(String categoryId, Category category) {
        return categoryRepository.update(categoryId, category);
    }

    @Override
    @Transactional
    public void delete(String categoryId) {
        categoryRepository.delete(categoryId);
    }

    @Override
    @Transactional
    public void publishCategory(String categoryId) {
        Category category = categoryRepository.findById(categoryId, null)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound(ENTITY_NAME, categoryId));
        CategoryStatus newStatus = category.getStatus() == CategoryStatus.PUBLISHED
                ? CategoryStatus.DRAFT
                : CategoryStatus.PUBLISHED;
        categoryRepository.updateStatus(categoryId, newStatus);
    }

    @Override
    @Transactional
    public void toggleActive(String categoryId, boolean active) {
        categoryRepository.toggleActive(categoryId, active);
    }

    @Override
    @Transactional
    public void toggleFeatured(String categoryId, boolean featured) {
        categoryRepository.toggleFeatured(categoryId, featured);
    }

    @Override
    @Transactional
    public void deleteAll(List<String> ids) {
        categoryRepository.deleteAll(ids);
    }

    @Override
    @Transactional
    public void bulkUpdateStatus(List<String> ids, String status) {
        CategoryStatus newStatus = CategoryStatus.valueOf(status.toUpperCase());
        categoryRepository.bulkUpdateStatus(ids, newStatus);
    }

    @Override
    @Transactional
    public int publishAllDrafts() {
        return categoryRepository.publishAllDrafts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findFeatured(String locale) {
        return categoryRepository.findFeatured(locale);
    }

    @Override
    @Transactional
    public BulkCategoryResult bulkCreate(List<BulkCategoryRow> rows) {
        BulkCreateCounters counters = new BulkCreateCounters();

        for (BulkCategoryRow row : rows) {
            processBulkRow(row, counters);
        }

        return BulkCategoryResult.builder().created(counters.created).skipped(counters.skipped).totalRows(rows.size())
                .build();
    }

    private void processBulkRow(BulkCategoryRow row, BulkCreateCounters counters) {
        String l1Id = upsertLevel(row.level1Translations(), 1, null, counters);
        if (l1Id == null) {
            return;
        }
        String l2Id = upsertLevel(row.level2Translations(), 2, l1Id, counters);
        if (l2Id == null) {
            return;
        }
        upsertLevel(row.level3Translations(), 3, l2Id, counters);
    }

    /**
     * Upserts a category at the given level and returns its id. Returns
     * {@code null} when the translations are absent so the caller can stop
     * processing deeper levels.
     */
    private String upsertLevel(List<CategoryTranslation> translations, int level, String parentId,
            BulkCreateCounters counters) {
        if (translations == null || translations.isEmpty()) {
            return null;
        }
        String name = translations.get(0).getName();
        String locale = translations.get(0).getLocale();

        Optional<String> existing = categoryRepository.findCategoryIdByNameAndLocaleAndLevelAndParent(name, locale,
                level, parentId);
        if (existing.isPresent()) {
            counters.skipped++;
            return existing.get();
        }
        Category created = Category.builder().level(level).parentId(parentId).translations(translations).build();
        String newId = categoryRepository.saveAndReturnId(created);
        counters.created++;
        return newId;
    }

    private List<Category> buildTree(List<Category> flatCategories) {
        Map<String, Category> categoryMap = flatCategories.stream().collect(Collectors.toMap(Category::getId, c -> c));

        flatCategories.stream().filter(c -> c.getParentId() != null).forEach(child -> {
            Category parent = categoryMap.get(child.getParentId());
            if (parent != null) {
                parent.getSubCategories().add(child);
            }
        });

        return flatCategories.stream().filter(c -> c.getParentId() == null).toList();
    }

    private static final class BulkCreateCounters {
        int created;
        int skipped;
    }
}
