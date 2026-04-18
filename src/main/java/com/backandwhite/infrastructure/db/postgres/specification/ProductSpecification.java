package com.backandwhite.infrastructure.db.postgres.specification;

import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.and;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.equalIfNotNull;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.joinTranslations;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.likeIgnoreCase;
import static com.backandwhite.infrastructure.db.postgres.specification.SpecificationHelpers.localeEqual;

import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductSpecification {

    private static final String TRANSLATIONS = "translations";

    public static Specification<ProductEntity> byCategoryAndLocale(String categoryId, String locale) {
        return (root, query, cb) -> {
            Join<ProductEntity, ProductTranslationEntity> translationJoin = joinTranslations(root, query, TRANSLATIONS);

            query.distinct(true);

            Predicate predicate = cb.equal(root.get("categoryId"), categoryId);
            predicate = and(cb, predicate, localeEqual(cb, translationJoin, locale));

            return predicate;
        };
    }

    public static Specification<ProductEntity> byLocale(String locale) {
        return byLocaleAndCategory(locale, null);
    }

    public static Specification<ProductEntity> byLocaleAndCategory(String locale, String categoryId) {
        return byLocaleAndCategoryIds(locale, categoryId != null ? java.util.List.of(categoryId) : null);
    }

    /**
     * Filters products by locale and a collection of category IDs (supports
     * recursive category trees).
     */
    public static Specification<ProductEntity> byLocaleAndCategoryIds(String locale, Collection<String> categoryIds) {
        return byLocaleAndCategoryIds(locale, categoryIds, null);
    }

    /**
     * Filters products by locale, category IDs and optionally by status.
     */
    public static Specification<ProductEntity> byLocaleAndCategoryIds(String locale, Collection<String> categoryIds,
            ProductStatus status) {
        return byLocaleAndCategoryIds(locale, categoryIds, status, null);
    }

    /**
     * Filters products by locale, category IDs, status and optionally by name
     * (ILIKE).
     */
    public static Specification<ProductEntity> byLocaleAndCategoryIds(String locale, Collection<String> categoryIds,
            ProductStatus status, String name) {
        return (root, query, cb) -> {
            Join<ProductEntity, ProductTranslationEntity> translationJoin = joinTranslations(root, query, TRANSLATIONS);

            query.distinct(true);

            Predicate predicate = cb.conjunction();
            predicate = and(cb, predicate, localeEqual(cb, translationJoin, locale));

            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicate = cb.and(predicate, root.get("categoryId").in(categoryIds));
            }

            predicate = and(cb, predicate, equalIfNotNull(cb, root.get("status"), status));
            predicate = and(cb, predicate, likeIgnoreCase(cb, translationJoin.get("name"), name));

            return predicate;
        };
    }
}
