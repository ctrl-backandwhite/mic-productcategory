package com.backandwhite.infrastructure.db.postgres.specification;

import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductSpecification {

    @SuppressWarnings("unchecked")
    public static Specification<ProductEntity> byCategoryAndLocale(String categoryId, String locale) {
        return (root, query, cb) -> {
            Join<ProductEntity, ProductTranslationEntity> translationJoin;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                translationJoin = (Join<ProductEntity, ProductTranslationEntity>) (Object) root.fetch("translations",
                        JoinType.INNER);
            } else {
                translationJoin = root.join("translations", JoinType.INNER);
            }

            query.distinct(true);

            Predicate predicate = cb.equal(root.get("categoryId"), categoryId);

            if (locale != null && !locale.isBlank()) {
                predicate = cb.and(predicate, cb.equal(translationJoin.get("id").get("locale"), locale));
            }

            return predicate;
        };
    }

    @SuppressWarnings("unchecked")
    public static Specification<ProductEntity> byLocale(String locale) {
        return byLocaleAndCategory(locale, null);
    }

    @SuppressWarnings("unchecked")
    public static Specification<ProductEntity> byLocaleAndCategory(String locale, String categoryId) {
        return byLocaleAndCategoryIds(locale, categoryId != null ? java.util.List.of(categoryId) : null);
    }

    /**
     * Filters products by locale and a collection of category IDs (supports
     * recursive category trees).
     */
    @SuppressWarnings("unchecked")
    public static Specification<ProductEntity> byLocaleAndCategoryIds(String locale, Collection<String> categoryIds) {
        return byLocaleAndCategoryIds(locale, categoryIds, null);
    }

    /**
     * Filters products by locale, category IDs and optionally by status.
     */
    @SuppressWarnings("unchecked")
    public static Specification<ProductEntity> byLocaleAndCategoryIds(String locale, Collection<String> categoryIds,
            ProductStatus status) {
        return byLocaleAndCategoryIds(locale, categoryIds, status, null);
    }

    /**
     * Filters products by locale, category IDs, status and optionally by name
     * (ILIKE).
     */
    @SuppressWarnings("unchecked")
    public static Specification<ProductEntity> byLocaleAndCategoryIds(String locale, Collection<String> categoryIds,
            ProductStatus status, String name) {
        return (root, query, cb) -> {
            Join<ProductEntity, ProductTranslationEntity> translationJoin;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                translationJoin = (Join<ProductEntity, ProductTranslationEntity>) (Object) root.fetch("translations",
                        JoinType.INNER);
            } else {
                translationJoin = root.join("translations", JoinType.INNER);
            }

            query.distinct(true);

            Predicate predicate = cb.conjunction();

            if (locale != null && !locale.isBlank()) {
                predicate = cb.and(predicate, cb.equal(translationJoin.get("id").get("locale"), locale));
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicate = cb.and(predicate, root.get("categoryId").in(categoryIds));
            }

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(translationJoin.get("name")), "%" + name.toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}
