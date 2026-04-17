package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.usecase.ProductSyncUseCase;
import com.backandwhite.domain.exception.ExternalServiceException;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductSyncResult;
import com.backandwhite.domain.repository.CategoryRepository;
import com.backandwhite.domain.repository.ProductRepository;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListPageDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductListV2ItemDto;
import com.backandwhite.infrastructure.client.cj.mapper.CjProductDetailMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductSyncUseCaseImpl implements ProductSyncUseCase {

    private static final int PAGE_SIZE = 100;
    private static final long DELAY_BETWEEN_PAGES_MS = 10_000;
    /** Maximum parallel requests to CJ to avoid saturating the rate limit */
    private static final int PARALLEL_FETCH_THREADS = 5;

    private final ExecutorService fetchExecutor = Executors.newFixedThreadPool(PARALLEL_FETCH_THREADS, r -> {
        Thread t = Thread.ofVirtual().unstarted(r);
        t.setDaemon(true);
        return t;
    });

    private final DropshippingPort cjClient;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CjProductDetailMapper cjProductDetailMapper;

    @Override
    public ProductSyncResult syncFromCjDropshipping(boolean forceOverwrite) {
        log.info("Starting CJ Dropshipping full product sync (forceOverwrite={})...", forceOverwrite);

        int totalCreated = 0;
        int totalUpdated = 0;
        int totalSkipped = 0;
        int page = 0;

        while (true) {
            Page<String> idsPage = productRepository.findAllProductIds(page, PAGE_SIZE);
            List<String> productIds = idsPage.getContent();

            if (productIds.isEmpty()) {
                log.info("No more local products on page {}. Full sync finished.", page);
                break;
            }

            log.info("Processing page {} with {} local products (parallel={})...", page, productIds.size(),
                    PARALLEL_FETCH_THREADS);

            // Parallel fetch — max PARALLEL_FETCH_THREADS concurrent requests to CJ
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
                    }, fetchExecutor)).toList();

            List<Product> productsToSync = futures.stream().map(CompletableFuture::join).filter(Optional::isPresent)
                    .map(Optional::get).toList();

            int skipped = productIds.size() - productsToSync.size();

            // Bulk save/update all fetched products at once
            if (!productsToSync.isEmpty()) {
                try {
                    int[] result = productRepository.bulkSyncProducts(productsToSync, forceOverwrite);
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

        log.info("CJ Dropshipping full sync completed: updated={}, created={}, skipped={}, total={}", totalUpdated,
                totalCreated, totalSkipped, totalCreated + totalUpdated);

        return ProductSyncResult.builder().created(totalCreated).updated(totalUpdated)
                .total(totalCreated + totalUpdated).page(page).hasMore(false).build();
    }

    @Override
    public ProductSyncResult syncPageFromCjDropshipping(int page, int size, boolean forceOverwrite,
            List<String> categoryIds) {
        // page is 1-based from the frontend, convert to 0-based for Spring Data
        int zeroBasedPage = page - 1;
        log.info("Syncing local products page {} (0-based={}, size={}, forceOverwrite={}, categoryIds={})...", page,
                zeroBasedPage, size, forceOverwrite, categoryIds);

        Page<String> idsPage = (categoryIds != null && !categoryIds.isEmpty())
                ? productRepository.findProductIdsByCategoryIds(categoryIds, zeroBasedPage, size)
                : productRepository.findAllProductIds(zeroBasedPage, size);
        List<String> productIds = idsPage.getContent();

        if (productIds.isEmpty()) {
            log.info("No more local products on page {}. Sync finished.", page);
            return ProductSyncResult.builder().created(0).updated(0).total(0).page(page).hasMore(false).build();
        }

        log.info("Processing {} local products (page {}, parallel={})...", productIds.size(), page,
                PARALLEL_FETCH_THREADS);

        // Parallel fetch — max PARALLEL_FETCH_THREADS concurrent requests to CJ
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
                }, fetchExecutor)).toList();

        List<Product> productsToSync = futures.stream().map(CompletableFuture::join).filter(Optional::isPresent)
                .map(Optional::get).toList();

        int skipped = productIds.size() - productsToSync.size();
        int created = 0;
        int updated = 0;

        // Bulk save/update all fetched products at once
        if (!productsToSync.isEmpty()) {
            try {
                int[] result = productRepository.bulkSyncProducts(productsToSync, forceOverwrite);
                created = result[0];
                updated = result[1];
            } catch (Exception e) {
                log.error("Bulk save failed on page {}: {}", page, e.getMessage());
                skipped += productsToSync.size();
            }
        }

        boolean hasMore = idsPage.hasNext();
        log.info("Page {} sync done: updated={}, created={}, skipped={}, hasMore={}", page, updated, created, skipped,
                hasMore);

        return ProductSyncResult.builder().created(created).updated(updated).total(created + updated).page(page)
                .hasMore(hasMore).build();
    }

    // ── Discover new products by category ────────────────────────────────────

    /** Max CJ pages to crawl per category (100 products each) */
    private static final int MAX_CJ_PAGES_PER_CATEGORY = 10;

    @Override
    public ProductSyncResult discoverNewProductsByCategory(int categoryOffset) {
        List<String> l3Ids = categoryRepository.findAllLevel3Ids();
        Collections.sort(l3Ids); // stable order for offset-based paging

        int totalCategories = l3Ids.size();

        if (categoryOffset >= totalCategories) {
            log.info("Discover: offset {} >= totalCategories {}. Nothing to do.", categoryOffset, totalCategories);
            return ProductSyncResult.builder().created(0).updated(0).total(0).page(categoryOffset).hasMore(false)
                    .totalCategories(totalCategories).build();
        }

        String categoryId = l3Ids.get(categoryOffset);
        boolean hasMore = categoryOffset + 1 < totalCategories;

        log.info("Discover: processing category {} ({}/{}) ...", categoryId, categoryOffset + 1, totalCategories);

        int created = 0;
        int updated = 0;

        try {
            // Iterate CJ pages for this category (capped)
            for (int cjPage = 1; cjPage <= MAX_CJ_PAGES_PER_CATEGORY; cjPage++) {
                CjProductListPageDto pageResult = cjClient.getProductListFiltered(cjPage, PAGE_SIZE, categoryId, null,
                        null, null, 3, "desc");

                List<CjProductListV2ItemDto> products = (pageResult != null) ? pageResult.getAllProducts() : List.of();

                if (products.isEmpty()) {
                    log.info("Discover: CJ page {} returned EMPTY for category {} (totalRecords={})", cjPage,
                            categoryId, pageResult != null ? pageResult.getTotalRecords() : "null");
                    break;
                }

                // Extract product IDs and filter out those already in our DB
                // listV2 uses "id" field, not "pid"
                List<String> newPids = products.stream().map(CjProductListV2ItemDto::getId)
                        .filter(id -> id != null && !id.isBlank()).filter(id -> !productRepository.existsById(id))
                        .toList();

                log.info("Discover: CJ page {} returned {} products, {} are new", cjPage, products.size(),
                        newPids.size());

                if (!newPids.isEmpty()) {
                    int[] result = fetchDetailAndSave(newPids);
                    created += result[0];
                    updated += result[1];
                }

                // Check if we've exhausted CJ results for this category
                int totalCjProducts = pageResult.getTotalRecords() != null ? pageResult.getTotalRecords() : 0;
                if (cjPage * PAGE_SIZE >= totalCjProducts) {
                    break;
                }
            }
        } catch (ExternalServiceException e) {
            log.warn("Discover: CJ API error for category {}: {}", categoryId, e.getMessage());
        } catch (Exception e) {
            log.error("Discover: unexpected error for category {}: {}", categoryId, e.getMessage(), e);
        }

        categoryRepository.updateLastDiscoveredAt(categoryId);
        log.info("Discover: category {} done — created={}, updated={}", categoryId, created, updated);

        return ProductSyncResult.builder().created(created).updated(updated).total(created + updated)
                .page(categoryOffset).hasMore(hasMore).totalCategories(totalCategories).build();
    }

    /** Delay between sequential CJ detail API calls (ms) */
    private static final long DETAIL_CALL_DELAY_MS = 600;
    /** Extra backoff when rate-limited (ms) */
    private static final long RATE_LIMIT_BACKOFF_MS = 3_000;
    /** Max retries per product on rate limit */
    private static final int MAX_RATE_LIMIT_RETRIES = 2;

    /**
     * Fetches full product detail from CJ sequentially with throttling to respect
     * rate limits. Products are saved in batches of 10 for efficiency.
     *
     * @return int[]{created, updated}
     */
    private int[] fetchDetailAndSave(List<String> pids) {
        int created = 0, updated = 0;
        List<Product> buffer = new ArrayList<>();

        for (int i = 0; i < pids.size(); i++) {
            String pid = pids.get(i);
            Optional<Product> product = fetchDetailWithRetry(pid);
            product.ifPresent(buffer::add);

            // Flush buffer every 10 products or at the end
            if (buffer.size() >= 10 || i == pids.size() - 1) {
                if (!buffer.isEmpty()) {
                    try {
                        int[] result = productRepository.bulkSyncProducts(buffer, true);
                        created += result[0];
                        updated += result[1];
                    } catch (Exception e) {
                        log.error("Bulk save failed during discover: {}", e.getMessage());
                    }
                    buffer = new ArrayList<>();
                }
            }

            // Throttle between calls
            if (i < pids.size() - 1) {
                try {
                    Thread.sleep(DETAIL_CALL_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return new int[]{created, updated};
    }

    private Optional<Product> fetchDetailWithRetry(String pid) {
        for (int attempt = 0; attempt <= MAX_RATE_LIMIT_RETRIES; attempt++) {
            try {
                CjProductDetailDto cjProduct = cjClient.getProductDetail(pid);
                return Optional.of(cjProductDetailMapper.toProduct(cjProduct));
            } catch (ExternalServiceException e) {
                if (e.getMessage() != null && e.getMessage().contains("Too many requests")
                        && attempt < MAX_RATE_LIMIT_RETRIES) {
                    log.info("Rate limited on pid={}, backing off {}ms (attempt {}/{})", pid, RATE_LIMIT_BACKOFF_MS,
                            attempt + 1, MAX_RATE_LIMIT_RETRIES);
                    try {
                        Thread.sleep(RATE_LIMIT_BACKOFF_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return Optional.empty();
                    }
                } else {
                    log.warn("CJ API error for pid={}: {}", pid, e.getMessage());
                    return Optional.empty();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch product pid={}: {}", pid, e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
