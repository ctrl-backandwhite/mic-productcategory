package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backandwhite.api.dto.out.AutocompleteSuggestion;
import com.backandwhite.api.dto.out.ProductSearchResponse;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import com.backandwhite.infrastructure.search.elasticsearch.repository.ProductSearchRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;

@ExtendWith(MockitoExtension.class)
class ProductSearchUseCaseImplTest {

    @Mock
    private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductSearchUseCaseImpl useCase;

    @Test
    @SuppressWarnings("unchecked")
    void search_mapsResults() {
        ProductDocument doc = ProductDocument.builder().id("1").pid("p1").name("X").description("d").categoryName("c")
                .brandName("b").price(10f).originalPrice(12f).inStock(true).totalStock(5).imageUrl("url")
                .status("PUBLISHED").build();
        SearchHit<ProductDocument> hit = mock(SearchHit.class);
        when(hit.getContent()).thenReturn(doc);
        when(hit.getHighlightFields()).thenReturn(java.util.Map.of());

        SearchHits<ProductDocument> hits = mock(SearchHits.class);
        when(hits.getTotalHits()).thenReturn(1L);
        when(hits.iterator()).thenReturn(List.of(hit).iterator());

        SearchPage<ProductDocument> page = mock(SearchPage.class);
        when(page.getSearchHits()).thenReturn(hits);

        when(productSearchRepository.search(anyString(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ProductSearchResponse response = useCase.search("q", List.of("cat"), "b", 0f, 100f, true, "price", 0, 10);

        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().getFirst().getId()).isEqualTo("1");
        assertThat(response.getTotalHits()).isEqualTo(1);
    }

    @Test
    void autocomplete_mapsSuggestions() {
        ProductDocument doc = ProductDocument.builder().pid("p1").name("Phone").imageUrl("url").price(12f).build();
        when(productSearchRepository.autocomplete("ph", 5)).thenReturn(List.of(doc));
        List<AutocompleteSuggestion> result = useCase.autocomplete("ph", 5);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getText()).isEqualTo("Phone");
    }

    @Test
    void autocomplete_empty_returnsEmpty() {
        when(productSearchRepository.autocomplete("x", 5)).thenReturn(Collections.emptyList());
        assertThat(useCase.autocomplete("x", 5)).isEmpty();
    }
}
