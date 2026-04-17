package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.valueobject.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findByCategoryId(String categoryId, String locale, ProductStatus status);

    Page<Product> findAllPaged(String locale, String categoryId, ProductStatus status, String name, Pageable pageable);

    Optional<Product> findById(String productId, String locale);

    boolean existsById(String productId);

    Product save(Product product);

    Product update(String productId, Product product);

    void enrichDetail(String productId, String description, String productImageSet);

    void updateStatus(String productId, ProductStatus status);

    void bulkUpdateStatus(List<String> productIds, ProductStatus status);

    void delete(String productId);

    void deleteAll(List<String> productIds);

    Page<String> findAllProductIds(int page, int size);

    Page<String> findProductIdsByCategoryIds(List<String> categoryIds, int page, int size);

    /**
     * Bulk sync: receives a list of Products (already mapped from CJ).
     * For each one, if it exists it updates it; if not, it creates it.
     * Performs a single bulk read and a single saveAll.
     *
     * @param forceOverwrite true = overwrite all fields; false = only update
     *                       changed fields (skip unchanged)
     * @return int[]{created, updated, skipped}
     */
    int[] bulkSyncProducts(List<Product> products, boolean forceOverwrite);
}
