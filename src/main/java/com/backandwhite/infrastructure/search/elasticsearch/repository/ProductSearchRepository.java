package com.backandwhite.infrastructure.search.elasticsearch.repository;

import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository
                extends ElasticsearchRepository<ProductDocument, String>, ProductSearchRepositoryCustom {
}
