package com.backandwhite.application.usecase;

import com.backandwhite.api.dto.out.AutocompleteSuggestion;
import com.backandwhite.api.dto.out.ProductSearchResponse;

import java.util.List;

public interface ProductSearchUseCase {

    ProductSearchResponse search(
            String query,
            List<String> categoryIds,
            String brand,
            Float minPrice,
            Float maxPrice,
            Boolean inStock,
            String sortBy,
            int page,
            int size);

    List<AutocompleteSuggestion> autocomplete(String query, int limit);
}
