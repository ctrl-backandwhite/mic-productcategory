package com.backandwhite.infrastructure.search.elasticsearch.adapter;

import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.valueobject.ProductStatus;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of {@link ProductSearchIndexPort} used when
 * Elasticsearch is disabled. All methods intentionally do nothing — indexing is
 * skipped.
 */
@Log4j2
@Component
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpProductSearchAdapter implements ProductSearchIndexPort {

    @Override
    public void indexProduct(Product product) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void indexProductDetail(ProductDetail detail) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void indexBulk(List<Product> products) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void indexBulkProductDetail(List<ProductDetail> details) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void updateStock(String pid, Map<String, Integer> variantStock) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void updateStatus(String productId, ProductStatus status) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void removeProduct(String productId) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void removeBulk(List<String> productIds) {
        // No-op: Elasticsearch disabled
    }

    @Override
    public void deleteIndex() {
        // No-op: Elasticsearch disabled
    }

    @Override
    public long countDocuments() {
        return 0;
    }
}
