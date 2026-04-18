package com.backandwhite.application.usecase.impl;

import com.backandwhite.api.dto.out.AutocompleteSuggestion;
import com.backandwhite.api.dto.out.ProductSearchResponse;
import com.backandwhite.application.usecase.ProductSearchUseCase;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import com.backandwhite.infrastructure.search.elasticsearch.repository.ProductSearchCriteria;
import com.backandwhite.infrastructure.search.elasticsearch.repository.ProductSearchRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductSearchUseCaseImpl implements ProductSearchUseCase {

    private final ProductSearchRepository productSearchRepository;

    @Override
    public ProductSearchResponse search(String query, List<String> categoryIds, String brand, Float minPrice,
            Float maxPrice, Boolean inStock, String sortBy, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        ProductSearchCriteria criteria = new ProductSearchCriteria(query, categoryIds, brand, minPrice, maxPrice,
                inStock, sortBy, pageable);
        SearchPage<ProductDocument> searchPage = productSearchRepository.search(criteria);

        List<ProductSearchResponse.ProductSearchHit> hits = new ArrayList<>();
        for (SearchHit<ProductDocument> hit : searchPage.getSearchHits()) {
            ProductDocument doc = hit.getContent();
            hits.add(ProductSearchResponse.ProductSearchHit.builder().id(doc.getId()).pid(doc.getPid())
                    .name(doc.getName()).description(doc.getDescription()).categoryName(doc.getCategoryName())
                    .brandName(doc.getBrandName()).price(doc.getPrice()).originalPrice(doc.getOriginalPrice())
                    .inStock(doc.getInStock()).totalStock(doc.getTotalStock()).imageUrl(doc.getImageUrl())
                    .status(doc.getStatus()).highlights(hit.getHighlightFields()).build());
        }

        return ProductSearchResponse.builder().results(hits).totalHits(searchPage.getSearchHits().getTotalHits())
                .page(page).size(size).build();
    }

    @Override
    public List<AutocompleteSuggestion> autocomplete(String query, int limit) {
        // Use the dedicated edge-ngram autocomplete — NOT the fuzzy search —
        // to avoid returning unrelated products that happen to mention the
        // search term somewhere in their description.
        return productSearchRepository.autocomplete(query, limit).stream().map(doc -> AutocompleteSuggestion.builder()
                .text(doc.getName()).pid(doc.getPid()).imageUrl(doc.getImageUrl()).price(doc.getPrice()).build())
                .toList();
    }
}
