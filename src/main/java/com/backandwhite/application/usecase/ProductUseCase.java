package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import org.springframework.data.domain.Page;

import java.util.List;

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
     * Carga masiva de productos. Crea cada producto individualmente,
     * acumulando errores por fila sin abortar el lote completo.
     */
    BulkImportResult bulkCreate(List<Product> products);
}
