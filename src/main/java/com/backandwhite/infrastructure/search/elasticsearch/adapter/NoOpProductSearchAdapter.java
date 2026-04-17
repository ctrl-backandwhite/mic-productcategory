package com.backandwhite.infrastructure.search.elasticsearch.adapter;

import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.valueobject.ProductStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Log4j2
@Component
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpProductSearchAdapter implements ProductSearchIndexPort {

    @Override
    public void indexProduct(Product product) {
    }

    @Override
    public void indexProductDetail(ProductDetail detail) {
    }

    @Override
    public void indexBulk(List<Product> products) {
    }

    @Override
    public void indexBulkProductDetail(List<ProductDetail> details) {
    }

    @Override
    public void updateStock(String pid, Map<String, Integer> variantStock) {
    }

    @Override
    public void updateStatus(String productId, ProductStatus status) {
    }

    @Override
    public void removeProduct(String productId) {
    }

    @Override
    public void removeBulk(List<String> productIds) {
    }

    @Override
    public void deleteIndex() {
    }

    @Override
    public long countDocuments() {
        return 0;
    }
}
