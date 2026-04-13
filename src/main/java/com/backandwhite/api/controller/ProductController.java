package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PageFilterRequest;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BulkProductDtoIn;
import com.backandwhite.api.dto.in.BulkStatusUpdateDtoIn;
import com.backandwhite.api.dto.in.BulkVariantDtoIn;
import com.backandwhite.api.dto.in.ProductDetailVariantDtoIn;
import com.backandwhite.api.dto.in.ProductDtoIn;
import com.backandwhite.api.dto.in.ProductFilterDto;
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
import com.backandwhite.domain.model.BulkImportResult;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductSyncResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Endpoints para gestión de productos")
public class ProductController {

        private final ProductUseCase productUseCase;
        private final ProductDetailUseCase productDetailUseCase;
        private final ProductSyncUseCase productSyncUseCase;
        private final ProductApiMapper productApiMapper;
        private final ProductDetailApiMapper productDetailApiMapper;
        private final PricingService pricingService;

        @GetMapping("/category/{categoryId}")
        @Operation(summary = "Listar productos por categoría", description = "Devuelve todos los productos de una categoría con sus traducciones y variantes")
        public ResponseEntity<List<ProductDtoOut>> findByCategoryId(
                        @Parameter(description = "ID de la categoría") @PathVariable String categoryId,
                        @Parameter(description = "Código de idioma (ej: es, en, pt-BR)", example = "es") @RequestParam(defaultValue = "en") String locale,
                        @Parameter(description = "Filtrar por estado (DRAFT, PUBLISHED). Si no se envía, muestra todos.") @RequestParam(required = false) String status) {

                List<Product> products = productUseCase.findByCategoryId(categoryId, locale, status);
                products.forEach(pricingService::applyMarginsToProduct);
                List<ProductDtoOut> result = productApiMapper.toDtoList(products);
                return ResponseEntity.ok(result);
        }

