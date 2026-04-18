package com.backandwhite.infrastructure.search.elasticsearch.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.valueobject.ProductStatus;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NoOpProductSearchAdapterTest {

    private final NoOpProductSearchAdapter adapter = new NoOpProductSearchAdapter();

    @Test
    void allMethods_doNothing() {
        adapter.indexProduct(Product.builder().build());
        adapter.indexProductDetail(ProductDetail.builder().build());
        adapter.indexBulk(List.of());
        adapter.indexBulkProductDetail(List.of());
        adapter.updateStock("pid", Map.of());
        adapter.updateStatus("pid", ProductStatus.DRAFT);
        adapter.removeProduct("pid");
        adapter.removeBulk(List.of("a"));
        adapter.deleteIndex();
        assertThat(adapter.countDocuments()).isZero();
    }
}
