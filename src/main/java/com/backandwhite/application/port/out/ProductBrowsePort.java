package com.backandwhite.application.port.out;

import java.util.List;

/**
 * Outbound port for paginated product browsing. Used when the catalogue is
 * indexed in a dedicated search engine (Elasticsearch) that can paginate and
 * filter faster than the relational store, while Postgres remains the source of
 * truth for translations and detail enrichment.
 */
public interface ProductBrowsePort {

    /**
     * Returns a page of product ids matching the filters, in the order chosen by
     * the backing engine. Implementations apply the PUBLISHED status filter
     * implicitly.
     */
    BrowsePage browse(BrowseCriteria criteria);

    record BrowseCriteria(String categoryId, List<String> categoryIds, String brand, Float minPrice, Float maxPrice,
            Boolean inStock, String sortBy, int page, int size) {
    }

    record BrowsePage(List<String> ids, long totalElements, int totalPages, int currentPage, int size) {
    }
}
