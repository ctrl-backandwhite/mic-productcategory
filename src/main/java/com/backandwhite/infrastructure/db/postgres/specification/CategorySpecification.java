package com.backandwhite.infrastructure.db.postgres.specification;

import com.backandwhite.domain.valureobject.CategoryStatus;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.CategoryTranslationEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class CategorySpecification {

    @SuppressWarnings("unchecked")
    public static Specification<CategoryEntity> withFilters(String locale, CategoryStatus status, Boolean active) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin = (Join<CategoryEntity, CategoryTranslationEntity>) (Object) root
                    .fetch("translations", JoinType.INNER);

            query.distinct(true);
            query.orderBy(
                    cb.asc(root.get("level")),
                    cb.asc(root.get("id")));

            Predicate predicate = cb.equal(translationJoin.get("id").get("locale"), locale);

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (active != null) {
                predicate = cb.and(predicate, cb.equal(root.get("active"), active));
            }

            return predicate;
        };
    }

    @SuppressWarnings("unchecked")
    public static Specification<CategoryEntity> withPagedFilters(String locale, CategoryStatus status, Boolean active,
            String name, Integer level) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                translationJoin = (Join<CategoryEntity, CategoryTranslationEntity>) (Object) root
                        .fetch("translations", JoinType.INNER);
            } else {
                translationJoin = root.join("translations", JoinType.INNER);
            }

            query.distinct(true);

            Predicate predicate = cb.conjunction();

            if (locale != null && !locale.isBlank()) {
                predicate = cb.and(predicate, cb.equal(translationJoin.get("id").get("locale"), locale));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (active != null) {
                predicate = cb.and(predicate, cb.equal(root.get("active"), active));
            }
            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(translationJoin.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (level != null) {
                predicate = cb.and(predicate, cb.equal(root.get("level"), level));
            }

            return predicate;
        };
    }

    /**
     * Finds categories by translation name, locale, level and optional parentId.
     * Replaces the @Query in
     * CategoryJpaRepository#findByTranslationNameAndLocaleAndLevelAndParent.
     */
    @SuppressWarnings("unchecked")
    public static Specification<CategoryEntity> byTranslationNameAndLocaleAndLevelAndParent(
            String name, String locale, Integer level, String parentId) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                translationJoin = (Join<CategoryEntity, CategoryTranslationEntity>) (Object) root
                        .fetch("translations", JoinType.INNER);
            } else {
                translationJoin = root.join("translations", JoinType.INNER);
            }

            query.distinct(true);

            Predicate predicate = cb.equal(translationJoin.get("name"), name);
            predicate = cb.and(predicate, cb.equal(translationJoin.get("id").get("locale"), locale));
            predicate = cb.and(predicate, cb.equal(root.get("level"), level));

            if (parentId == null) {
                predicate = cb.and(predicate, cb.isNull(root.get("parentId")));
            } else {
                predicate = cb.and(predicate, cb.equal(root.get("parentId"), parentId));
            }

            return predicate;
        };
    }

    @SuppressWarnings("unchecked")
    public static Specification<CategoryEntity> withFeatured(String locale) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                translationJoin = (Join<CategoryEntity, CategoryTranslationEntity>) (Object) root
                        .fetch("translations", JoinType.INNER);
            } else {
                translationJoin = root.join("translations", JoinType.INNER);
            }

            query.distinct(true);
            query.orderBy(
                    cb.asc(root.get("level")),
                    cb.asc(root.get("id")));

            Predicate predicate = cb.equal(root.get("featured"), true);
            predicate = cb.and(predicate, cb.equal(root.get("active"), true));

            if (locale != null && !locale.isBlank()) {
                predicate = cb.and(predicate, cb.equal(translationJoin.get("id").get("locale"), locale));
            }

            return predicate;
        };
    }
}
