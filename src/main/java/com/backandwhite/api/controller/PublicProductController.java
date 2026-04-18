package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.out.*;
import com.backandwhite.api.mapper.BrandApiMapper;
import com.backandwhite.api.mapper.CategoryApiMapper;
import com.backandwhite.api.mapper.ProductApiMapper;
import com.backandwhite.api.mapper.ProductDetailApiMapper;
import com.backandwhite.api.mapper.ReviewApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.service.PricingService;
import com.backandwhite.application.usecase.*;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.domain.model.*;
import com.backandwhite.domain.valueobject.BrandStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public catalog endpoints — no authentication required. All paths under
 * /api/v1/public/** are configured without JWT in the gateway.
 */
@NxPublic
@Tag(name = "Public Catalog", description = "Public catalog APIs for the storefront (no auth)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public")
public class PublicProductController {

    private final ProductUseCase productUseCase;
    private final ProductDetailUseCase productDetailUseCase;
    private final ProductSearchUseCase productSearchUseCase;
    private final CategoryUseCase categoryUseCase;
    private final BrandUseCase brandUseCase;
    private final ReviewUseCase reviewUseCase;
    private final InventoryUseCase inventoryUseCase;
    private final PricingService pricingService;
    private final ProductApiMapper productApiMapper;
    private final ProductDetailApiMapper productDetailApiMapper;
    private final CategoryApiMapper categoryApiMapper;
    private final BrandApiMapper brandApiMapper;
    private final ReviewApiMapper reviewApiMapper;

    // ── Products ─────────────────────────────────────────────────────────────

    @Operation(summary = "List published products with filters (public)")
    @GetMapping("/products")
    public ResponseEntity<PaginationDtoOut<ProductDtoOut>> listProducts(
            @RequestParam(defaultValue = "en") String locale, @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String name, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {
        Page<Product> products = productUseCase.findAllPaged(locale, categoryId, "PUBLISHED", name, page, size, sortBy,
                ascending);
        return ResponseEntity.ok(PageableUtils.toResponse(products.map(p -> {
            pricingService.applyMarginsToProduct(p);
            return productApiMapper.toDto(p);
        })));
    }

    @Operation(summary = "Get product detail by ID (public)")
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDtoOut> getProduct(@PathVariable String id,
            @RequestParam(defaultValue = "en") String locale) {
        Product product = productUseCase.findById(id, locale);
        pricingService.applyMarginsToProduct(product);
        return ResponseEntity.ok(productApiMapper.toDto(product));
    }

    @Operation(summary = "Get product variants (public)")
    @GetMapping("/products/{pid}/variants")
    public ResponseEntity<List<ProductDetailVariantDtoOut>> getProductVariants(@PathVariable String pid,
            @RequestParam(defaultValue = "en") String locale) {
        List<ProductDetailVariant> variants = productDetailUseCase.findVariantsByPid(pid, locale);
        return ResponseEntity.ok(variants.stream().map(productDetailApiMapper::toVariantDto).toList());
    }

    @Operation(summary = "Check stock availability for a variant (public)")
    @GetMapping("/products/variants/{vid}/stock")
    public ResponseEntity<StockDtoOut> checkStock(@PathVariable String vid) {
        int available = inventoryUseCase.getAvailableStock(vid);
        return ResponseEntity
                .ok(StockDtoOut.builder().variantId(vid).available(available).inStock(available > 0).build());
    }

    // ── Search ───────────────────────────────────────────────────────────────

    @Operation(summary = "Full-text product search powered by Elasticsearch (public)")
    @GetMapping("/search")
    public ResponseEntity<ProductSearchResponse> search(@RequestParam String q,
            @RequestParam(required = false) List<String> categoryId, @RequestParam(required = false) String brand,
            @RequestParam(required = false) Float minPrice, @RequestParam(required = false) Float maxPrice,
            @RequestParam(required = false) Boolean inStock, @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "24") int size) {
        ProductSearchResponse response = productSearchUseCase.search(q, categoryId, brand, minPrice, maxPrice, inStock,
                sortBy, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Autocomplete product suggestions (public)")
    @GetMapping("/search/autocomplete")
    public ResponseEntity<List<AutocompleteSuggestion>> autocomplete(@RequestParam String q,
            @RequestParam(defaultValue = "8") int limit) {
        List<AutocompleteSuggestion> suggestions = productSearchUseCase.autocomplete(q, limit);
        return ResponseEntity.ok(suggestions);
    }

    // ── Categories ───────────────────────────────────────────────────────────

    @Operation(summary = "List active categories (public)")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDtoOut>> listCategories(@RequestParam(defaultValue = "en") String locale,
            @RequestParam(required = false, defaultValue = "false") boolean featured) {
        List<Category> categories;
        if (featured) {
            categories = categoryUseCase.findFeatured(locale);
        } else {
            categories = categoryUseCase.findCategories(locale, null, true);
        }
        return ResponseEntity.ok(categories.stream().map(categoryApiMapper::toDto).toList());
    }

    @Operation(summary = "Get category by ID (public)")
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDtoOut> getCategory(@PathVariable String id,
            @RequestParam(defaultValue = "en") String locale) {
        Category category = categoryUseCase.findById(id, locale);
        return ResponseEntity.ok(categoryApiMapper.toDto(category));
    }

    // ── Brands ───────────────────────────────────────────────────────────────

    @Operation(summary = "List active brands (public)")
    @GetMapping("/brands")
    public ResponseEntity<PaginationDtoOut<BrandDtoOut>> listBrands(@RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        Page<Brand> brands = brandUseCase.findAll(BrandStatus.ACTIVE, name, page, size, "name", true);
        List<BrandDtoOut> content = brands.getContent().stream().map(brandApiMapper::toDto).toList();
        return ResponseEntity.ok(PaginationDtoOut.<BrandDtoOut>builder().content(content)
                .currentPage(brands.getNumber()).pageSize(brands.getSize()).totalElements(brands.getTotalElements())
                .totalPages(brands.getTotalPages()).hasNext(brands.hasNext()).hasPrevious(brands.hasPrevious())
                .build());
    }

    @Operation(summary = "Get products by brand slug (public)")
    @GetMapping("/brands/{slug}/products")
    public ResponseEntity<PaginationDtoOut<ProductDtoOut>> getProductsByBrand(@PathVariable String slug,
            @RequestParam(defaultValue = "en") String locale, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {
        brandUseCase.findBySlug(slug);
        Page<Product> products = productUseCase.findAllPaged(locale, null, "PUBLISHED", null, page, size, sortBy,
                ascending);
        return ResponseEntity.ok(PageableUtils.toResponse(products.map(p -> {
            pricingService.applyMarginsToProduct(p);
            return productApiMapper.toDto(p);
        })));
    }

    // ── Reviews ──────────────────────────────────────────────────────────────

    @Operation(summary = "Get reviews for a product (public)")
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<PaginationDtoOut<ReviewDtoOut>> getProductReviews(@PathVariable String productId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Review> reviews = reviewUseCase.findByProductId(productId, page, size, "createdAt", false);
        return ResponseEntity.ok(PageableUtils.toResponse(reviews.map(reviewApiMapper::toDto)));
    }

    @Operation(summary = "Get review stats for a product (public)")
    @GetMapping("/products/{productId}/reviews/stats")
    public ResponseEntity<ReviewStatsDtoOut> getReviewStats(@PathVariable String productId) {
        ReviewStats stats = reviewUseCase.getStatsByProductId(productId);
        return ResponseEntity.ok(reviewApiMapper.toStatsDto(stats));
    }
}
