package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PageFilterRequest;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BulkProductDtoIn;
import com.backandwhite.api.dto.in.BulkStatusUpdateDtoIn;
import com.backandwhite.api.dto.in.BulkVariantDtoIn;
import com.backandwhite.api.dto.in.ProductDetailVariantDtoIn;
import com.backandwhite.api.dto.in.ProductDtoIn;
import com.backandwhite.api.dto.in.ProductFilterDto;
import com.backandwhite.api.dto.in.ProductLinkDtoIn;
import com.backandwhite.api.dto.in.VariantFilterDto;
import com.backandwhite.api.dto.out.BulkImportResultDtoOut;
import com.backandwhite.api.dto.out.ProductDetailDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantDtoOut;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.dto.out.ProductSyncResultDtoOut;
import com.backandwhite.api.mapper.ProductApiMapper;
import com.backandwhite.api.mapper.ProductDetailApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.service.PricingService;
import com.backandwhite.application.usecase.ProductDetailUseCase;
import com.backandwhite.application.usecase.ProductSyncUseCase;
import com.backandwhite.application.usecase.ProductUseCase;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductSyncResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Endpoints for product management")
public class ProductController {

    private final ProductUseCase productUseCase;
    private final ProductDetailUseCase productDetailUseCase;
    private final ProductSyncUseCase productSyncUseCase;
    private final ProductApiMapper productApiMapper;
    private final ProductDetailApiMapper productDetailApiMapper;
    private final PricingService pricingService;

    @NxPublic
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List products by category", description = "Returns all products of a category with their translations and variants")
    public ResponseEntity<List<ProductDtoOut>> findByCategoryId(
            @Parameter(description = "Category ID") @PathVariable String categoryId,
            @Parameter(description = "Language code (e.g. es, en, pt-BR)", example = "es") @RequestParam(defaultValue = "en") String locale,
            @Parameter(description = "Filter by status (DRAFT, PUBLISHED). If not provided, shows all.") @RequestParam(required = false) String status) {

        List<Product> products = productUseCase.findByCategoryId(categoryId, locale, status);
        products.forEach(pricingService::applyMarginsToProduct);
        List<ProductDtoOut> result = productApiMapper.toDtoList(products);
        return ResponseEntity.ok(result);
    }

    @NxPublic
    @GetMapping
    @Operation(summary = "List paginated products", description = "Returns all products paginated, filtering by locale. Optionally filters by category.")
    public ResponseEntity<PaginationDtoOut<ProductDtoOut>> findAllPaged(
            @Parameter(description = "Language code (e.g. es, en, pt-BR)", example = "es") @RequestParam(defaultValue = "en") String locale,
            @Parameter(description = "Category ID (optional, if not provided lists all)") @RequestParam(required = false) String categoryId,
            @Parameter(description = "Filter by status (DRAFT, PUBLISHED). If not provided, shows all.") @RequestParam(required = false) String status,
            @Parameter(description = "Search by product name (partial match, case-insensitive)") @RequestParam(required = false) String name,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Ascending order", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {

        Pageable pageable = PageableUtils.toPageable(page, size, sortBy, ascending);
        Page<Product> pagedResult = productUseCase.findAllPaged(locale, categoryId, status, name,
                pageable.getPageNumber(), pageable.getPageSize(), sortBy, ascending);

        return ResponseEntity.ok(PageableUtils.toResponse(pagedResult.map(p -> {
            pricingService.applyMarginsToProduct(p);
            return productApiMapper.toDto(p);
        })));
    }

