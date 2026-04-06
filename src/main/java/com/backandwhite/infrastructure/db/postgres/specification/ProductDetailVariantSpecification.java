package com.backandwhite.infrastructure.db.postgres.specification;

import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductDetailVariantSpecification {

    /**
     * Filtra variantes por estado (DRAFT / PUBLISHED).
     */
    public static Specification<ProductDetailVariantEntity> hasStatus(
            com.backandwhite.domain.valueobject.ProductStatus status) {
        return (root, query, cb) -> {
            if (status == null)
                return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    /**
     * Filtra variantes que pertenezcan exactamente al PID indicado.
     */
    public static Specification<ProductDetailVariantEntity> hasPid(String pid) {
        return (root, query, cb) -> {
            if (pid == null || pid.isBlank()) return cb.conjunction();
            return cb.equal(root.get("pid"), pid);
        };
    }

    /**
     * Busca variantes cuyo VID, PID, nombre de variante, SKU, key o nombre
     * del producto padre contengan el término (case-insensitive).
     */
    public static Specification<ProductDetailVariantEntity> searchByTerm(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + term.trim().toLowerCase() + "%";

            Join<ProductDetailVariantEntity, ProductDetailEntity> pdJoin;
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                pdJoin = (Join<ProductDetailVariantEntity, ProductDetailEntity>) (Object) root
                        .fetch("productDetail", JoinType.LEFT);
            } else {
                pdJoin = root.join("productDetail", JoinType.LEFT);
            }

            Predicate vid = cb.like(cb.lower(root.get("vid")), pattern);
            Predicate pid = cb.like(cb.lower(root.get("pid")), pattern);
            Predicate nameEn = cb.like(cb.lower(root.get("variantNameEn")), pattern);
            Predicate sku = cb.like(cb.lower(root.get("variantSku")), pattern);
            Predicate key = cb.like(cb.lower(root.get("variantKey")), pattern);
            Predicate prodName = cb.like(cb.lower(pdJoin.get("productNameEn")), pattern);

            return cb.or(vid, pid, nameEn, sku, key, prodName);
        };
    }
}
