package com.backandwhite.application.usecase.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.domain.model.Product;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.ProductInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.ProductJpaRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductSearchReindexUseCaseImplTest {

    @Mock
    private ProductJpaRepository productJpaRepository;
    @Mock
    private ProductInfraMapper productInfraMapper;
    @Mock
    private ProductSearchIndexPort productSearchIndexPort;

    @InjectMocks
    private ProductSearchReindexUseCaseImpl useCase;

    @Test
    void reindexAll_deletesAndReindexes() {
        ProductEntity entity = ProductEntity.builder().id("1").build();
        Page<ProductEntity> page = new PageImpl<>(List.of(entity));
        when(productJpaRepository.findAll(any(Pageable.class))).thenReturn(page);
        Product p = Product.builder().id("1").build();
        when(productInfraMapper.toDomain(entity)).thenReturn(p);

        long total = useCase.reindexAll();
        assertThat(total).isEqualTo(1);
        verify(productSearchIndexPort).deleteIndex();
        verify(productSearchIndexPort).indexBulk(any());
    }

    @Test
    void reindexFromDb_skipsDelete() {
        ProductEntity entity = ProductEntity.builder().id("1").build();
        Page<ProductEntity> page = new PageImpl<>(List.of(entity));
        when(productJpaRepository.findAll(any(Pageable.class))).thenReturn(page);
        Product p = Product.builder().id("1").build();
        when(productInfraMapper.toDomain(entity)).thenReturn(p);

        long total = useCase.reindexFromDb();
        assertThat(total).isEqualTo(1);
        verify(productSearchIndexPort, org.mockito.Mockito.never()).deleteIndex();
    }

    @Test
    void reindexFromDb_emptyPage_noIndex() {
        Page<ProductEntity> empty = new PageImpl<>(List.of());
        when(productJpaRepository.findAll(any(Pageable.class))).thenReturn(empty);
        long total = useCase.reindexFromDb();
        assertThat(total).isZero();
        verify(productSearchIndexPort, org.mockito.Mockito.never()).indexBulk(any());
    }

    @Test
    void reindexFromDb_mapperThrows_filtersNull() {
        ProductEntity entity = ProductEntity.builder().id("1").build();
        Page<ProductEntity> page = new PageImpl<>(List.of(entity));
        when(productJpaRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(productInfraMapper.toDomain(entity)).thenThrow(new RuntimeException("map fail"));

        long total = useCase.reindexFromDb();
        assertThat(total).isZero();
    }
}
