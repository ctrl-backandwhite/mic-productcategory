package com.backandwhite.infrastructure.search.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;

@Log4j2
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchPage<ProductDocument> search(String query, List<String> categoryIds, String brand, Float minPrice,
            Float maxPrice, Boolean inStock, String sortBy, Pageable pageable) {

        NativeQuery nq = NativeQuery.builder().withQuery(q -> q.bool(bool -> {
            // Full-text search: fuzzy on title fields, exact on description
            if (query != null && !query.isBlank()) {
                bool.must(
                        m -> m.multiMatch(mm -> mm
                                .query(query).fields("name^3", "name.autocomplete^2", "categoryName^1.5",
                                        "brandName^1.5", "variants.name", "tags^2")
                                .type(TextQueryType.BestFields).fuzziness("AUTO")));
                // Description match boosts score but is NOT required
                // (description is not in the must clause to prevent false positives
                // e.g. a vest whose description mentions "jeans" should NOT match "jean")
                bool.should(s -> s.match(mm -> mm.field("description").query(query)));
            }

            // Only show published products
            bool.filter(f -> f.term(t -> t.field("status").value("PUBLISHED")));

            // Category filter
            if (categoryIds != null && !categoryIds.isEmpty()) {
                bool.filter(f -> f.terms(t -> t.field("categoryId").terms(tv -> tv.value(
                        categoryIds.stream().map(co.elastic.clients.elasticsearch._types.FieldValue::of).toList()))));
            }

            // Brand filter
            if (brand != null && !brand.isBlank()) {
                bool.filter(f -> f.term(t -> t.field("brandSlug").value(brand)));
            }

            // Price range filter
            if (minPrice != null || maxPrice != null) {
                bool.filter(f -> f.range(r -> r.number(n -> {
                    n.field("price");
                    if (minPrice != null)
                        n.gte(minPrice.doubleValue());
                    if (maxPrice != null)
                        n.lte(maxPrice.doubleValue());
                    return n;
                })));
            }

            // In stock filter
            if (inStock != null && inStock) {
                bool.filter(f -> f.term(t -> t.field("inStock").value(true)));
            }

            return bool;
        })).withPageable(pageable).withHighlightQuery(buildHighlightQuery()).build();

        // Sorting
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc" -> nq = NativeQuery.builder().withQuery(nq.getQuery()).withPageable(pageable)
                        .withHighlightQuery(buildHighlightQuery())
                        .withSort(s -> s.field(sf -> sf.field("price").order(SortOrder.Asc))).build();
                case "price_desc" -> nq = NativeQuery.builder().withQuery(nq.getQuery()).withPageable(pageable)
                        .withHighlightQuery(buildHighlightQuery())
                        .withSort(s -> s.field(sf -> sf.field("price").order(SortOrder.Desc))).build();
                case "newest" -> nq = NativeQuery.builder().withQuery(nq.getQuery()).withPageable(pageable)
                        .withHighlightQuery(buildHighlightQuery())
                        .withSort(s -> s.field(sf -> sf.field("createdAt").order(SortOrder.Desc))).build();
                default -> {
                } // relevance (score-based, no explicit sort)
            }
        }

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(nq, ProductDocument.class);
        return SearchHitSupport.searchPageFor(hits, pageable);
    }

    @Override
    public List<ProductDocument> autocomplete(String prefix, int limit) {
        if (prefix == null || prefix.isBlank())
            return List.of();

        NativeQuery nq = NativeQuery.builder().withQuery(q -> q.bool(bool -> {
            bool.must(m -> m.match(mm -> mm.field("name.autocomplete").query(prefix)));
            bool.filter(f -> f.term(t -> t.field("status").value("PUBLISHED")));
            return bool;
        })).withPageable(Pageable.ofSize(limit)).build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(nq, ProductDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).filter(doc -> doc.getName() != null).toList();
    }

    private HighlightQuery buildHighlightQuery() {
        HighlightParameters params = HighlightParameters.builder().withPreTags("<mark>").withPostTags("</mark>")
                .withNumberOfFragments(1).withFragmentSize(150).build();

        List<HighlightField> fields = List.of(new HighlightField("name"), new HighlightField("description"),
                new HighlightField("categoryName"));

        return new HighlightQuery(new Highlight(params, fields), ProductDocument.class);
    }
}
