package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.valureobject.ProductStatus;
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

    /**
     * Bulk sync: recibe una lista de Products (ya mapeados desde CJ).
     * Para cada uno, si existe lo actualiza; si no, lo crea.
     * Hace una sola lectura masiva y un solo saveAll.
     * 
     * @return int[]{created, updated}
     */
    int[] bulkSyncProducts(List<Product> products);
}
