package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.valueobject.CategoryStatus;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.CategoryInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.CategoryJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.CategorySpecification;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryInfraMapper categoryInfraMapper;

    @Override
    public List<Category> findCategories(String locale, CategoryStatus status, Boolean active) {
        return categoryJpaRepository.findAll(CategorySpecification.withFilters(locale, status, active)).stream()
                .map(categoryInfraMapper::toDomain).toList();
    }

    @Override
    public Page<Category> findCategoriesPaged(String locale, CategoryStatus status, Boolean active, String name,
            Integer level, Pageable pageable) {
        return categoryJpaRepository
                .findAll(CategorySpecification.withPagedFilters(locale, status, active, name, level), pageable)
                .map(categoryInfraMapper::toDomain);
    }

    @Override
    public Optional<Category> findById(String categoryId, String locale) {
        return categoryJpaRepository.findById(categoryId)
                .map(entity -> enrichWithTree(categoryInfraMapper.toDomain(entity), locale));
    }

    @Override
    public Category save(Category category) {
        String newId = UUID.randomUUID().toString();
        category.setId(newId);
        category.setStatus(CategoryStatus.DRAFT);
        category.setActive(true);

        categoryJpaRepository.save(categoryInfraMapper.toEntityWithChildren(category));

        String locale = resolveLocale(category);
        return findById(newId, locale).orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", newId));
    }

    @Override
    public Category update(String categoryId, Category category) {
        CategoryEntity entity = findOrThrow(categoryId);

        entity.setParentId(category.getParentId());
        entity.setLevel(category.getLevel());
        upsertTranslations(entity, category.getTranslations());
        categoryJpaRepository.save(entity);

        String locale = resolveLocale(category);
        return findById(categoryId, locale)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
    }

    @Override
    public void delete(String categoryId) {
        categoryJpaRepository.delete(findOrThrow(categoryId));
    }

    @Override
    public void updateStatus(String categoryId, CategoryStatus status) {
        CategoryEntity entity = findOrThrow(categoryId);
        entity.setStatus(status);
        categoryJpaRepository.save(entity);
    }

    @Override
    public void toggleActive(String categoryId, boolean active) {
        CategoryEntity entity = findOrThrow(categoryId);
        entity.setActive(active);
        categoryJpaRepository.save(entity);
    }

    @Override
    public void toggleFeatured(String categoryId, boolean featured) {
        CategoryEntity entity = findOrThrow(categoryId);
        entity.setFeatured(featured);
        categoryJpaRepository.save(entity);
    }

    @Override
    public void deleteAll(List<String> ids) {
        categoryJpaRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public void bulkUpdateStatus(List<String> ids, CategoryStatus status) {
        categoryJpaRepository.bulkUpdateStatus(ids, status);
    }

    @Override
    public int publishAllDrafts() {
        return categoryJpaRepository.publishAllDrafts();
    }

    @Override
    public List<Category> findFeatured(String locale) {
        Map<String, List<CategoryEntity>> childrenMap = loadAllChildrenMap();
        return categoryJpaRepository.findAll(CategorySpecification.withFeatured(locale)).stream()
                .map(entity -> enrichWithTree(categoryInfraMapper.toDomain(entity), locale, childrenMap)).toList();
    }

    @Override
    public Optional<String> findCategoryIdByNameAndLocaleAndLevelAndParent(String name, String locale, int level,
            String parentId) {
        List<CategoryEntity> results = categoryJpaRepository.findAll(
                CategorySpecification.byTranslationNameAndLocaleAndLevelAndParent(name, locale, level, parentId));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst().getId());
    }

    @Override
    public Optional<String> findCategoryIdById(String categoryId) {
        return categoryJpaRepository.findById(categoryId).map(CategoryEntity::getId);
    }

    @Override
    public String upsertCategory(String id, String parentId, int level, String name, String locale) {
        Optional<CategoryEntity> existingOpt = id != null
                ? categoryJpaRepository.findById(id)
                : categoryJpaRepository.findAll(CategorySpecification.byTranslationNameAndLocaleAndLevelAndParent(name,
                        locale, level, parentId)).stream().findFirst();

        CategoryTranslation ct = CategoryTranslation.builder().locale(locale).name(name).build();

        if (existingOpt.isPresent()) {
            CategoryEntity existing = existingOpt.get();
            existing.setParentId(parentId);
            existing.setLevel(level);
            upsertTranslations(existing, List.of(ct));
            categoryJpaRepository.save(existing);
            return existing.getId();
        }

        String newId = id != null ? id : UUID.randomUUID().toString();
        Category category = Category.builder().id(newId).parentId(parentId).level(level).status(CategoryStatus.DRAFT)
                .active(true).translations(List.of(ct)).build();
        categoryJpaRepository.save(categoryInfraMapper.toEntityWithChildren(category));
        return newId;
    }

    @Override
    public String saveAndReturnId(Category category) {
        String newId = UUID.randomUUID().toString();
        category.setId(newId);
        category.setStatus(CategoryStatus.DRAFT);
        category.setActive(true);
        categoryJpaRepository.save(categoryInfraMapper.toEntityWithChildren(category));
        return newId;
    }

    @Override
    public List<String> findAllLevel3Ids() {
        return categoryJpaRepository.findIdsByLevel(3);
    }

    @Override
    @Transactional
    public void updateLastDiscoveredAt(String categoryId) {
        categoryJpaRepository.updateLastDiscoveredAt(categoryId, Instant.now());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private CategoryEntity findOrThrow(String id) {
        return categoryJpaRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", id));
    }

    /** Loads the entity, builds the children tree and applies locale name. */
    private Category enrichWithTree(Category category, String locale) {
        Map<String, List<CategoryEntity>> childrenMap = loadAllChildrenMap();
        return enrichWithTree(category, locale, childrenMap);
    }

    /** Applies locale name and builds the children tree using a pre-loaded map. */
    private Category enrichWithTree(Category category, String locale, Map<String, List<CategoryEntity>> childrenMap) {
        applyLocaleName(category, locale);
        buildSubCategories(category, childrenMap, locale);
        return category;
    }

    /** Single query that groups all categories by their parentId. */
    private Map<String, List<CategoryEntity>> loadAllChildrenMap() {
        return categoryJpaRepository.findAll().stream().filter(e -> e.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryEntity::getParentId));
    }

    /**
     * Sets the category name from the translation matching the requested locale.
     * No-op if the locale is not found (keeps the existing name).
     */
    private void applyLocaleName(Category category, String locale) {
        if (locale == null || category.getTranslations() == null || category.getTranslations().isEmpty())
            return;
        category.getTranslations().stream().filter(t -> locale.equals(t.getLocale())).findFirst()
                .ifPresent(t -> category.setName(t.getName()));
    }

    /** Recursively builds the subCategories tree for a given category. */
    private void buildSubCategories(Category parent, Map<String, List<CategoryEntity>> childrenMap, String locale) {
        for (CategoryEntity childEntity : childrenMap.getOrDefault(parent.getId(), List.of())) {
            Category child = categoryInfraMapper.toDomain(childEntity);
            applyLocaleName(child, locale);
            buildSubCategories(child, childrenMap, locale);
            parent.getSubCategories().add(child);
        }
    }

    /** Upserts the given translations into the entity (add or update by locale). */
    private void upsertTranslations(CategoryEntity entity, List<CategoryTranslation> translations) {
        if (translations == null)
            return;
        translations.forEach(t -> entity.getTranslations().stream()
                .filter(existing -> existing.getId().getLocale().equals(t.getLocale())).findFirst()
                .ifPresentOrElse(existing -> existing.setName(t.getName()), () -> {
                    CategoryTranslationEntity te = categoryInfraMapper.toTranslationEntity(t, entity.getId());
                    te.setCategory(entity);
                    entity.getTranslations().add(te);
                }));
    }

    /** Returns the locale of the first translation, falling back to "en". */
    private String resolveLocale(Category category) {
        if (category.getTranslations() != null && !category.getTranslations().isEmpty()) {
            return category.getTranslations().getFirst().getLocale();
        }
        return "en";
    }
}