    @NxPublic
    @PostMapping("/search")
    @Operation(summary = "Paginated product search with dynamic filters", description = """
            Paginated product listing with dynamic filters via reflection.
            Only non-null fields from the `filters` object are applied as predicates.

            Example body:
            ```json
            {
              "page": 0, "size": 20, "sortBy": "createdAt", "ascending": true,
              "locale": "es",
              "filters": { "status": "PUBLISHED", "categoryId": "abc123" }
            }
            ```
            """)
    public ResponseEntity<PaginationDtoOut<ProductDtoOut>> search(
            @Valid @RequestBody PageFilterRequest<ProductFilterDto> request) {

        Pageable pageable = PageableUtils.toPageable(request);
        Page<Product> result = productUseCase.findAllPaged(request.getLocale(),
                request.getFilters() != null ? request.getFilters().getCategoryId() : null,
                request.getFilters() != null && request.getFilters().getStatus() != null
                        ? request.getFilters().getStatus().name()
                        : null,
                null, pageable.getPageNumber(), pageable.getPageSize(), request.getSortBy(), request.isAscending());

        return ResponseEntity.ok(PageableUtils.toResponse(result.map(p -> {
            pricingService.applyMarginsToProduct(p);
            return productApiMapper.toDto(p);
        })));
    }

    @NxPublic
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns a product with all its translations and variants")
    public ResponseEntity<ProductDtoOut> getById(@Parameter(description = "Product ID") @PathVariable String id,
            @Parameter(description = "Language code", example = "es") @RequestParam(defaultValue = "en") String locale) {

        Product product = productUseCase.findById(id, locale);
        pricingService.applyMarginsToProduct(product);
        return ResponseEntity.ok(productApiMapper.toDto(product));
    }

    @NxAdmin
    @PostMapping
    @Operation(summary = "Create product", description = "Creates a new product with its translations and variants")
    public ResponseEntity<ProductDtoOut> create(@Valid @RequestBody ProductDtoIn dto) {

        Product product = productApiMapper.toDomain(dto);
        Product created = productUseCase.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productApiMapper.toDto(created));
    }

    @NxAdmin
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product's data, including translations and variants")
    public ResponseEntity<ProductDtoOut> update(@Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody ProductDtoIn dto) {

        Product product = productApiMapper.toDomain(dto);
        Product updated = productUseCase.update(id, product);
        return ResponseEntity.ok(productApiMapper.toDto(updated));
    }

    @NxAdmin
    @DeleteMapping
