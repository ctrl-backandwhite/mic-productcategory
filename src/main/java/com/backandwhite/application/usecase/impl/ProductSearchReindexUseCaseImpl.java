package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.application.usecase.ProductSearchReindexUseCase;
import com.backandwhite.domain.model.Product;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.ProductInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.ProductJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductSearchReindexUseCaseImpl implements ProductSearchReindexUseCase {

    private static final int BATCH_SIZE = 500;

    private final ProductJpaRepository productJpaRepository;
    private final ProductInfraMapper productInfraMapper;
    private final ProductSearchIndexPort productSearchIndexPort;

    @Override
    @Transactional(readOnly = true)
    public long reindexAll() {
        log.info("Starting full Elasticsearch reindex (delete + recreate)...");
        long start = System.currentTimeMillis();

        productSearchIndexPort.deleteIndex();

        long totalIndexed = reindexBatches();

        long duration = System.currentTimeMillis() - start;
        log.info("Full reindex completed: indexed={}, durationMs={}", totalIndexed, duration);
        return totalIndexed;
    }

    @Override
    @Transactional(readOnly = true)
    public long reindexFromDb() {
        log.info("Starting incremental Elasticsearch reindex from DB (upsert, no index drop)...");
        long start = System.currentTimeMillis();

        long totalIndexed = reindexBatches();

        long duration = System.currentTimeMillis() - start;
        log.info("Incremental reindex completed: indexed={}, durationMs={}", totalIndexed, duration);
        return totalIndexed;
    }

    private long reindexBatches() {
        long totalIndexed = 0;
        int page = 0;
        Page<ProductEntity> batch;
        do {
            batch = productJpaRepository.findAll(PageRequest.of(page, BATCH_SIZE));
            List<Product> products = batch.getContent().stream().map(entity -> {
                try {
                    return productInfraMapper.toDomain(entity);
                } catch (Exception e) {
                    log.warn("Failed to map product id={}: {}", entity.getId(), e.getMessage());
                    return null;
                }
            }).filter(p -> p != null).toList();

            if (!products.isEmpty()) {
                productSearchIndexPort.indexBulk(products);
                totalIndexed += products.size();
            }

            log.info("Reindex progress: indexed {} / {} total", totalIndexed, batch.getTotalElements());
            page++;
        } while (batch.hasNext());
        return totalIndexed;
    }
}
