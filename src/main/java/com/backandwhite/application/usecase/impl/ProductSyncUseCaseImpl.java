package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.usecase.ProductSyncUseCase;
import com.backandwhite.domain.exception.ExternalServiceException;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductSyncResult;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.infrastructure.client.cj.client.CjDropshippingClient;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductSyncUseCaseImpl implements ProductSyncUseCase {

    private static final int PAGE_SIZE = 100;
    private static final long DELAY_BETWEEN_PAGES_MS = 10_000;
    /** Máximo de requests paralelos a CJ para no saturar el rate limit */
    private static final int PARALLEL_FETCH_THREADS = 5;

    private final ExecutorService fetchExecutor = Executors.newFixedThreadPool(
            PARALLEL_FETCH_THREADS,
            r -> { Thread t = Thread.ofVirtual().unstarted(r); t.setDaemon(true); return t; });

    private final CjDropshippingClient cjClient;
    private final ProductRepository productRepository;
    private final CjProductDetailMapper cjProductDetailMapper;

    @Override
    public ProductSyncResult syncFromCjDropshipping() {
        log.info("Starting CJ Dropshipping full product sync (iterating local DB products)...");

        int totalCreated = 0;
        int totalUpdated = 0;
        int totalSkipped = 0;
        int page = 0;

        while (true) {
            var idsPage = productRepository.findAllProductIds(page, PAGE_SIZE);
            List<String> productIds = idsPage.getContent();

            if (productIds.isEmpty()) {
                log.info("No more local products on page {}. Full sync finished.", page);
                break;
            }

            log.info("Processing page {} with {} local products (parallel={})...",
                    page, productIds.size(), PARALLEL_FETCH_THREADS);

            // Fetch en paralelo — máx PARALLEL_FETCH_THREADS requests concurrentes a CJ
            List<CompletableFuture<Optional<Product>>> futures = productIds.stream()
                    .map(pid -> CompletableFuture.supplyAsync(() -> {
                        try {
                            CjProductDetailDto cjProduct = cjClient.getProductDetail(pid);
                            return Optional.of(cjProductDetailMapper.toProduct(cjProduct));
                        } catch (ExternalServiceException e) {
                            log.warn("CJ API error for pid={}: {}", pid, e.getMessage());
                            return Optional.<Product>empty();
                        } catch (Exception e) {
                            log.warn("Failed to fetch product pid={}: {}", pid, e.getMessage());
                            return Optional.<Product>empty();
                        }
                    }, fetchExecutor))
                    .toList();

            List<Product> productsToSync = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            int skipped = productIds.size() - productsToSync.size();

            // Bulk save/update all fetched products at once
            if (!productsToSync.isEmpty()) {
                try {
                    int[] result = productRepository.bulkSyncProducts(productsToSync);
                    totalCreated += result[0];
                    totalUpdated += result[1];
                } catch (Exception e) {
                    log.error("Bulk save failed on page {}: {}", page, e.getMessage());
                    totalSkipped += productsToSync.size();
                }
            }
            totalSkipped += skipped;

            if (!idsPage.hasNext()) {
                log.info("Last page reached. Full sync stopping.");
                break;
            }

            page++;

            try {
                log.info("Waiting {} ms before fetching next page...", DELAY_BETWEEN_PAGES_MS);
                Thread.sleep(DELAY_BETWEEN_PAGES_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Product sync interrupted during page delay");
                break;
            }
        }

        log.info("CJ Dropshipping full sync completed: updated={}, created={}, skipped={}, total={}",
                totalUpdated, totalCreated, totalSkipped, totalCreated + totalUpdated);

        return ProductSyncResult.builder()
                .created(totalCreated)
                .updated(totalUpdated)
                .total(totalCreated + totalUpdated)
                .page(page)
                .hasMore(false)
                .build();
    }

    @Override
    public ProductSyncResult syncPageFromCjDropshipping(int page, int size) {
        // page is 1-based from the frontend, convert to 0-based for Spring Data
        int zeroBasedPage = page - 1;
        log.info("Syncing local products page {} (0-based={}, size={})...", page, zeroBasedPage, size);

        var idsPage = productRepository.findAllProductIds(zeroBasedPage, size);
        List<String> productIds = idsPage.getContent();

        if (productIds.isEmpty()) {
            log.info("No more local products on page {}. Sync finished.", page);
            return ProductSyncResult.builder()
                    .created(0).updated(0).total(0)
                    .page(page).hasMore(false)
                    .build();
        }

        log.info("Processing {} local products (page {}, parallel={})...",
                productIds.size(), page, PARALLEL_FETCH_THREADS);

        // Fetch en paralelo — máx PARALLEL_FETCH_THREADS requests concurrentes a CJ
        List<CompletableFuture<Optional<Product>>> futures = productIds.stream()
                .map(pid -> CompletableFuture.supplyAsync(() -> {
                    try {
                        CjProductDetailDto cjProduct = cjClient.getProductDetail(pid);
                        return Optional.of(cjProductDetailMapper.toProduct(cjProduct));
                    } catch (ExternalServiceException e) {
                        log.warn("CJ API error for pid={}: {}", pid, e.getMessage());
                        return Optional.<Product>empty();
                    } catch (Exception e) {
                        log.warn("Failed to fetch product pid={}: {}", pid, e.getMessage());
                        return Optional.<Product>empty();
                    }
                }, fetchExecutor))
                .toList();

        List<Product> productsToSync = futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        int skipped = productIds.size() - productsToSync.size();
        int created = 0;
        int updated = 0;

        // Bulk save/update all fetched products at once
        if (!productsToSync.isEmpty()) {
            try {
                int[] result = productRepository.bulkSyncProducts(productsToSync);
                created = result[0];
                updated = result[1];
            } catch (Exception e) {
                log.error("Bulk save failed on page {}: {}", page, e.getMessage());
                skipped += productsToSync.size();
            }
        }

        boolean hasMore = idsPage.hasNext();
        log.info("Page {} sync done: updated={}, created={}, skipped={}, hasMore={}",
                page, updated, created, skipped, hasMore);

        return ProductSyncResult.builder()
                .created(created)
                .updated(updated)
                .total(created + updated)
                .page(page)
                .hasMore(hasMore)
                .build();
    }
}