@Operation(summary = "Delete products", description = "Deletes one or more products and all their translations and variants")
    public ResponseEntity<Void> deleteAll(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of product IDs to delete") @RequestBody List<String> ids) {
        productUseCase.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish/unpublish product", description = "Toggles a product's status between DRAFT and PUBLISHED")
    public ResponseEntity<Void> publishProduct(@Parameter(description = "Product ID") @PathVariable String id) {
        productUseCase.publishProduct(id);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/bulk-status")
    @Operation(summary = "Bulk status update", description = "Changes the status of multiple products to DRAFT or PUBLISHED")
    public ResponseEntity<Void> bulkUpdateStatus(@Valid @RequestBody BulkStatusUpdateDtoIn body) {
        productUseCase.bulkUpdateStatus(body.getIds(), body.getStatus());
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/{id}/brand")
    @Operation(summary = "Link brand", description = "Associates a brand with the product. Pass {\"id\":null} to detach.")
    public ResponseEntity<Void> linkBrand(@Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody ProductLinkDtoIn body) {
        productUseCase.linkBrand(id, body.getId());
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/{id}/warranty")
    @Operation(summary = "Link warranty", description = "Associates a warranty with the product. Pass {\"id\":null} to detach.")
    public ResponseEntity<Void> linkWarranty(@Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody ProductLinkDtoIn body) {
        productUseCase.linkWarranty(id, body.getId());
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/{id}/category")
    @Operation(summary = "Move product to another category", description = "Changes the product's category.")
    public ResponseEntity<Void> linkCategory(@Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody ProductLinkDtoIn body) {
        productUseCase.linkCategory(id, body.getId());
        return ResponseEntity.noContent().build();
    }

    @NxPublic
    @GetMapping("/detail/{pid}")
    @Operation(summary = "Product detail (CJ)", description = "Gets the complete detail of a product. If it doesn't exist in the local DB, fetches it from CJ Dropshipping, persists it and returns it from the DB.")
    public ResponseEntity<ProductDetailDtoOut> getProductDetail(
            @Parameter(description = "CJ Product ID (pid)") @PathVariable String pid,
            @Parameter(description = "Language code", example = "en") @RequestParam(defaultValue = "en") String locale) {

        ProductDetail detail = productDetailUseCase.getOrFetchFromCj(pid, locale);
        pricingService.applyMarginsToProductDetail(detail);
        // Pull the warranty plan from the sibling `products` row so the
        // storefront can show it without a second round-trip.
        try {
            Product sibling = productUseCase.findById(pid, locale);
            if (sibling != null) {
                detail.setWarrantyId(sibling.getWarrantyId());
            }
        } catch (RuntimeException ignored) {
            // product row may not exist yet for CJ-only imports — leave warrantyId null
        }
        return ResponseEntity.ok(productDetailApiMapper.toDto(detail));
    }

    // ── Variant CRUD ─────────────────────────────────────────────────────────

    @NxPublic
    @GetMapping("/detail/variants")
    @Operation(summary = "List all variants (paginated)", description = "Returns all variants of all products paginated. Supports search, status filter, PID filter and sorting.")
    public ResponseEntity<PaginationDtoOut<ProductDetailVariantDtoOut>> findAllVariantsPaged(
            @Parameter(description = "Language code (es, en, pt-BR)", example = "en") @RequestParam(defaultValue = "en") String locale,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Search text (name, SKU, VID, PID)") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status (DRAFT / PUBLISHED)") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by parent product PID") @RequestParam(required = false) String pid,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Ascending order") @RequestParam(defaultValue = "false") boolean ascending) {

        Pageable pageable = PageableUtils.toPageable(page, size, sortBy, ascending);
        Page<ProductDetailVariant> pagedResult = productDetailUseCase.findAllVariantsPaged(pageable.getPageNumber(),
                pageable.getPageSize(), locale, search, status, pid, sortBy, ascending);

        return ResponseEntity.ok(PageableUtils.toResponse(pagedResult.map(productDetailApiMapper::toVariantDto)));
    }

    @NxPublic
    @PostMapping("/detail/variants/search")
    @Operation(summary = "Paginated variant search with dynamic filters", description = """
            Paginated variant listing with dynamic filters.

            Example body:
            ```json
            {
              "page": 0, "size": 20, "sortBy": "createdAt", "ascending": false,
              "filters": { "status": "PUBLISHED", "pid": "PROD-001" }
            }
            ```
            """)
    public ResponseEntity<PaginationDtoOut<ProductDetailVariantDtoOut>> searchVariants(
            @Valid @RequestBody PageFilterRequest<VariantFilterDto> request) {

        Pageable pageable = PageableUtils.toPageable(request);
        String localeReq = request.getLocale() != null ? request.getLocale() : "en";
        Page<ProductDetailVariant> result = productDetailUseCase.findAllVariantsPaged(pageable.getPageNumber(),
                pageable.getPageSize(), localeReq,
                request.getFilters() != null ? request.getFilters().getSearch() : null,
                request.getFilters() != null && request.getFilters().getStatus() != null
                        ? request.getFilters().getStatus().name()
                        : null,
                request.getFilters() != null ? request.getFilters().getPid() : null, request.getSortBy(),
                request.isAscending());

        return ResponseEntity.ok(PageableUtils.toResponse(result.map(productDetailApiMapper::toVariantDto)));
    }

    @NxPublic
    @GetMapping("/detail/{pid}/variants")
    @Operation(summary = "List variants of a product", description = "Returns all variants of a product with their translations and inventories")
    public ResponseEntity<List<ProductDetailVariantDtoOut>> findVariantsByPid(
            @Parameter(description = "CJ Product ID (pid)") @PathVariable String pid,
            @Parameter(description = "Language code (es, en, pt-BR)", example = "en") @RequestParam(defaultValue = "en") String locale) {

        List<ProductDetailVariant> variants = productDetailUseCase.findVariantsByPid(pid, locale);
        return ResponseEntity.ok(productDetailApiMapper.toVariantDtoList(variants));
    }

    @NxPublic
    @GetMapping("/detail/variants/{vid}")
    @Operation(summary = "Get variant by VID", description = "Returns a specific variant with its translations and inventories")
    public ResponseEntity<ProductDetailVariantDtoOut> findVariantByVid(
            @Parameter(description = "Variant ID (vid)") @PathVariable String vid,
            @Parameter(description = "Language code (es, en, pt-BR)", example = "en") @RequestParam(defaultValue = "en") String locale) {

        ProductDetailVariant variant = productDetailUseCase.findVariantByVid(vid, locale);
        return ResponseEntity.ok(productDetailApiMapper.toVariantDto(variant));
    }

    @NxAdmin
    @PostMapping("/detail/variants")
    @Operation(summary = "Create variant", description = "Manually creates a new variant for an existing product")
    public ResponseEntity<ProductDetailVariantDtoOut> createVariant(@Valid @RequestBody ProductDetailVariantDtoIn dto) {

        ProductDetailVariant variant = productDetailApiMapper.toVariantDomain(dto);
        ProductDetailVariant created = productDetailUseCase.createVariant(variant);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDetailApiMapper.toVariantDto(created));
    }

    @NxAdmin
    @PutMapping("/detail/variants/{vid}")
    @Operation(summary = "Update variant", description = "Updates an existing variant's data, including translations and inventories")
    public ResponseEntity<ProductDetailVariantDtoOut> updateVariant(
            @Parameter(description = "Variant ID (vid)") @PathVariable String vid,
            @Valid @RequestBody ProductDetailVariantDtoIn dto) {

        ProductDetailVariant variant = productDetailApiMapper.toVariantDomain(dto);
        ProductDetailVariant updated = productDetailUseCase.updateVariant(vid, variant);
        return ResponseEntity.ok(productDetailApiMapper.toVariantDto(updated));
    }

    @NxAdmin
    @DeleteMapping("/detail/variants/{vid}")
    @Operation(summary = "Delete variant", description = "Deletes a variant and all its translations and inventories")
    public ResponseEntity<Void> deleteVariant(@Parameter(description = "Variant ID (vid)") @PathVariable String vid) {
        productDetailUseCase.deleteVariant(vid);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @DeleteMapping("/detail/variants")
    @Operation(summary = "Bulk delete variants", description = "Deletes multiple variants and their translations and inventories")
    public ResponseEntity<Void> deleteVariants(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of VIDs to delete") @RequestBody List<String> vids) {
        productDetailUseCase.deleteVariants(vids);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/detail/variants/{vid}/publish")
    @Operation(summary = "Publish/unpublish variant", description = "Toggles a variant's status between DRAFT and PUBLISHED")
    public ResponseEntity<Void> publishVariant(@Parameter(description = "Variant ID (vid)") @PathVariable String vid) {
        productDetailUseCase.publishVariant(vid);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/detail/variants/bulk-status")
    @Operation(summary = "Bulk update variant status", description = "Changes the status of multiple variants to DRAFT or PUBLISHED")
    public ResponseEntity<Void> bulkUpdateVariantStatus(@Valid @RequestBody BulkStatusUpdateDtoIn body) {
        productDetailUseCase.bulkUpdateVariantStatus(body.getIds(), body.getStatus());
        return ResponseEntity.noContent().build();
    }

    // ── Bulk operations ──────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping("/bulk")
    @Operation(summary = "Bulk product upload", description = "Creates multiple products in bulk. Individual errors do not abort the batch.")
    public ResponseEntity<BulkImportResultDtoOut> bulkCreateProducts(@Valid @RequestBody BulkProductDtoIn dto) {

        List<Product> products = dto.getRows().stream().map(productApiMapper::toDomain).toList();

        BulkImportResult result = productUseCase.bulkCreate(products);

        return toBulkImportResponse(result);
    }

    @NxAdmin
    @PostMapping("/detail/variants/bulk")
    @Operation(summary = "Bulk variant upload", description = "Creates multiple variants in bulk. Individual errors do not abort the batch.")
    public ResponseEntity<BulkImportResultDtoOut> bulkCreateVariants(@Valid @RequestBody BulkVariantDtoIn dto) {

        List<ProductDetailVariant> variants = dto.getRows().stream().map(productDetailApiMapper::toVariantDomain)
                .toList();

        BulkImportResult result = productDetailUseCase.bulkCreateVariants(variants);

        return toBulkImportResponse(result);
    }

    private ResponseEntity<BulkImportResultDtoOut> toBulkImportResponse(BulkImportResult result) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BulkImportResultDtoOut.builder()
                .created(result.getCreated()).failed(result.getFailed()).totalRows(result.getTotalRows())
                .errors(result.getErrors().stream().map(
                        e -> BulkImportResultDtoOut.RowError.builder().row(e.getRow()).message(e.getMessage()).build())
                        .toList())
                .build());
    }

    // ── Sync ──────────────────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping("/sync")
    @Operation(summary = "Sync all products", description = "Syncs ALL products from CJ Dropshipping (listV2). Pages internally with 10s intervals.")
    public ResponseEntity<ProductSyncResultDtoOut> syncFromCjDropshipping(
            @RequestParam(defaultValue = "true") boolean forceOverwrite) {
        ProductSyncResult result = productSyncUseCase.syncFromCjDropshipping(forceOverwrite);
        return ResponseEntity.ok(ProductSyncResultDtoOut.builder().created(result.getCreated())
                .updated(result.getUpdated()).skipped(result.getSkipped()).total(result.getTotal())
                .page(result.getPage()).hasMore(result.isHasMore()).build());
    }

    @NxAdmin
    @PostMapping("/sync/page")
    @Operation(summary = "Sync one page of products", description = "Syncs ONE page of products from CJ Dropshipping. The frontend iterates calling with incremental page until hasMore=false. Optionally filters by comma-separated categoryIds.")
    public ResponseEntity<ProductSyncResultDtoOut> syncPageFromCjDropshipping(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "true") boolean forceOverwrite,
            @RequestParam(required = false) List<String> categoryIds) {
        ProductSyncResult result = productSyncUseCase.syncPageFromCjDropshipping(page, size, forceOverwrite,
                categoryIds);
        return ResponseEntity.ok(ProductSyncResultDtoOut.builder().created(result.getCreated())
                .updated(result.getUpdated()).skipped(result.getSkipped()).total(result.getTotal())
                .page(result.getPage()).hasMore(result.isHasMore()).build());
    }

    @NxAdmin
    @PostMapping("/sync/discover/page")
    @Operation(summary = "Discover new products by category", description = "Iterates synced L3 categories and searches for new products in CJ "
            + "that don't yet exist in the local DB. Processes ONE category per call. "
            + "The frontend iterates incrementing offset until hasMore=false.")
    public ResponseEntity<ProductSyncResultDtoOut> discoverNewByCategory(
            @Parameter(description = "0-based offset in the L3 category list") @RequestParam(defaultValue = "0") int offset) {
        ProductSyncResult result = productSyncUseCase.discoverNewProductsByCategory(offset);
        return ResponseEntity
                .ok(ProductSyncResultDtoOut.builder().created(result.getCreated()).updated(result.getUpdated())
                        .skipped(result.getSkipped()).total(result.getTotal()).page(result.getPage())
                        .hasMore(result.isHasMore()).totalCategories(result.getTotalCategories()).build());
    }
}
