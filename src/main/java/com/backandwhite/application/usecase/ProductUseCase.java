package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductUseCase {

    List<Product> findByCategoryId(String categoryId, String locale, String status);

    @SuppressWarnings("java:S107")
    Page<Product> findAllPaged(String locale, String categoryId, String status, String name, int page, int size,
            String sortBy, boolean ascending);

    /**
     * Admin-flavored variant. When {@code includeDrafts} is true, the query
     * bypasses the Elasticsearch browse index (which only holds PUBLISHED
     * documents) and reads straight from Postgres, so the admin panel can see
     * products in any status including DRAFT.
     */
    @SuppressWarnings("java:S107")
    Page<Product> findAllPaged(String locale, String categoryId, String status, String name, int page, int size,
            String sortBy, boolean ascending, boolean includeDrafts);

    /**
     * Moves every DRAFT product to PUBLISHED. Returns the number of affected rows.
     * Indexing to ES happens downstream via the catalog event pipeline.
     */
    int publishAllDrafts();

    Product findById(String productId, String locale);

    Product create(Product product);

    Product update(String productId, Product product);

    void publishProduct(String productId);

    /**
     * Fine-grained linking: associates a brand (or null to detach) with the given
     * product without requiring the full ProductDtoIn payload.
     */
    void linkBrand(String productId, String brandId);

    /**
     * Fine-grained linking: associates a warranty (or null to detach) with the
     * given product.
     */
    void linkWarranty(String productId, String warrantyId);

    /**
     * Fine-grained linking: changes the category of the given product.
     */
    void linkCategory(String productId, String categoryId);

    void bulkUpdateStatus(List<String> productIds, String status);

    void delete(String productId);

    void deleteAll(List<String> productIds);

    /**
     * Bulk product creation. Creates each product individually, accumulating errors
     * per row without aborting the entire batch.
     */
    BulkImportResult bulkCreate(List<Product> products);
}
