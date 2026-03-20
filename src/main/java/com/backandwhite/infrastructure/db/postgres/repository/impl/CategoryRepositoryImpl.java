package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.domain.exception.Message;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.valureobject.CategoryStatus;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.CategoryInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.CategoryJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.CategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryInfraMapper categoryInfraMapper;

    @Override
    public List<Category> findCategories(String locale, CategoryStatus status, Boolean active) {
        return categoryJpaRepository.findAll(CategorySpecification.withFilters(locale, status, active)).stream()
                .map(categoryInfraMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Category> findCategoriesPaged(String locale, CategoryStatus status, Boolean active, String name,
            Integer level, Pageable pageable) {
        return categoryJpaRepository
                .findAll(CategorySpecification.withPagedFilters(locale, status, active, name, level),
                        pageable)
                .map(categoryInfraMapper::toDomain);
    }

    @Override
    public Optional<Category> findById(String categoryId, String locale) {
        return categoryJpaRepository.findById(categoryId)
                .map(entity -> {
                    Category category = categoryInfraMapper.toDomain(entity);
                    applyLocaleName(category, locale);
                    // Build children tree recursively
                    List<CategoryEntity> allEntities = categoryJpaRepository.findAll();
                    Map<String, List<CategoryEntity>> childrenMap = allEntities.stream()
                            .filter(e -> e.getParentId() != null)
                            .collect(Collectors.groupingBy(CategoryEntity::getParentId));
                    buildSubCategories(category, childrenMap, locale);
                    return category;
                });
    }

    /**
     * Sets the category name from the translation matching the requested locale.
     * Falls back to the first available translation if the locale is not found.
     */
    private void applyLocaleName(Category category, String locale) {
        if (locale == null || category.getTranslations() == null || category.getTranslations().isEmpty()) {
            return;
        }
        category.getTranslations().stream()
                .filter(t -> locale.equals(t.getLocale()))
                .findFirst()
                .ifPresent(t -> category.setName(t.getName()));
    }

    /**
     * Recursively builds the subCategories tree for a given category.
     */
    private void buildSubCategories(Category parent, Map<String, List<CategoryEntity>> childrenMap, String locale) {
        List<CategoryEntity> childEntities = childrenMap.getOrDefault(parent.getId(), List.of());
        for (CategoryEntity childEntity : childEntities) {
            Category child = categoryInfraMapper.toDomain(childEntity);
            applyLocaleName(child, locale);
            buildSubCategories(child, childrenMap, locale);
            parent.getSubCategories().add(child);
        }
    }

    @Override
    public Category save(Category category) {
        String newId = UUID.randomUUID().toString();
        category.setId(newId);
        category.setStatus(CategoryStatus.DRAFT);
        category.setActive(true);

        CategoryEntity entity = categoryInfraMapper.toEntityWithChildren(category);
        categoryJpaRepository.save(entity);

        String locale = resolveLocale(category);
        return findById(newId, locale)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", newId));
    }

    @Override
    public Category update(String categoryId, Category category) {
        CategoryEntity entity = categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));

        entity.setParentId(category.getParentId());
        entity.setLevel(category.getLevel());

        if (category.getTranslations() != null) {
            category.getTranslations().forEach(t -> entity.getTranslations().stream()
                    .filter(existing -> existing.getId().getLocale().equals(t.getLocale()))
                    .findFirst()
                    .ifPresentOrElse(
                            existing -> existing.setName(t.getName()),
                            () -> {
                                CategoryTranslationEntity te = categoryInfraMapper.toTranslationEntity(t, categoryId);
                                te.setCategory(entity);
                                entity.getTranslations().add(te);
                            }));
        }

        categoryJpaRepository.save(entity);

        String locale = resolveLocale(category);
        return findById(categoryId, locale)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
    }

    @Override
    public void delete(String categoryId) {
        CategoryEntity entity = categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
        categoryJpaRepository.delete(entity);
    }

    @Override
    public void updateStatus(String categoryId, CategoryStatus status) {
        CategoryEntity entity = categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
        entity.setStatus(status);
        categoryJpaRepository.save(entity);
    }

    @Override
    public void toggleActive(String categoryId, boolean active) {
        CategoryEntity entity = categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
        entity.setActive(active);
        categoryJpaRepository.save(entity);
    }

    @Override
    public void toggleFeatured(String categoryId, boolean featured) {
        CategoryEntity entity = categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("Category", categoryId));
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
    public List<Category> findFeatured(String locale) {
        // Load all entities once to build the children map
        List<CategoryEntity> allEntities = categoryJpaRepository.findAll();
        Map<String, List<CategoryEntity>> childrenMap = allEntities.stream()
                .filter(e -> e.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryEntity::getParentId));

        return categoryJpaRepository.findAll(CategorySpecification.withFeatured(locale)).stream()
                .map(entity -> {
                    Category category = categoryInfraMapper.toDomain(entity);
                    applyLocaleName(category, locale);
                    buildSubCategories(category, childrenMap, locale);
                    return category;
                })
                .toList();
    }

    @Override
    public Optional<String> findCategoryIdByNameAndLocaleAndLevelAndParent(String name, String locale, int level,
            String parentId) {
        List<CategoryEntity> results = categoryJpaRepository
                .findAll(CategorySpecification.byTranslationNameAndLocaleAndLevelAndParent(name, locale, level,
                        parentId));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst().getId());
    }

    @Override
    public Optional<String> findCategoryIdById(String categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .map(CategoryEntity::getId);
    }

    @Override
    public String upsertCategory(String id, String parentId, int level, String name, String locale) {
        Optional<CategoryEntity> existingOpt;
        if (id != null) {
            existingOpt = categoryJpaRepository.findById(id);
        } else {
            // Look up by name+locale+level+parent to avoid creating duplicates
            List<CategoryEntity> found = categoryJpaRepository
                    .findAll(CategorySpecification.byTranslationNameAndLocaleAndLevelAndParent(name, locale, level,
                            parentId));
            existingOpt = found.isEmpty() ? Optional.empty() : Optional.of(found.getFirst());
        }

        CategoryTranslation ct = CategoryTranslation.builder().locale(locale).name(name).build();

        if (existingOpt.isPresent()) {
            CategoryEntity existing = existingOpt.get();
            existing.setParentId(parentId);
            existing.setLevel(level);

            existing.getTranslations().stream()
                    .filter(t -> locale.equals(t.getId().getLocale()))
                    .findFirst()
                    .ifPresentOrElse(
                            t -> t.setName(name),
                            () -> {
                                CategoryTranslationEntity te = categoryInfraMapper.toTranslationEntity(ct,
                                        existing.getId());
                                te.setCategory(existing);
                                existing.getTranslations().add(te);
                            });

            categoryJpaRepository.save(existing);
            return existing.getId();
        } else {
            String newId = (id != null) ? id : UUID.randomUUID().toString();

            Category category = Category.builder()
                    .id(newId)
                    .parentId(parentId)
                    .level(level)
                    .status(CategoryStatus.DRAFT)
                    .active(true)
                    .translations(List.of(ct))
                    .build();

            CategoryEntity entity = categoryInfraMapper.toEntityWithChildren(category);
            categoryJpaRepository.save(entity);
            return newId;
        }
    }

    @Override
    public String saveAndReturnId(Category category) {
        String newId = UUID.randomUUID().toString();
        category.setId(newId);
        category.setStatus(CategoryStatus.DRAFT);
        category.setActive(true);

        CategoryEntity entity = categoryInfraMapper.toEntityWithChildren(category);
        categoryJpaRepository.save(entity);
        return newId;
    }

    /**
     * Resolves the locale from the category's translations (uses the first one).
     * Falls back to "es" if no translations are available.
     */
    private String resolveLocale(Category category) {
        if (category.getTranslations() != null && !category.getTranslations().isEmpty()) {
            return category.getTranslations().getFirst().getLocale();
        }
        return "es";
    }
}
