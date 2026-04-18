package com.backandwhite.infrastructure.search.elasticsearch.repository;

import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import java.util.List;
import org.springframework.data.elasticsearch.core.SearchPage;

public interface ProductSearchRepositoryCustom {

    SearchPage<ProductDocument> search(ProductSearchCriteria criteria);

    List<ProductDocument> autocomplete(String prefix, int limit);
}
