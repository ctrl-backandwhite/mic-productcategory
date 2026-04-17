package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Use case for getting product detail. If the product doesn't exist in the
 * local DB, fetches it from CJ Dropshipping, persists it and then returns it
 * from the DB.
 */
public interface ProductDetailUseCase {

    /**
     * Gets the detail of a product by its pid (CJ product ID).
     * <ol>
     * <li>Searches for the product in the local DB by pid.</li>
     * <li>If NOT found, calls the CJ API, persists it and returns it from the
     * DB.</li>
     * <li>If it already EXISTS, returns it directly from the DB.</li>
     * </ol>
     *
     * @param pid
     *            CJ product ID
     * @param locale
     *            Language code for filtering translations
     * @return Product detail with translations, variants and inventories
     */
    ProductDetail getOrFetchFromCj(String pid, String locale);

    // ── Variant CRUD ─────────────────────────────────────────────────────────

    /**
     * Lists all variants paginated, with optional search and filters.
     */
    Page<ProductDetailVariant> findAllVariantsPaged(int page, int size, String locale, String search, String status,
            String pid, String sortBy, boolean ascending);

    /**
     * Lists all variants of a product.
     */
    List<ProductDetailVariant> findVariantsByPid(String pid, String locale);

    /**
     * Gets a variant by its vid.
     */
    ProductDetailVariant findVariantByVid(String vid, String locale);

    /**
     * Manually creates a new variant.
     */
    ProductDetailVariant createVariant(ProductDetailVariant variant);

    /**
     * Updates an existing variant.
     */
    ProductDetailVariant updateVariant(String vid, ProductDetailVariant variant);

    /**
     * Deletes a variant by its vid.
     */
    void deleteVariant(String vid);

    /**
     * Publishes or unpublishes a variant (toggle DRAFT/PUBLISHED).
     */
    void publishVariant(String vid);

    /**
     * Deletes multiple variants by their vids.
     */
    void deleteVariants(List<String> vids);

    /**
     * Changes the status of multiple variants to DRAFT or PUBLISHED.
     */
    void bulkUpdateVariantStatus(List<String> vids, String status);

    /**
     * Bulk variant upload. Creates each variant individually, accumulating errors
     * per row without aborting the entire batch.
     */
    BulkImportResult bulkCreateVariants(List<ProductDetailVariant> variants);
}
