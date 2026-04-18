package com.backandwhite.infrastructure.search.elasticsearch.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import com.backandwhite.infrastructure.search.elasticsearch.mapper.ProductDocumentMapper;
import com.backandwhite.infrastructure.search.elasticsearch.repository.ProductSearchRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

@ExtendWith(MockitoExtension.class)
class ElasticsearchProductSearchAdapterTest {

    @Mock
    private ProductSearchRepository searchRepository;
    @Mock
    private ProductDocumentMapper documentMapper;
    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ElasticsearchProductSearchAdapter adapter;

    @Test
    void indexProduct_happyPath() {
        Product p = Product.builder().id("1").build();
        ProductDocument doc = ProductDocument.builder().id("1").build();
        when(documentMapper.fromProduct(p)).thenReturn(doc);
        adapter.indexProduct(p);
        verify(searchRepository).save(doc);
    }

    @Test
    void indexProduct_exception_swallowed() {
        Product p = Product.builder().id("1").build();
        when(documentMapper.fromProduct(p)).thenThrow(new RuntimeException("fail"));
        assertThatCode(() -> adapter.indexProduct(p)).doesNotThrowAnyException();
    }

    @Test
    void indexProductDetail_happyPath() {
        ProductDetail detail = ProductDetail.builder().pid("pid").build();
        ProductDocument doc = ProductDocument.builder().build();
        when(documentMapper.fromProductDetail(detail)).thenReturn(doc);
        adapter.indexProductDetail(detail);
        verify(searchRepository).save(doc);
    }

    @Test
    void indexProductDetail_exception_swallowed() {
        ProductDetail detail = ProductDetail.builder().pid("pid").build();
        when(documentMapper.fromProductDetail(detail)).thenThrow(new RuntimeException("x"));
        assertThatCode(() -> adapter.indexProductDetail(detail)).doesNotThrowAnyException();
    }

    @Test
    void indexBulk_happyPath() {
        Product p = Product.builder().id("1").build();
        ProductDocument doc = ProductDocument.builder().id("1").build();
        when(documentMapper.fromProduct(p)).thenReturn(doc);
        adapter.indexBulk(List.of(p));
        verify(searchRepository).saveAll(anyList());
    }

    @Test
    void indexBulk_exception_swallowed() {
        Product p = Product.builder().id("1").build();
        when(documentMapper.fromProduct(p)).thenThrow(new RuntimeException("x"));
        List<Product> list = List.of(p);
        assertThatCode(() -> adapter.indexBulk(list)).doesNotThrowAnyException();
    }

    @Test
    void indexBulkProductDetail_happyPath() {
        ProductDetail detail = ProductDetail.builder().pid("pid").build();
        ProductDocument doc = ProductDocument.builder().build();
        when(documentMapper.fromProductDetail(detail)).thenReturn(doc);
        adapter.indexBulkProductDetail(List.of(detail));
        verify(searchRepository).saveAll(anyList());
    }

    @Test
    void indexBulkProductDetail_exception_swallowed() {
        ProductDetail detail = ProductDetail.builder().pid("pid").build();
        when(documentMapper.fromProductDetail(detail)).thenThrow(new RuntimeException("x"));
        List<ProductDetail> list = List.of(detail);
        assertThatCode(() -> adapter.indexBulkProductDetail(list)).doesNotThrowAnyException();
    }

    @Test
    void updateStock_existingDoc() {
        ProductDocument doc = ProductDocument.builder().id("pid")
                .variants(List.of(ProductDocument.VariantDocument.builder().vid("v1").build())).build();
        when(searchRepository.findById("pid")).thenReturn(Optional.of(doc));
        adapter.updateStock("pid", Map.of("v1", 10));
        assertThat(doc.getTotalStock()).isEqualTo(10);
        assertThat(doc.getInStock()).isTrue();
    }

    @Test
    void updateStock_nullVariants_handled() {
        ProductDocument doc = ProductDocument.builder().id("pid").variants(null).build();
        when(searchRepository.findById("pid")).thenReturn(Optional.of(doc));
        adapter.updateStock("pid", Map.of("v1", 5));
        assertThat(doc.getTotalStock()).isEqualTo(5);
    }

    @Test
    void updateStock_missingDoc_noOp() {
        when(searchRepository.findById("pid")).thenReturn(Optional.empty());
        Map<String, Integer> emptyStock = Map.of();
        assertThatCode(() -> adapter.updateStock("pid", emptyStock)).doesNotThrowAnyException();
    }

    @Test
    void updateStock_exception_swallowed() {
        when(searchRepository.findById("pid")).thenThrow(new RuntimeException("x"));
        Map<String, Integer> emptyStock = Map.of();
        assertThatCode(() -> adapter.updateStock("pid", emptyStock)).doesNotThrowAnyException();
    }

    @Test
    void updateStatus_existingDoc() {
        ProductDocument doc = ProductDocument.builder().id("p1").build();
        when(searchRepository.findById("p1")).thenReturn(Optional.of(doc));
        adapter.updateStatus("p1", ProductStatus.PUBLISHED);
        assertThat(doc.getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    void updateStatus_exception_swallowed() {
        when(searchRepository.findById("p1")).thenThrow(new RuntimeException("x"));
        assertThatCode(() -> adapter.updateStatus("p1", ProductStatus.DRAFT)).doesNotThrowAnyException();
    }

    @Test
    void removeProduct_happyPath() {
        adapter.removeProduct("p1");
        verify(searchRepository).deleteById("p1");
    }

    @Test
    void removeProduct_exception_swallowed() {
        doThrow(new RuntimeException("x")).when(searchRepository).deleteById(anyString());
        assertThatCode(() -> adapter.removeProduct("p1")).doesNotThrowAnyException();
    }

    @Test
    void removeBulk_happyPath() {
        adapter.removeBulk(List.of("a", "b"));
        verify(searchRepository).deleteById("a");
        verify(searchRepository).deleteById("b");
    }

    @Test
    void removeBulk_exception_swallowed() {
        doThrow(new RuntimeException("x")).when(searchRepository).deleteById(anyString());
        List<String> ids = List.of("a");
        assertThatCode(() -> adapter.removeBulk(ids)).doesNotThrowAnyException();
    }

    @Test
    void deleteIndex_existingIndex_recreates() {
        IndexOperations ops = org.mockito.Mockito.mock(IndexOperations.class);
        when(elasticsearchOperations.indexOps(any(Class.class))).thenReturn(ops);
        when(ops.exists()).thenReturn(true);
        adapter.deleteIndex();
        verify(ops).delete();
        verify(ops).createWithMapping();
    }

    @Test
    void deleteIndex_nonExistingIndex_onlyCreates() {
        IndexOperations ops = org.mockito.Mockito.mock(IndexOperations.class);
        when(elasticsearchOperations.indexOps(any(Class.class))).thenReturn(ops);
        when(ops.exists()).thenReturn(false);
        adapter.deleteIndex();
        verify(ops).createWithMapping();
    }

    @Test
    void deleteIndex_exception_rethrown() {
        when(elasticsearchOperations.indexOps(any(Class.class))).thenThrow(new RuntimeException("x"));
        assertThatThrownBy(() -> adapter.deleteIndex()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void countDocuments_delegates() {
        when(searchRepository.count()).thenReturn(5L);
        assertThat(adapter.countDocuments()).isEqualTo(5L);
    }
}
