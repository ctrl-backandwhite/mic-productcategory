package com.backandwhite.infrastructure.db.postgres.specification;

import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Shared helpers used by JPA
 * {@link org.springframework.data.jpa.domain.Specification} implementations to
 * build predicates in a consistent and null-safe way.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpecificationHelpers {

    /**
     * Join translations using a {@code fetch} for regular queries and a plain
     * {@code join} for count queries. This is the pattern shared by
     * {@code CategorySpecification} and {@code ProductSpecification}.
     */
    @SuppressWarnings("unchecked")
    public static <E, T> Join<E, T> joinTranslations(Root<E> root, CriteriaQuery<?> query, String attribute) {
        if (isCountQuery(query)) {
            return root.join(attribute, JoinType.INNER);
        }
        return (Join<E, T>) (Object) root.fetch(attribute, JoinType.INNER);
    }

    /**
     * Whether the current query is a COUNT query (used to decide between
     * {@code fetch} and {@code join} so we don't break Hibernate's
     * {@code count(distinct)} expansion).
     */
    public static boolean isCountQuery(CommonAbstractCriteria query) {
        if (!(query instanceof CriteriaQuery<?> cq)) {
            return false;
        }
        Class<?> resultType = cq.getResultType();
        return resultType == Long.class || resultType == long.class;
    }

    /**
     * Case-insensitive {@code LIKE} predicate against a text column:
     * {@code lower(path) LIKE %value%}. Returns {@code null} if the value is blank
     * so callers can skip combining it.
     */
    public static Predicate likeIgnoreCase(CriteriaBuilder cb, Path<String> path, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");
    }

    /**
     * Equality predicate that is only applied when the value is non-null. Returns
     * {@code null} when the value is null so callers can skip combining it.
     */
    public static <T> Predicate equalIfNotNull(CriteriaBuilder cb, Path<T> path, T value) {
        return value == null ? null : cb.equal(path, value);
    }

    /**
     * Locale equality predicate against a translation id:
     * {@code translationJoin.id.locale = locale}. Returns {@code null} when locale
     * is blank.
     */
    public static Predicate localeEqual(CriteriaBuilder cb, From<?, ?> translationJoin, String locale) {
        if (locale == null || locale.isBlank()) {
            return null;
        }
        return cb.equal(translationJoin.get("id").get("locale"), locale);
    }

    /**
     * Combine a base predicate with an optional one using {@code AND}. Null extras
     * are ignored.
     */
    public static Predicate and(CriteriaBuilder cb, Predicate base, Predicate extra) {
        if (extra == null) {
            return base;
        }
        return cb.and(base, extra);
    }
}
