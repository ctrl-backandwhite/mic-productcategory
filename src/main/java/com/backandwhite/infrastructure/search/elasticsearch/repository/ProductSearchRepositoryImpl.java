package com.backandwhite.infrastructure.search.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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

    private static final String FIELD_PRICE = "price";
    private static final String FIELD_STATUS = "status";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchPage<ProductDocument> search(ProductSearchCriteria criteria) {
        // trackTotalHits(true) tells ES to count every matching document instead
        // of stopping at the default 10 000 threshold. Without it the admin
        // product list (and every count that uses this query) is clamped to
        // "10000 en catálogo" even when the real total is larger or smaller.
        NativeQuery nq = NativeQuery.builder().withQuery(q -> q.bool(bool -> buildBoolQuery(bool, criteria)))
                .withPageable(criteria.pageable()).withHighlightQuery(buildHighlightQuery()).withTrackTotalHits(true)
                .build();

        nq = applySort(nq, criteria);

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(nq, ProductDocument.class);
        return SearchHitSupport.searchPageFor(hits, criteria.pageable());
    }

    @Override
    public List<ProductDocument> autocomplete(String prefix, int limit) {
        if (prefix == null || prefix.isBlank())
            return List.of();

        NativeQuery nq = NativeQuery.builder().withQuery(q -> q.bool(bool -> {
            bool.must(m -> m.match(mm -> mm.field("name.autocomplete").query(prefix)));
            bool.filter(f -> f.term(t -> t.field(FIELD_STATUS).value(STATUS_PUBLISHED)));
            return bool;
        })).withPageable(Pageable.ofSize(limit)).build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(nq, ProductDocument.class);
        return hits.getSearchHits().stream().map(SearchHit::getContent).filter(doc -> doc.getName() != null).toList();
    }

    private BoolQuery.Builder buildBoolQuery(BoolQuery.Builder bool, ProductSearchCriteria criteria) {
        applyFullTextQuery(bool, criteria.query());
        bool.filter(f -> f.term(t -> t.field(FIELD_STATUS).value(STATUS_PUBLISHED)));
        applyCategoryFilter(bool, criteria.categoryIds());
        applyBrandFilter(bool, criteria.brand());
        applyPriceFilter(bool, criteria.minPrice(), criteria.maxPrice());
        applyInStockFilter(bool, criteria.inStock());
        return bool;
    }

    private static void applyFullTextQuery(BoolQuery.Builder bool, String query) {
        if (query == null || query.isBlank()) {
            return;
        }
        bool.must(m -> m.multiMatch(mm -> mm.query(query)
                .fields("name^3", "name.autocomplete^2", "categoryName^1.5", "brandName^1.5", "variants.name", "tags^2")
                .type(TextQueryType.BestFields).fuzziness("AUTO")));
        bool.should(s -> s.match(mm -> mm.field("description").query(query)));
    }

    private static void applyCategoryFilter(BoolQuery.Builder bool, List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        bool.filter(f -> f.terms(t -> t.field("categoryId").terms(tv -> tv
                .value(categoryIds.stream().map(co.elastic.clients.elasticsearch._types.FieldValue::of).toList()))));
    }

    private static void applyBrandFilter(BoolQuery.Builder bool, String brand) {
        if (brand == null || brand.isBlank()) {
            return;
        }
        bool.filter(f -> f.term(t -> t.field("brandSlug").value(brand)));
    }

    private static void applyPriceFilter(BoolQuery.Builder bool, Float minPrice, Float maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return;
        }
        bool.filter(f -> f.range(r -> r.number(n -> {
            n.field(FIELD_PRICE);
            if (minPrice != null) {
                n.gte(minPrice.doubleValue());
            }
            if (maxPrice != null) {
                n.lte(maxPrice.doubleValue());
            }
            return n;
        })));
    }

    private static void applyInStockFilter(BoolQuery.Builder bool, Boolean inStock) {
        if (!Boolean.TRUE.equals(inStock)) {
            return;
        }
        bool.filter(f -> f.term(t -> t.field("inStock").value(true)));
    }

    private NativeQuery applySort(NativeQuery nq, ProductSearchCriteria criteria) {
        String sortBy = criteria.sortBy();
        if (sortBy == null) {
            return nq;
        }
        return switch (sortBy) {
            case "price_asc" -> rebuildWithSort(nq, criteria.pageable(),
                    s -> s.field(sf -> sf.field(FIELD_PRICE).order(SortOrder.Asc)));
            case "price_desc" -> rebuildWithSort(nq, criteria.pageable(),
                    s -> s.field(sf -> sf.field(FIELD_PRICE).order(SortOrder.Desc)));
            case "newest" -> rebuildWithSort(nq, criteria.pageable(),
                    s -> s.field(sf -> sf.field("createdAt").order(SortOrder.Desc)));
            default -> nq;
        };
    }

    private NativeQuery rebuildWithSort(NativeQuery nq, Pageable pageable,
            Function<co.elastic.clients.elasticsearch._types.SortOptions.Builder, co.elastic.clients.util.ObjectBuilder<co.elastic.clients.elasticsearch._types.SortOptions>> sort) {
        return NativeQuery.builder().withQuery(Objects.requireNonNull(nq.getQuery())).withPageable(pageable)
                .withHighlightQuery(buildHighlightQuery()).withSort(sort).build();
    }

    private HighlightQuery buildHighlightQuery() {
        HighlightParameters params = HighlightParameters.builder().withPreTags("<mark>").withPostTags("</mark>")
                .withNumberOfFragments(1).withFragmentSize(150).build();

        List<HighlightField> fields = List.of(new HighlightField("name"), new HighlightField("description"),
                new HighlightField("categoryName"));

        return new HighlightQuery(new Highlight(params, fields), ProductDocument.class);
    }
}
