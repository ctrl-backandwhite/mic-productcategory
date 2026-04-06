package com.backandwhite.infrastructure.db.postgres.specification;

import com.backandwhite.domain.valueobject.BrandStatus;
import com.backandwhite.infrastructure.db.postgres.entity.BrandEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class BrandSpecification {

    public static Specification<BrandEntity> withFilters(BrandStatus status, String name) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}
