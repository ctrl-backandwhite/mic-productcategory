package com.backandwhite.infrastructure.search.elasticsearch.adapter;

import com.backandwhite.application.port.out.ProductBrowsePort;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import com.backandwhite.infrastructure.search.elasticsearch.repository.ProductSearchCriteria;
import com.backandwhite.infrastructure.search.elasticsearch.repository.ProductSearchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch adapter for paginated browsing (no full-text query). Returns
 * only the ids so Postgres can load the full entity with the active locale's
 * translations — ES stores English only.
 */
@Log4j2
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true")
public class ElasticsearchProductBrowseAdapter implements ProductBrowsePort {

    private final ProductSearchRepository searchRepository;

    @Override
    public BrowsePage browse(BrowseCriteria criteria) {
        Pageable pageable = PageRequest.of(Math.max(0, criteria.page()), Math.max(1, criteria.size()));
        List<String> categoryIds = criteria.categoryIds();
        if ((categoryIds == null || categoryIds.isEmpty()) && criteria.categoryId() != null
                && !criteria.categoryId().isBlank()) {
            categoryIds = List.of(criteria.categoryId());
        }
        ProductSearchCriteria esCriteria = new ProductSearchCriteria(null, categoryIds, criteria.brand(),
                criteria.minPrice(), criteria.maxPrice(), criteria.inStock(), mapSort(criteria.sortBy()), pageable);

        SearchPage<ProductDocument> hits = searchRepository.search(esCriteria);
        List<String> ids = hits.getSearchHits().stream().map(SearchHit::getContent).map(ProductDocument::getId)
                .toList();
        return new BrowsePage(ids, hits.getTotalElements(), hits.getTotalPages(), pageable.getPageNumber(),
                pageable.getPageSize());
    }

    /** Maps app-level sort keys to Elasticsearch-understood ones. */
    private static String mapSort(String sortBy) {
        if (sortBy == null)
            return null;
        return switch (sortBy.toLowerCase()) {
            case "price-low", "price_asc" -> "price_asc";
            case "price-high", "price_desc" -> "price_desc";
            case "createdat", "newest" -> "newest";
            default -> null;
        };
    }
}
