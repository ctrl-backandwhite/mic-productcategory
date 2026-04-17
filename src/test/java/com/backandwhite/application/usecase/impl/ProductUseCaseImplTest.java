package com.backandwhite.application.usecase.impl;

import com.backandwhite.common.exception.EntityNotFoundException;
import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.application.port.out.CatalogEventPort;
import com.backandwhite.application.port.out.ProductSearchIndexPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUseCaseImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CatalogEventPort catalogEventPort;

    @Mock
    private ProductSearchIndexPort productSearchIndexPort;

    @InjectMocks
    private ProductUseCaseImpl productUseCase;

    @Test
    void findByCategoryId_returnsProductList() {
        List<Product> products = List.of(product(CATEGORY_ID));
        when(productRepository.findByCategoryId(CATEGORY_ID, "es", null)).thenReturn(products);

        List<Product> result = productUseCase.findByCategoryId(CATEGORY_ID, "es", null);

        assertSame(products, result);
        verify(productRepository).findByCategoryId(CATEGORY_ID, "es", null);
    }

    @Test
    void findAllPaged_returnsPage() {
        Page<Product> page = new PageImpl<>(List.of(product(CATEGORY_ID)));
        when(productRepository.findAllPaged(eq("es"), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        Page<Product> result = productUseCase.findAllPaged("es", null, null, null, 0, 20, "createdAt", true);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAllPaged(eq("es"), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void findById_existingProduct_returnsProduct() {
        Product model = product(CATEGORY_ID);
        when(productRepository.findById(PRODUCT_ID, "es")).thenReturn(Optional.of(model));

        Product result = productUseCase.findById(PRODUCT_ID, "es");

        assertSame(model, result);
        verify(productRepository).findById(PRODUCT_ID, "es");
    }

    @Test
    void findById_missingProduct_throwsEntityNotFoundException() {
        when(productRepository.findById("non-existent", "es")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productUseCase.findById("non-existent", "es"));
        verify(productRepository).findById("non-existent", "es");
    }

    @Test
    void create_delegatesToRepository() {
        Product model = product(CATEGORY_ID);
        when(productRepository.save(model)).thenReturn(model);

        Product result = productUseCase.create(model);

        assertSame(model, result);
        verify(productRepository).save(model);
    }

    @Test
    void update_delegatesToRepository() {
        Product model = product(CATEGORY_ID);
        when(productRepository.update(PRODUCT_ID, model)).thenReturn(model);

        Product result = productUseCase.update(PRODUCT_ID, model);

        assertSame(model, result);
        verify(productRepository).update(PRODUCT_ID, model);
    }

    @Test
    void publishProduct_draftProduct_setsPublished() {
        Product draft = product(CATEGORY_ID).withStatus(ProductStatus.DRAFT);
        when(productRepository.findById(PRODUCT_ID, null)).thenReturn(Optional.of(draft));

        productUseCase.publishProduct(PRODUCT_ID);

        verify(productRepository).updateStatus(PRODUCT_ID, ProductStatus.PUBLISHED);
    }

    @Test
    void publishProduct_publishedProduct_setsDraft() {
        Product published = product(CATEGORY_ID).withStatus(ProductStatus.PUBLISHED);
        when(productRepository.findById(PRODUCT_ID, null)).thenReturn(Optional.of(published));

        productUseCase.publishProduct(PRODUCT_ID);

        verify(productRepository).updateStatus(PRODUCT_ID, ProductStatus.DRAFT);
    }

    @Test
    void publishProduct_notFound_throwsEntityNotFoundException() {
        when(productRepository.findById("missing", null)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productUseCase.publishProduct("missing"));
    }

    @Test
    void deleteAll_delegatesToRepository() {
        List<String> ids = List.of(PRODUCT_ID, OTHER_PRODUCT_ID);

        productUseCase.deleteAll(ids);

        verify(productRepository).deleteAll(ids);
    }

    @Test
    void bulkUpdateStatus_validStatus_delegatesToRepository() {
        List<String> ids = List.of(PRODUCT_ID);

        productUseCase.bulkUpdateStatus(ids, "PUBLISHED");

        verify(productRepository).bulkUpdateStatus(ids, ProductStatus.PUBLISHED);
    }

    @Test
    void bulkCreate_allSucceed_returnsResult() {
        List<Product> products = List.of(product(CATEGORY_ID), otherProduct(CATEGORY_ID));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BulkImportResult result = productUseCase.bulkCreate(products);

        assertThat(result.getCreated()).isEqualTo(2);
        assertThat(result.getFailed()).isEqualTo(0);
        assertThat(result.getTotalRows()).isEqualTo(2);
    }

    @Test
    void bulkCreate_someFail_recordsErrors() {
        Product validProduct = product(CATEGORY_ID);
        Product failingProduct = otherProduct(CATEGORY_ID);

        when(productRepository.save(validProduct)).thenReturn(validProduct);
        when(productRepository.save(failingProduct)).thenThrow(new RuntimeException("DB error"));

        BulkImportResult result = productUseCase.bulkCreate(List.of(validProduct, failingProduct));

        assertThat(result.getCreated()).isEqualTo(1);
        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
    }
}
