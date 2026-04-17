package com.backandwhite.application.port.out;

import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.valueobject.ProductStatus;

import java.util.List;
import java.util.Map;

public interface ProductSearchIndexPort {

    void indexProduct(Product product);

    void indexProductDetail(ProductDetail detail);

    void indexBulk(List<Product> products);

    void indexBulkProductDetail(List<ProductDetail> details);

    void updateStock(String pid, Map<String, Integer> variantStock);

    void updateStatus(String productId, ProductStatus status);

    void removeProduct(String productId);

    void removeBulk(List<String> productIds);

    void deleteIndex();

    long countDocuments();
}
