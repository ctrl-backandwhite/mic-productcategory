package com.backandwhite.infrastructure.db.postgres.specification;

import com.backandwhite.domain.valueobject.ReviewStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ReviewEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class ReviewSpecification {

    public static Specification<ReviewEntity> approvedByProductId(String productId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("productId"), productId),
                cb.equal(root.get("status"), ReviewStatus.APPROVED));
    }

    public static Specification<ReviewEntity> withAdminFilters(ReviewStatus status, Integer rating) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (rating != null) {
                predicate = cb.and(predicate, cb.equal(root.get("rating"), rating));
            }

            return predicate;
        };
    }
}
