package com.backandwhite.infrastructure.db.postgres.specification;

import com.backandwhite.domain.valureobject.MediaCategory;
import com.backandwhite.infrastructure.db.postgres.entity.MediaAssetEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class MediaAssetSpecification {

    public static Specification<MediaAssetEntity> withFilters(MediaCategory category, String mimeType, String tag) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (category != null) {
                predicate = cb.and(predicate, cb.equal(root.get("category"), category));
            }
            if (mimeType != null && !mimeType.isBlank()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("mimeType")), "%" + mimeType.toLowerCase() + "%"));
            }
            if (tag != null && !tag.isBlank()) {
                predicate = cb.and(predicate,
                        cb.isTrue(cb.function("jsonb_exists", Boolean.class, root.get("tags"), cb.literal(tag))));
            }

            return predicate;
        };
    }
}
