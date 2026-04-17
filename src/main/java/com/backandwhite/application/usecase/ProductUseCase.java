package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductUseCase {

    List<Product> findByCategoryId(String categoryId, String locale, String status);

    Page<Product> findAllPaged(String locale, String categoryId, String status, String name, int page, int size,
            String sortBy, boolean ascending);

    Product findById(String productId, String locale);

    Product create(Product product);

    Product update(String productId, Product product);

    void publishProduct(String productId);

    void bulkUpdateStatus(List<String> productIds, String status);

    void delete(String productId);

    void deleteAll(List<String> productIds);

    /**
     * Bulk product creation. Creates each product individually, accumulating errors
     * per row without aborting the entire batch.
     */
    BulkImportResult bulkCreate(List<Product> products);
}
