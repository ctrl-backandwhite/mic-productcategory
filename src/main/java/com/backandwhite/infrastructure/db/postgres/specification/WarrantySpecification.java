package com.backandwhite.infrastructure.db.postgres.specification;

import com.backandwhite.domain.valueobject.WarrantyType;
import com.backandwhite.infrastructure.db.postgres.entity.WarrantyEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class WarrantySpecification {

    public static Specification<WarrantyEntity> withFilters(Boolean active, WarrantyType type) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (active != null) {
                predicate = cb.and(predicate, cb.equal(root.get("active"), active));
            }
            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }

            return predicate;
        };
    }
}
