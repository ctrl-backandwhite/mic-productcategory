package com.backandwhite.infrastructure.db.postgres.specification;

import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.and;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.equalIfNotNull;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.joinTranslations;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.likeIgnoreCase;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.localeEqual;

import com.backandwhite.domain.valueobject.CategoryStatus;
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

    private static final String TRANSLATIONS = "translations";
    private static final String LEVEL = "level";
    private static final String LOCALE_PATH = "locale";
    private static final String ACTIVE = "active";

    @SuppressWarnings("unchecked")
    public static Specification<CategoryEntity> withFilters(String locale, CategoryStatus status, Boolean active) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin = (Join<CategoryEntity, CategoryTranslationEntity>) (Object) root
                    .fetch(TRANSLATIONS, JoinType.INNER);

            query.distinct(true);
            query.orderBy(cb.asc(root.get(LEVEL)), cb.asc(root.get("id")));

            Predicate predicate = cb.equal(translationJoin.get("id").get(LOCALE_PATH), locale);
            predicate = and(cb, predicate, equalIfNotNull(cb, root.get("status"), status));
            predicate = and(cb, predicate, equalIfNotNull(cb, root.get(ACTIVE), active));

            return predicate;
        };
    }

    public static Specification<CategoryEntity> withPagedFilters(String locale, CategoryStatus status, Boolean active,
            String name, Integer level) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin = joinTranslations(root, query,
                    TRANSLATIONS);

            query.distinct(true);

            Predicate predicate = cb.conjunction();
            predicate = and(cb, predicate, localeEqual(cb, translationJoin, locale));
            predicate = and(cb, predicate, equalIfNotNull(cb, root.get("status"), status));
            predicate = and(cb, predicate, equalIfNotNull(cb, root.get(ACTIVE), active));
            predicate = and(cb, predicate, likeIgnoreCase(cb, translationJoin.get("name"), name));
            predicate = and(cb, predicate, equalIfNotNull(cb, root.get(LEVEL), level));

            return predicate;
        };
    }

    /**
     * Finds categories by translation name, locale, level and optional parentId.
     * Replaces the @Query in
     * CategoryJpaRepository#findByTranslationNameAndLocaleAndLevelAndParent.
     */
    public static Specification<CategoryEntity> byTranslationNameAndLocaleAndLevelAndParent(String name, String locale,
            Integer level, String parentId) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin = joinTranslations(root, query,
                    TRANSLATIONS);

            query.distinct(true);

            Predicate predicate = cb.equal(translationJoin.get("name"), name);
            predicate = cb.and(predicate, cb.equal(translationJoin.get("id").get(LOCALE_PATH), locale));
            predicate = cb.and(predicate, cb.equal(root.get(LEVEL), level));

            if (parentId == null) {
                predicate = cb.and(predicate, cb.isNull(root.get("parentId")));
            } else {
                predicate = cb.and(predicate, cb.equal(root.get("parentId"), parentId));
            }

            return predicate;
        };
    }

    public static Specification<CategoryEntity> withFeatured(String locale) {
        return (root, query, cb) -> {
            Join<CategoryEntity, CategoryTranslationEntity> translationJoin = joinTranslations(root, query,
                    TRANSLATIONS);

            query.distinct(true);
            query.orderBy(cb.asc(root.get(LEVEL)), cb.asc(root.get("id")));

            Predicate predicate = cb.equal(root.get("featured"), true);
            predicate = cb.and(predicate, cb.equal(root.get(ACTIVE), true));
            predicate = and(cb, predicate, localeEqual(cb, translationJoin, locale));

            return predicate;
        };
    }
}
