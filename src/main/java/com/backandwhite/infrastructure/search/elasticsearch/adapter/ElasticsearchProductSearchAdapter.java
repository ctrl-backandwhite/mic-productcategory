package com.backandwhite.infrastructure.search.elasticsearch.adapter;

import com.backandwhite.application.port.out.ProductSearchIndexPort;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import com.backandwhite.infrastructure.search.elasticsearch.mapper.ProductDocumentMapper;
import com.backandwhite.infrastructure.search.elasticsearch.repository.ProductSearchRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true")
public class ElasticsearchProductSearchAdapter implements ProductSearchIndexPort {

    private final ProductSearchRepository searchRepository;
    private final ProductDocumentMapper documentMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    @Async
    public void indexProduct(Product product) {
        try {
            ProductDocument doc = documentMapper.fromProduct(product);
            searchRepository.save(doc);
            log.debug("Indexed product id={}", product.getId());
        } catch (Exception e) {
            log.error("Failed to index product id={}: {}", product.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void indexProductDetail(ProductDetail detail) {
        try {
            ProductDocument doc = documentMapper.fromProductDetail(detail);
            searchRepository.save(doc);
            log.debug("Indexed product detail pid={}", detail.getPid());
        } catch (Exception e) {
            log.error("Failed to index product detail pid={}: {}", detail.getPid(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void indexBulk(List<Product> products) {
        try {
            List<ProductDocument> docs = products.stream().map(documentMapper::fromProduct).toList();
            searchRepository.saveAll(docs);
            log.info("Bulk indexed {} products", docs.size());
        } catch (Exception e) {
            log.error("Failed to bulk index {} products: {}", products.size(), e.getMessage(), e);
        }
    }

    @Override
    public void indexBulkProductDetail(List<ProductDetail> details) {
        try {
            List<ProductDocument> docs = details.stream().map(documentMapper::fromProductDetail).toList();
            searchRepository.saveAll(docs);
            log.info("Bulk indexed {} product details", docs.size());
        } catch (Exception e) {
            log.error("Failed to bulk index {} product details: {}", details.size(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void updateStock(String pid, Map<String, Integer> variantStock) {
        try {
            searchRepository.findById(pid).ifPresent(doc -> {
                int totalStock = variantStock.values().stream().mapToInt(Integer::intValue).sum();
                doc.setTotalStock(totalStock);
                doc.setInStock(totalStock > 0);

                if (doc.getVariants() != null) {
                    doc.getVariants().forEach(variant -> {
                        Integer stock = variantStock.get(variant.getVid());
                        if (stock != null) {
                            variant.setStock(stock);
                            variant.setInStock(stock > 0);
                        }
                    });
                }

                searchRepository.save(doc);
                log.debug("Updated stock for pid={}, totalStock={}", pid, totalStock);
            });
        } catch (Exception e) {
            log.error("Failed to update stock for pid={}: {}", pid, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void updateStatus(String productId, ProductStatus status) {
        try {
            searchRepository.findById(productId).ifPresent(doc -> {
                doc.setStatus(status.name());
                searchRepository.save(doc);
                log.debug("Updated status for product id={} to {}", productId, status);
            });
        } catch (Exception e) {
            log.error("Failed to update status for product id={}: {}", productId, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void removeProduct(String productId) {
        try {
            searchRepository.deleteById(productId);
            log.debug("Removed product id={} from index", productId);
        } catch (Exception e) {
            log.error("Failed to remove product id={}: {}", productId, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void removeBulk(List<String> productIds) {
        try {
            productIds.forEach(searchRepository::deleteById);
            log.info("Bulk removed {} products from index", productIds.size());
        } catch (Exception e) {
            log.error("Failed to bulk remove products: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            if (indexOps.exists()) {
                indexOps.delete();
                log.info("Deleted 'products' index");
            }
            indexOps.createWithMapping();
            log.info("Recreated 'products' index with mappings");
        } catch (Exception e) {
            log.error("Failed to delete/recreate index: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public long countDocuments() {
        return searchRepository.count();
    }
}