        @GetMapping
        @Operation(summary = "Listar productos paginados", description = "Devuelve todos los productos de forma paginada, filtrando por locale. Opcionalmente filtra por categoría.")
        public ResponseEntity<PaginationDtoOut<ProductDtoOut>> findAllPaged(
                        @Parameter(description = "Código de idioma (ej: es, en, pt-BR)", example = "es") @RequestParam(defaultValue = "en") String locale,
                        @Parameter(description = "ID de la categoría (opcional, si no se envía lista todos)") @RequestParam(required = false) String categoryId,
                        @Parameter(description = "Filtrar por estado (DRAFT, PUBLISHED). Si no se envía, muestra todos.") @RequestParam(required = false) String status,
                        @Parameter(description = "Buscar por nombre del producto (coincidencia parcial, case-insensitive)") @RequestParam(required = false) String name,
                        @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {

                Pageable pageable = PageableUtils.toPageable(page, size, sortBy, ascending);
                Page<Product> pagedResult = productUseCase.findAllPaged(locale, categoryId, status, name,
                                pageable.getPageNumber(), pageable.getPageSize(), sortBy, ascending);

                return ResponseEntity.ok(PageableUtils.toResponse(pagedResult.map(p -> {
                        pricingService.applyMarginsToProduct(p);
                        return productApiMapper.toDto(p);
                })));
        }

        @PostMapping("/search")
        @Operation(summary = "Búsqueda paginada de productos con filtros dinámicos", description = """
                        Listado paginado de productos con filtros dinámicos vía reflexión.
                        Solo los campos no nulos del objeto `filters` se aplican como predicados.

                        Ejemplo de body:
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
                Page<Product> result = productUseCase.findAllPaged(
                                request.getLocale(),
                                request.getFilters() != null ? request.getFilters().getCategoryId() : null,
                                request.getFilters() != null && request.getFilters().getStatus() != null
                                                ? request.getFilters().getStatus().name()
                                                : null,
                                null,
                                pageable.getPageNumber(), pageable.getPageSize(),
                                request.getSortBy(), request.isAscending());

                return ResponseEntity.ok(PageableUtils.toResponse(result.map(p -> {
                        pricingService.applyMarginsToProduct(p);
                        return productApiMapper.toDto(p);
                })));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener producto por ID", description = "Devuelve un producto con todas sus traducciones y variantes")
        public ResponseEntity<ProductDtoOut> getById(
                        @Parameter(description = "ID del producto") @PathVariable String id,
                        @Parameter(description = "Código de idioma", example = "es") @RequestParam(defaultValue = "en") String locale) {

                Product product = productUseCase.findById(id, locale);
                pricingService.applyMarginsToProduct(product);
                return ResponseEntity.ok(productApiMapper.toDto(product));
        }

        @PostMapping
        @Operation(summary = "Crear producto", description = "Crea un nuevo producto con sus traducciones y variantes")
        public ResponseEntity<ProductDtoOut> create(
                        @Valid @RequestBody ProductDtoIn dto) {

                Product product = productApiMapper.toDomain(dto);
                Product created = productUseCase.create(product);
                return ResponseEntity.status(HttpStatus.CREATED).body(productApiMapper.toDto(created));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente, incluyendo traducciones y variantes")
        public ResponseEntity<ProductDtoOut> update(
                        @Parameter(description = "ID del producto") @PathVariable String id,
                        @Valid @RequestBody ProductDtoIn dto) {

                Product product = productApiMapper.toDomain(dto);
                Product updated = productUseCase.update(id, product);
                return ResponseEntity.ok(productApiMapper.toDto(updated));
        }

        @DeleteMapping
        @Operation(summary = "Eliminar productos", description = "Elimina uno o más productos y todas sus traducciones y variantes")
        public ResponseEntity<Void> deleteAll(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lista de IDs de productos a eliminar") @RequestBody List<String> ids) {
                productUseCase.deleteAll(ids);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/publish")
        @Operation(summary = "Publicar/despublicar producto", description = "Alterna el estado de un producto entre DRAFT y PUBLISHED")
        public ResponseEntity<Void> publishProduct(
                        @Parameter(description = "ID del producto") @PathVariable String id) {
                productUseCase.publishProduct(id);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/bulk-status")
        @Operation(summary = "Cambiar estado masivo", description = "Cambia el estado de múltiples productos a DRAFT o PUBLISHED")
        public ResponseEntity<Void> bulkUpdateStatus(
                        @Valid @RequestBody BulkStatusUpdateDtoIn body) {
                productUseCase.bulkUpdateStatus(body.getIds(), body.getStatus());
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/detail/{pid}")
        @Operation(summary = "Detalle de producto (CJ)", description = "Obtiene el detalle completo de un producto. Si no existe en la BD local, lo obtiene desde CJ Dropshipping, lo persiste y lo devuelve desde la BD.")
        public ResponseEntity<ProductDetailDtoOut> getProductDetail(
                        @Parameter(description = "CJ Product ID (pid)") @PathVariable String pid,
                        @Parameter(description = "Código de idioma", example = "en") @RequestParam(defaultValue = "en") String locale) {

                ProductDetail detail = productDetailUseCase.getOrFetchFromCj(pid, locale);
                pricingService.applyMarginsToProductDetail(detail);
                return ResponseEntity.ok(productDetailApiMapper.toDto(detail));
        }

        // ── Variant CRUD ─────────────────────────────────────────────────────────

        @GetMapping("/detail/variants")
        @Operation(summary = "Listar todas las variantes (paginado)", description = "Devuelve todas las variantes de todos los productos de forma paginada. Soporta búsqueda, filtro por estado, filtro por PID y ordenamiento.")
        public ResponseEntity<PaginationDtoOut<ProductDetailVariantDtoOut>> findAllVariantsPaged(
                        @Parameter(description = "Código de idioma (es, en, pt-BR)", example = "en") @RequestParam(defaultValue = "en") String locale,
                        @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Texto de búsqueda (nombre, SKU, VID, PID)") @RequestParam(required = false) String search,
                        @Parameter(description = "Filtro por estado (DRAFT / PUBLISHED)") @RequestParam(required = false) String status,
                        @Parameter(description = "Filtrar por PID del producto padre") @RequestParam(required = false) String pid,
                        @Parameter(description = "Campo de ordenamiento") @RequestParam(required = false) String sortBy,
                        @Parameter(description = "Orden ascendente") @RequestParam(defaultValue = "false") boolean ascending) {

                Pageable pageable = PageableUtils.toPageable(page, size, sortBy, ascending);
                Page<ProductDetailVariant> pagedResult = productDetailUseCase.findAllVariantsPaged(
                                pageable.getPageNumber(), pageable.getPageSize(), locale, search, status, pid, sortBy,
                                ascending);

                return ResponseEntity
                                .ok(PageableUtils.toResponse(pagedResult.map(productDetailApiMapper::toVariantDto)));
        }

        @PostMapping("/detail/variants/search")
        @Operation(summary = "Búsqueda paginada de variantes con filtros dinámicos", description = """
                        Listado paginado de variantes con filtros dinámicos.

                        Ejemplo de body:
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
                Page<ProductDetailVariant> result = productDetailUseCase.findAllVariantsPaged(
                                pageable.getPageNumber(), pageable.getPageSize(),
                                localeReq,
                                request.getFilters() != null ? request.getFilters().getSearch() : null,
                                request.getFilters() != null && request.getFilters().getStatus() != null
                                                ? request.getFilters().getStatus().name()
                                                : null,
                                request.getFilters() != null ? request.getFilters().getPid() : null,
                                request.getSortBy(), request.isAscending());

                return ResponseEntity.ok(PageableUtils.toResponse(result.map(productDetailApiMapper::toVariantDto)));
        }

        @GetMapping("/detail/{pid}/variants")
        @Operation(summary = "Listar variantes de un producto", description = "Devuelve todas las variantes de un producto con sus traducciones e inventarios")
        public ResponseEntity<List<ProductDetailVariantDtoOut>> findVariantsByPid(
                        @Parameter(description = "CJ Product ID (pid)") @PathVariable String pid,
                        @Parameter(description = "Código de idioma (es, en, pt-BR)", example = "en") @RequestParam(defaultValue = "en") String locale) {

                List<ProductDetailVariant> variants = productDetailUseCase.findVariantsByPid(pid, locale);
                return ResponseEntity.ok(productDetailApiMapper.toVariantDtoList(variants));
        }

        @GetMapping("/detail/variants/{vid}")
        @Operation(summary = "Obtener variante por VID", description = "Devuelve una variante específica con sus traducciones e inventarios")
        public ResponseEntity<ProductDetailVariantDtoOut> findVariantByVid(
                        @Parameter(description = "Variant ID (vid)") @PathVariable String vid,
                        @Parameter(description = "Código de idioma (es, en, pt-BR)", example = "en") @RequestParam(defaultValue = "en") String locale) {

                ProductDetailVariant variant = productDetailUseCase.findVariantByVid(vid, locale);
                return ResponseEntity.ok(productDetailApiMapper.toVariantDto(variant));
        }

        @PostMapping("/detail/variants")
        @Operation(summary = "Crear variante", description = "Crea una nueva variante manualmente para un producto existente")
        public ResponseEntity<ProductDetailVariantDtoOut> createVariant(
                        @Valid @RequestBody ProductDetailVariantDtoIn dto) {

                ProductDetailVariant variant = productDetailApiMapper.toVariantDomain(dto);
                ProductDetailVariant created = productDetailUseCase.createVariant(variant);
                return ResponseEntity.status(HttpStatus.CREATED).body(productDetailApiMapper.toVariantDto(created));
        }

        @PutMapping("/detail/variants/{vid}")
        @Operation(summary = "Actualizar variante", description = "Actualiza los datos de una variante existente, incluyendo traducciones e inventarios")
        public ResponseEntity<ProductDetailVariantDtoOut> updateVariant(
                        @Parameter(description = "Variant ID (vid)") @PathVariable String vid,
                        @Valid @RequestBody ProductDetailVariantDtoIn dto) {

                ProductDetailVariant variant = productDetailApiMapper.toVariantDomain(dto);
                ProductDetailVariant updated = productDetailUseCase.updateVariant(vid, variant);
                return ResponseEntity.ok(productDetailApiMapper.toVariantDto(updated));
        }

        @DeleteMapping("/detail/variants/{vid}")
        @Operation(summary = "Eliminar variante", description = "Elimina una variante y todas sus traducciones e inventarios")
        public ResponseEntity<Void> deleteVariant(
                        @Parameter(description = "Variant ID (vid)") @PathVariable String vid) {
                productDetailUseCase.deleteVariant(vid);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/detail/variants")
        @Operation(summary = "Eliminar variantes masivo", description = "Elimina múltiples variantes y sus traducciones e inventarios")
        public ResponseEntity<Void> deleteVariants(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lista de VIDs a eliminar") @RequestBody List<String> vids) {
                productDetailUseCase.deleteVariants(vids);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/detail/variants/{vid}/publish")
        @Operation(summary = "Publicar/despublicar variante", description = "Alterna el estado de una variante entre DRAFT y PUBLISHED")
        public ResponseEntity<Void> publishVariant(
                        @Parameter(description = "Variant ID (vid)") @PathVariable String vid) {
                productDetailUseCase.publishVariant(vid);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/detail/variants/bulk-status")
        @Operation(summary = "Cambiar estado masivo de variantes", description = "Cambia el estado de múltiples variantes a DRAFT o PUBLISHED")
        public ResponseEntity<Void> bulkUpdateVariantStatus(
                        @Valid @RequestBody BulkStatusUpdateDtoIn body) {
                productDetailUseCase.bulkUpdateVariantStatus(body.getIds(), body.getStatus());
                return ResponseEntity.noContent().build();
        }

        // ── Bulk operations ──────────────────────────────────────────────────────

        @PostMapping("/bulk")
        @Operation(summary = "Carga masiva de productos", description = "Crea múltiples productos de forma masiva. Los errores individuales no abortan el lote.")
        public ResponseEntity<BulkImportResultDtoOut> bulkCreateProducts(
                        @Valid @RequestBody BulkProductDtoIn dto) {

                List<Product> products = dto.getRows().stream()
                                .map(productApiMapper::toDomain)
                                .toList();

                BulkImportResult result = productUseCase.bulkCreate(products);

                return ResponseEntity.status(HttpStatus.CREATED).body(BulkImportResultDtoOut.builder()
                                .created(result.getCreated())
                                .failed(result.getFailed())
                                .totalRows(result.getTotalRows())
                                .errors(result.getErrors().stream()
                                                .map(e -> BulkImportResultDtoOut.RowError.builder()
                                                                .row(e.getRow())
                                                                .message(e.getMessage())
                                                                .build())
                                                .toList())
                                .build());
        }

        @PostMapping("/detail/variants/bulk")
        @Operation(summary = "Carga masiva de variantes", description = "Crea múltiples variantes de forma masiva. Los errores individuales no abortan el lote.")
        public ResponseEntity<BulkImportResultDtoOut> bulkCreateVariants(
                        @Valid @RequestBody BulkVariantDtoIn dto) {

                List<ProductDetailVariant> variants = dto.getRows().stream()
                                .map(productDetailApiMapper::toVariantDomain)
                                .toList();

                BulkImportResult result = productDetailUseCase.bulkCreateVariants(variants);

                return ResponseEntity.status(HttpStatus.CREATED).body(BulkImportResultDtoOut.builder()
                                .created(result.getCreated())
                                .failed(result.getFailed())
                                .totalRows(result.getTotalRows())
                                .errors(result.getErrors().stream()
                                                .map(e -> BulkImportResultDtoOut.RowError.builder()
                                                                .row(e.getRow())
                                                                .message(e.getMessage())
                                                                .build())
                                                .toList())
                                .build());
        }

        // ── Sync ──────────────────────────────────────────────────────────────────

        @PostMapping("/sync")
        @Operation(summary = "Sincronizar todos los productos", description = "Sincroniza TODOS los productos desde CJ Dropshipping (listV2). Pagina internamente con intervalos de 10 s.")
        public ResponseEntity<ProductSyncResultDtoOut> syncFromCjDropshipping(
                        @RequestParam(defaultValue = "true") boolean forceOverwrite) {
                ProductSyncResult result = productSyncUseCase.syncFromCjDropshipping(forceOverwrite);
                return ResponseEntity.ok(ProductSyncResultDtoOut.builder()
                                .created(result.getCreated())
                                .updated(result.getUpdated())
                                .skipped(result.getSkipped())
                                .total(result.getTotal())
                                .page(result.getPage())
                                .hasMore(result.isHasMore())
                                .build());
        }

        @PostMapping("/sync/page")
        @Operation(summary = "Sincronizar una página de productos", description = "Sincroniza UNA página de productos desde CJ Dropshipping. El frontend itera llamando con page incremental hasta que hasMore=false. Opcionalmente filtra por categoryIds separados por coma.")
        public ResponseEntity<ProductSyncResultDtoOut> syncPageFromCjDropshipping(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "100") int size,
                        @RequestParam(defaultValue = "true") boolean forceOverwrite,
                        @RequestParam(required = false) List<String> categoryIds) {
                ProductSyncResult result = productSyncUseCase.syncPageFromCjDropshipping(page, size, forceOverwrite,
                                categoryIds);
                return ResponseEntity.ok(ProductSyncResultDtoOut.builder()
                                .created(result.getCreated())
                                .updated(result.getUpdated())
                                .skipped(result.getSkipped())
                                .total(result.getTotal())
                                .page(result.getPage())
                                .hasMore(result.isHasMore())
                                .build());
        }

        @PostMapping("/sync/discover/page")
        @Operation(summary = "Descubrir productos nuevos por categoría", description = "Recorre las categorías L3 sincronizadas y busca productos nuevos en CJ "
                        + "que aún no existen en la BD local. Procesa UNA categoría por llamada. "
                        + "El frontend itera incrementando offset hasta que hasMore=false.")
        public ResponseEntity<ProductSyncResultDtoOut> discoverNewByCategory(
                        @Parameter(description = "Offset 0-based en la lista de categorías L3") @RequestParam(defaultValue = "0") int offset) {
                ProductSyncResult result = productSyncUseCase.discoverNewProductsByCategory(offset);
                return ResponseEntity.ok(ProductSyncResultDtoOut.builder()
                                .created(result.getCreated())
                                .updated(result.getUpdated())
                                .skipped(result.getSkipped())
                                .total(result.getTotal())
                                .page(result.getPage())
                                .hasMore(result.isHasMore())
                                .totalCategories(result.getTotalCategories())
                                .build());
        }
}
