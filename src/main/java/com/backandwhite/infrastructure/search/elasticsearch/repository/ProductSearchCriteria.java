package com.backandwhite.infrastructure.search.elasticsearch.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Aggregates the filters used to run a product search query against
 * Elasticsearch. Any field may be {@code null} to skip the corresponding
 * filter.
 */
public record ProductSearchCriteria(String query, List<String> categoryIds, String brand, Float minPrice,
        Float maxPrice, Boolean inStock, String sortBy, Pageable pageable) {
}
