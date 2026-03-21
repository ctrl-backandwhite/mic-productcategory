package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.valureobject.CategoryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryUseCaseImpl implements CategoryUseCase {

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
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
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
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
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
    @Transactional(readOnly = true)
    public List<Category> findFeatured(String locale) {
        return categoryRepository.findFeatured(locale);
    }

    @Override
    @Transactional
    public BulkCategoryResult bulkCreate(List<BulkCategoryRow> rows) {
        int created = 0;
        int skipped = 0;

        for (BulkCategoryRow row : rows) {
            // ── Level 1 ──────────────────────────────────
            List<CategoryTranslation> l1Translations = row.level1Translations();
            if (l1Translations == null || l1Translations.isEmpty()) {
                continue;
            }

            String l1Key = l1Translations.get(0).getName();
            String l1Locale = l1Translations.get(0).getLocale();
            String l1Id;

            Optional<String> existingL1 = categoryRepository
                    .findCategoryIdByNameAndLocaleAndLevelAndParent(l1Key, l1Locale, 1, null);

            if (existingL1.isPresent()) {
                l1Id = existingL1.get();
                skipped++;
            } else {
                Category l1 = Category.builder()
                        .level(1)
                        .parentId(null)
                        .translations(l1Translations)
                        .build();
                l1Id = categoryRepository.saveAndReturnId(l1);
                created++;
            }

            // ── Level 2 ──────────────────────────────────
            List<CategoryTranslation> l2Translations = row.level2Translations();
            if (l2Translations == null || l2Translations.isEmpty()) {
                continue;
            }

            String l2Key = l2Translations.get(0).getName();
            String l2Locale = l2Translations.get(0).getLocale();
            String l2Id;

            Optional<String> existingL2 = categoryRepository
                    .findCategoryIdByNameAndLocaleAndLevelAndParent(l2Key, l2Locale, 2, l1Id);

            if (existingL2.isPresent()) {
                l2Id = existingL2.get();
                skipped++;
            } else {
                Category l2 = Category.builder()
                        .level(2)
                        .parentId(l1Id)
                        .translations(l2Translations)
                        .build();
                l2Id = categoryRepository.saveAndReturnId(l2);
                created++;
            }

            // ── Level 3 ──────────────────────────────────
            List<CategoryTranslation> l3Translations = row.level3Translations();
            if (l3Translations == null || l3Translations.isEmpty()) {
                continue;
            }

            String l3Key = l3Translations.get(0).getName();
            String l3Locale = l3Translations.get(0).getLocale();

            Optional<String> existingL3 = categoryRepository
                    .findCategoryIdByNameAndLocaleAndLevelAndParent(l3Key, l3Locale, 3, l2Id);

            if (existingL3.isPresent()) {
                skipped++;
            } else {
                Category l3 = Category.builder()
                        .level(3)
                        .parentId(l2Id)
                        .translations(l3Translations)
                        .build();
                categoryRepository.saveAndReturnId(l3);
                created++;
            }
        }

        return BulkCategoryResult.builder()
                .created(created)
                .skipped(skipped)
                .totalRows(rows.size())
                .build();
    }

    private List<Category> buildTree(List<Category> flatCategories) {
        Map<String, Category> categoryMap = flatCategories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        flatCategories.stream()
                .filter(c -> c.getParentId() != null)
                .forEach(child -> {
                    Category parent = categoryMap.get(child.getParentId());
                    if (parent != null) {
                        parent.getSubCategories().add(child);
                    }
                });

        return flatCategories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
    }
}
