package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.valueobject.ProductStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductDetailRepository {

    Optional<ProductDetail> findByPid(String pid);

    boolean existsByPid(String pid);

    boolean existsVariantByVid(String vid);

    ProductDetail save(ProductDetail productDetail);

    long countAll();

    List<String> findPidsNeedingInventorySync(int limit);

    List<String> findPidsNeedingProductSync(int limit);

    List<String> findPidsNeedingReviewsSync(int limit);

    void markInventorySynced(String pid);

    void markProductSynced(String pid);

    void markReviewsSynced(String pid);

    // ── Variant CRUD ─────────────────────────────────────────────────────────

    Page<ProductDetailVariant> findAllVariantsPaged(Pageable pageable);

    Page<ProductDetailVariant> searchVariantsPaged(String search, Pageable pageable);

    Page<ProductDetailVariant> findVariantsFiltered(String locale, String search, ProductStatus status, String pid,
            Pageable pageable);

    List<ProductDetailVariant> findVariantsByPid(String pid, String locale);

    Optional<ProductDetailVariant> findVariantByVid(String vid, String locale);

    ProductDetailVariant saveVariant(ProductDetailVariant variant);

    void updateVariantStatus(String vid, ProductStatus status);

    void deleteVariant(String vid);

    void deleteVariants(List<String> vids);

    void bulkUpdateVariantStatus(List<String> vids, ProductStatus status);
}
