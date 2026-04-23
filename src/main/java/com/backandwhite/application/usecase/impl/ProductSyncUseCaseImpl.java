package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.port.out.DropshippingPort;
import com.backandwhite.application.usecase.CjInventorySyncUseCase;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ProductSyncUseCaseImpl implements ProductSyncUseCase {

    private static final int PAGE_SIZE = 100;
    private static final long DELAY_BETWEEN_PAGES_MS = 10_000;
    // Capped at 2 to match the CJ Plus plan's 2 req/s budget. The RateLimiter
    // on the client would block extra threads anyway, but lower parallelism
    // keeps timeout-duration headroom on the RateLimiter during long syncs.
    private static final int PARALLEL_FETCH_THREADS = 2;
    private static final int MAX_CJ_PAGES_PER_CATEGORY = 10;
    private static final long DETAIL_CALL_DELAY_MS = 600;
    private static final long RATE_LIMIT_BACKOFF_MS = 3_000;
    private static final int MAX_RATE_LIMIT_RETRIES = 2;
    private static final int BUFFER_FLUSH_SIZE = 10;

    private static final String LOG_CJ_API_ERROR = "CJ API error for pid={}: {}";
    private static final String LOG_FETCH_FAILED = "Failed to fetch product pid={}: {}";

    private final ExecutorService fetchExecutor = Executors.newFixedThreadPool(PARALLEL_FETCH_THREADS,
            r -> Thread.ofVirtual().unstarted(r));

    private final DropshippingPort cjClient;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CjProductDetailMapper cjProductDetailMapper;
    /**
     * Injected @Lazy to match the pattern in CjProductFullSyncUseCaseImpl — both
     * sync use cases share the inventory graph and Spring would otherwise flag a
     * potential cycle at wiring time.
     */
    private final CjInventorySyncUseCase cjInventorySyncUseCase;

    public ProductSyncUseCaseImpl(DropshippingPort cjClient, ProductRepository productRepository,
            CategoryRepository categoryRepository, CjProductDetailMapper cjProductDetailMapper,
            @Lazy CjInventorySyncUseCase cjInventorySyncUseCase) {
        this.cjClient = cjClient;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cjProductDetailMapper = cjProductDetailMapper;
        this.cjInventorySyncUseCase = cjInventorySyncUseCase;
    }

    @Override
    public ProductSyncResult syncFromCjDropshipping(boolean forceOverwrite) {
        log.info("Starting CJ Dropshipping full product sync (forceOverwrite={})...", forceOverwrite);

        int totalCreated = 0;
        int totalUpdated = 0;
        int totalSkipped = 0;
        int page = 0;
        boolean keepGoing = true;

        while (keepGoing) {
            Page<String> idsPage = productRepository.findAllProductIds(page, PAGE_SIZE);
            List<String> productIds = idsPage.getContent();

            if (productIds.isEmpty()) {
                log.info("No more local products on page {}. Full sync finished.", page);
                keepGoing = false;
            } else {
                PageSyncOutcome outcome = syncLocalPage(page, productIds, forceOverwrite);
                totalCreated += outcome.created();
                totalUpdated += outcome.updated();
                totalSkipped += outcome.skipped();

                if (!idsPage.hasNext()) {
                    log.info("Last page reached. Full sync stopping.");
                    keepGoing = false;
                } else {
                    page++;
                    keepGoing = delayBetweenPages();
                }
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

        PageSyncOutcome outcome = syncLocalPage(page, productIds, forceOverwrite);
        boolean hasMore = idsPage.hasNext();
        log.info("Page {} sync done: updated={}, created={}, skipped={}, hasMore={}", page, outcome.updated(),
                outcome.created(), outcome.skipped(), hasMore);

        return ProductSyncResult.builder().created(outcome.created()).updated(outcome.updated())
                .total(outcome.created() + outcome.updated()).page(page).hasMore(hasMore).build();
    }

    private PageSyncOutcome syncLocalPage(int page, List<String> productIds, boolean forceOverwrite) {
        log.info("Processing page {} with {} local products (parallel={})...", page, productIds.size(),
                PARALLEL_FETCH_THREADS);

        List<Product> productsToSync = fetchProductsInParallel(productIds);
        int skipped = productIds.size() - productsToSync.size();
        int created = 0;
        int updated = 0;

        if (!productsToSync.isEmpty()) {
            try {
                int[] result = productRepository.bulkSyncProducts(productsToSync, forceOverwrite);
                created = result[0];
                updated = result[1];
                chainInventorySync(productsToSync);
            } catch (Exception e) {
                log.error("Bulk save failed on page {}: {}", page, e.getMessage(), e);
                skipped += productsToSync.size();
            }
        }
        return new PageSyncOutcome(created, updated, skipped);
    }

    /**
     * Trigger inventory sync for every product that was persisted. CJ's
     * /product/query payload ships no inventory, so without this follow-up the
     * catalog lands with warehouseInventoryNum = 0 + empty
     * product_detail_variant_inventories and the storefront paints everything as
     * "Out of stock" until the 4h scheduler ticks. Failures are logged and
     * swallowed so a CJ rate-limit on a single pid never invalidates the whole page
     * of product saves.
     */
    private void chainInventorySync(List<Product> products) {
        for (Product p : products) {
            if (p.getId() == null) {
                continue;
            }
            try {
                cjInventorySyncUseCase.syncByPid(p.getId());
            } catch (RuntimeException e) {
                log.warn("::> Chained inventory sync failed for pid={} — scheduler will retry. reason={}", p.getId(),
                        e.getMessage());
            }
        }
    }

    private boolean delayBetweenPages() {
        try {
            log.info("Waiting {} ms before fetching next page...", DELAY_BETWEEN_PAGES_MS);
            Thread.sleep(DELAY_BETWEEN_PAGES_MS);
            return true;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            log.warn("Product sync interrupted during page delay");
            return false;
        }
    }

    @Override
    public ProductSyncResult discoverNewProductsByCategory(int categoryOffset) {
        List<String> l3Ids = categoryRepository.findAllLevel3Ids();
        Collections.sort(l3Ids);

        int totalCategories = l3Ids.size();

        if (categoryOffset >= totalCategories) {
            log.info("Discover: offset {} >= totalCategories {}. Nothing to do.", categoryOffset, totalCategories);
            return ProductSyncResult.builder().created(0).updated(0).total(0).page(categoryOffset).hasMore(false)
                    .totalCategories(totalCategories).build();
        }

        String categoryId = l3Ids.get(categoryOffset);
        boolean hasMore = categoryOffset + 1 < totalCategories;

        log.info("Discover: processing category {} ({}/{}) ...", categoryId, categoryOffset + 1, totalCategories);

        int[] totals = discoverCategoryProducts(categoryId);

        categoryRepository.updateLastDiscoveredAt(categoryId);
        log.info("Discover: category {} done — created={}, updated={}", categoryId, totals[0], totals[1]);

        return ProductSyncResult.builder().created(totals[0]).updated(totals[1]).total(totals[0] + totals[1])
                .page(categoryOffset).hasMore(hasMore).totalCategories(totalCategories).build();
    }

    private int[] discoverCategoryProducts(String categoryId) {
        int created = 0;
        int updated = 0;
        try {
            int cjPage = 1;
            DiscoverPageResult result = crawlDiscoverPage(categoryId, cjPage);
            boolean keepGoing = result != null;
            while (keepGoing && cjPage <= MAX_CJ_PAGES_PER_CATEGORY) {
                created += result.created();
                updated += result.updated();
                keepGoing = !result.last();
                cjPage++;
                if (keepGoing) {
                    result = crawlDiscoverPage(categoryId, cjPage);
                    keepGoing = result != null;
                }
            }
        } catch (ExternalServiceException e) {
            log.warn("Discover: CJ API error for category {}: {}", categoryId, e.getMessage());
        } catch (Exception e) {
            log.error("Discover: unexpected error for category {}: {}", categoryId, e.getMessage(), e);
        }
        return new int[]{created, updated};
    }

    /**
     * Crawls one CJ page for a discover-by-category run. Returns {@code null} when
     * the page is empty (caller stops immediately); otherwise the result carries
     * the page deltas plus a {@code last} flag that tells the caller whether to
     * request more pages.
     */
    private DiscoverPageResult crawlDiscoverPage(String categoryId, int cjPage) {
        CjProductListPageDto pageResult = cjClient.getProductListFiltered(cjPage, PAGE_SIZE, categoryId, null, null,
                null, 3, "desc");
        List<CjProductListV2ItemDto> products = (pageResult != null) ? pageResult.getAllProducts() : List.of();
        if (products.isEmpty()) {
            log.info("Discover: CJ page {} returned EMPTY for category {} (totalRecords={})", cjPage, categoryId,
                    pageResult != null ? pageResult.getTotalRecords() : "null");
            return null;
        }
        int[] pageTotals = processDiscoverPage(products, cjPage);
        int totalCjProducts = pageResult.getTotalRecords() != null ? pageResult.getTotalRecords() : 0;
        boolean last = cjPage * PAGE_SIZE >= totalCjProducts;
        return new DiscoverPageResult(pageTotals[0], pageTotals[1], last);
    }

    private record DiscoverPageResult(int created, int updated, boolean last) {
    }

    private int[] processDiscoverPage(List<CjProductListV2ItemDto> products, int cjPage) {
        List<String> newPids = products.stream().map(CjProductListV2ItemDto::getId)
                .filter(id -> id != null && !id.isBlank()).filter(id -> !productRepository.existsById(id)).toList();

        log.info("Discover: CJ page {} returned {} products, {} are new", cjPage, products.size(), newPids.size());

        if (newPids.isEmpty()) {
            return new int[]{0, 0};
        }
        return fetchDetailAndSave(newPids);
    }

    private int[] fetchDetailAndSave(List<String> pids) {
        int created = 0;
        int updated = 0;
        List<Product> buffer = new ArrayList<>();

        for (int i = 0; i < pids.size(); i++) {
            fetchDetailWithRetry(pids.get(i)).ifPresent(buffer::add);

            if (shouldFlushBuffer(buffer, i, pids.size())) {
                int[] result = flushBuffer(buffer);
                created += result[0];
                updated += result[1];
                buffer = new ArrayList<>();
            }

            if (i < pids.size() - 1 && !throttle()) {
                break;
            }
        }

        return new int[]{created, updated};
    }

    private boolean shouldFlushBuffer(List<Product> buffer, int index, int total) {
        return !buffer.isEmpty() && (buffer.size() >= BUFFER_FLUSH_SIZE || index == total - 1);
    }

    private int[] flushBuffer(List<Product> buffer) {
        try {
            int[] result = productRepository.bulkSyncProducts(buffer, true);
            chainInventorySync(buffer);
            return new int[]{result[0], result[1]};
        } catch (Exception e) {
            log.error("Bulk save failed during discover: {}", e.getMessage(), e);
            return new int[]{0, 0};
        }
    }

    private boolean throttle() {
        try {
            Thread.sleep(DETAIL_CALL_DELAY_MS);
            return true;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private List<Product> fetchProductsInParallel(List<String> productIds) {
        List<CompletableFuture<Optional<Product>>> futures = productIds.stream()
                .map(pid -> CompletableFuture.supplyAsync(() -> fetchOneForParallel(pid), fetchExecutor)).toList();

        return futures.stream().map(CompletableFuture::join).filter(Optional::isPresent).map(Optional::get).toList();
    }

    private Optional<Product> fetchOneForParallel(String pid) {
        try {
            CjProductDetailDto cjProduct = cjClient.getProductDetail(pid);
            return Optional.of(cjProductDetailMapper.toProduct(cjProduct));
        } catch (ExternalServiceException e) {
            log.warn(LOG_CJ_API_ERROR, pid, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn(LOG_FETCH_FAILED, pid, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Product> fetchDetailWithRetry(String pid) {
        for (int attempt = 0; attempt <= MAX_RATE_LIMIT_RETRIES; attempt++) {
            try {
                CjProductDetailDto cjProduct = cjClient.getProductDetail(pid);
                return Optional.of(cjProductDetailMapper.toProduct(cjProduct));
            } catch (ExternalServiceException e) {
                RateLimitOutcome outcome = handleRateLimit(pid, e, attempt);
                if (outcome == RateLimitOutcome.STOP) {
                    return Optional.empty();
                }
                // outcome == RETRY → continue the loop for the next attempt
            } catch (Exception e) {
                log.warn(LOG_FETCH_FAILED, pid, e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Signals whether the retry loop should keep trying or stop.
     */
    private enum RateLimitOutcome {
        RETRY, STOP
    }

    private RateLimitOutcome handleRateLimit(String pid, ExternalServiceException e, int attempt) {
        boolean rateLimited = e.getMessage() != null && e.getMessage().contains("Too many requests")
                && attempt < MAX_RATE_LIMIT_RETRIES;
        if (!rateLimited) {
            log.warn(LOG_CJ_API_ERROR, pid, e.getMessage());
            return RateLimitOutcome.STOP;
        }
        log.info("Rate limited on pid={}, backing off {}ms (attempt {}/{})", pid, RATE_LIMIT_BACKOFF_MS, attempt + 1,
                MAX_RATE_LIMIT_RETRIES);
        try {
            Thread.sleep(RATE_LIMIT_BACKOFF_MS);
            return RateLimitOutcome.RETRY;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return RateLimitOutcome.STOP;
        }
    }

    private record PageSyncOutcome(int created, int updated, int skipped) {
    }
}
