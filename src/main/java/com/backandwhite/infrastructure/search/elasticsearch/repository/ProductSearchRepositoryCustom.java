package com.backandwhite.infrastructure.search.elasticsearch.repository;

import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;

import java.util.List;

public interface ProductSearchRepositoryCustom {

    SearchPage<ProductDocument> search(
            String query,
            List<String> categoryIds,
            String brand,
            Float minPrice,
            Float maxPrice,
            Boolean inStock,
            String sortBy,
            Pageable pageable);

    List<ProductDocument> autocomplete(String prefix, int limit);
}
