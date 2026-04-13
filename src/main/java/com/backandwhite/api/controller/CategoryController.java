package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PageFilterRequest;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BulkCategoryDtoIn;
import com.backandwhite.api.dto.in.BulkStatusUpdateDtoIn;
import com.backandwhite.api.dto.in.CategoryDtoIn;
import com.backandwhite.api.dto.in.CategoryFilterDto;
import com.backandwhite.api.dto.out.BulkCategoryResultDtoOut;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.CategorySyncResultDtoOut;
import com.backandwhite.api.mapper.CategoryApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.CategorySyncUseCase;
import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategorySyncResult;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.valueobject.CategoryStatus;
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
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Endpoints para gestión de categorías")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CategorySyncUseCase categorySyncUseCase;
    private final CategoryApiMapper categoryApiMapper;

    // ── Listados ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar categorías en árbol", description = "Devuelve categorías en estructura jerárquica, con filtros opcionales por estado, activo y locale")
    public ResponseEntity<List<CategoryDtoOut>> findByLocale(
            @Parameter(description = "Código de idioma (ej: es, en, pt-BR)", example = "es") @RequestParam(defaultValue = "en") String locale,
            @Parameter(description = "Filtrar por estado de publicación (DRAFT, PUBLISHED)") @RequestParam(required = false) CategoryStatus status,
            @Parameter(description = "Filtrar por activo (true o false)") @RequestParam(required = false) Boolean active) {

        List<Category> categories = categoryUseCase.findCategories(locale, status, active);
        return ResponseEntity.ok(categoryApiMapper.toDtoList(categories));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar categorías paginadas", description = "Devuelve categorías paginadas con filtros opcionales. Usa PageableUtils internamente.")
    public ResponseEntity<PaginationDtoOut<CategoryDtoOut>> findPaged(
            @Parameter(description = "Código de idioma", example = "es") @RequestParam(defaultValue = "en") String locale,
            @Parameter(description = "Filtrar por estado (DRAFT, PUBLISHED)") @RequestParam(required = false) CategoryStatus status,
            @Parameter(description = "Filtrar por activo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Buscar por nombre (parcial, case-insensitive)") @RequestParam(required = false) String name,
            @Parameter(description = "Filtrar por nivel (1, 2, 3)") @RequestParam(required = false) Integer level,
            @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento", example = "level") @RequestParam(defaultValue = "level") String sortBy,
            @Parameter(description = "Orden ascendente", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {

        Pageable pageable = PageableUtils.toPageable(page, size, sortBy, ascending);
        Page<Category> result = categoryUseCase.findCategoriesPaged(locale, status, active, name, level,
                pageable.getPageNumber(), pageable.getPageSize(), sortBy, ascending);

        return ResponseEntity.ok(PageableUtils.toResponse(result.map(categoryApiMapper::toDto)));
    }

    @PostMapping("/search")
    @Operation(summary = "Búsqueda paginada con filtros dinámicos", description = """
            Listado paginado de categorías con filtros dinámicos vía reflexión.
            Solo los campos no nulos del objeto `filters` se aplican como predicados de igualdad.
            Soporta filtrado por locale (traducciones) + cualquier propiedad del DTO.

            Ejemplo de body:
            ```json
            {
              "page": 0, "size": 20, "sortBy": "level", "ascending": true,
              "locale": "es",
              "filters": { "status": "PUBLISHED", "active": true, "level": 1 }
            }
            ```
            """)
    public ResponseEntity<PaginationDtoOut<CategoryDtoOut>> search(
            @Valid @RequestBody PageFilterRequest<CategoryFilterDto> request) {

        Pageable pageable = PageableUtils.toPageable(request);

        // toFilterMap extrae via reflexión solo los campos no nulos del DTO de filtros
        // El mapa resultante: { status → PUBLISHED, active → true, level → 1 }
        // Se pasa al use case que lo convierte a Specification con
        // FilterUtils.buildSpecification()
        Page<Category> result = categoryUseCase.findCategoriesPaged(
                request.getLocale(),
                request.getFilters() != null ? request.getFilters().getStatus() : null,
                request.getFilters() != null ? request.getFilters().getActive() : null,
                null,
                request.getFilters() != null ? request.getFilters().getLevel() : null,
                pageable.getPageNumber(), pageable.getPageSize(),
                request.getSortBy(), request.isAscending());

        return ResponseEntity.ok(PageableUtils.toResponse(result.map(categoryApiMapper::toDto)));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoryDtoOut> getById(
            @Parameter(description = "ID de la categoría") @PathVariable String id,
            @Parameter(description = "Código de idioma", example = "es") @RequestParam(defaultValue = "en") String locale) {

        return ResponseEntity.ok(categoryApiMapper.toDto(categoryUseCase.findById(id, locale)));
    }

    @PostMapping
    @Operation(summary = "Crear categoría")
    public ResponseEntity<CategoryDtoOut> create(@Valid @RequestBody CategoryDtoIn dto) {
        Category created = categoryUseCase.create(categoryApiMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryApiMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría")
    public ResponseEntity<CategoryDtoOut> update(
            @PathVariable String id,
            @Valid @RequestBody CategoryDtoIn dto) {

        Category updated = categoryUseCase.update(id, categoryApiMapper.toDomain(dto));
        return ResponseEntity.ok(categoryApiMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Eliminar categorías masivo")
    public ResponseEntity<Void> deleteAll(@RequestBody List<String> ids) {
        categoryUseCase.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    // ── Estado / flags ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publicar / despublicar categoría")
    public ResponseEntity<Void> publish(@PathVariable String id) {
        categoryUseCase.publishCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/bulk-status")
    @Operation(summary = "Cambiar estado masivo de categorías")
    public ResponseEntity<Void> bulkUpdateStatus(@Valid @RequestBody BulkStatusUpdateDtoIn body) {
        categoryUseCase.bulkUpdateStatus(body.getIds(), body.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/publish-all-drafts")
    @Operation(summary = "Publicar todas las categorías en borrador")
    public ResponseEntity<java.util.Map<String, Integer>> publishAllDrafts() {
        int count = categoryUseCase.publishAllDrafts();
        return ResponseEntity.ok(java.util.Map.of("updated", count));
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Activar / desactivar categoría")
    public ResponseEntity<Void> toggleActive(
            @PathVariable String id,
            @RequestParam boolean active) {
        categoryUseCase.toggleActive(id, active);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/featured")
    @Operation(summary = "Marcar / desmarcar categoría como destacada")
    public ResponseEntity<Void> toggleFeatured(
            @PathVariable String id,
            @RequestParam boolean featured) {
        categoryUseCase.toggleFeatured(id, featured);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/featured")
    @Operation(summary = "Listar categorías destacadas")
    public ResponseEntity<List<CategoryDtoOut>> findFeatured(
            @RequestParam(defaultValue = "en") String locale) {
        return ResponseEntity.ok(categoryApiMapper.toDtoList(categoryUseCase.findFeatured(locale)));
    }

    // ── Bulk / Sync ──────────────────────────────────────────────────────────

    @PostMapping("/bulk")
    @Operation(summary = "Carga masiva de categorías")
    public ResponseEntity<BulkCategoryResultDtoOut> bulkCreate(@Valid @RequestBody BulkCategoryDtoIn dto) {

        List<CategoryUseCase.BulkCategoryRow> rows = dto.getRows().stream()
                .map(r -> new CategoryUseCase.BulkCategoryRow(
                        toTranslations(r.getLevel1Translations()),
                        toTranslations(r.getLevel2Translations()),
                        toTranslations(r.getLevel3Translations())))
                .toList();

        BulkCategoryResult result = categoryUseCase.bulkCreate(rows);

        return ResponseEntity.status(HttpStatus.CREATED).body(BulkCategoryResultDtoOut.builder()
                .created(result.getCreated())
                .skipped(result.getSkipped())
                .totalRows(result.getTotalRows())
                .build());
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar categorías desde CJ Dropshipping")
    public ResponseEntity<CategorySyncResultDtoOut> syncFromCjDropshipping() {
        CategorySyncResult result = categorySyncUseCase.syncFromCjDropshipping();
        return ResponseEntity.ok(CategorySyncResultDtoOut.builder()
                .created(result.getCreated())
                .updated(result.getUpdated())
                .total(result.getTotal())
                .build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<CategoryTranslation> toTranslations(
            List<com.backandwhite.api.dto.in.CategoryTranslationDtoIn> dtos) {
        if (dtos == null || dtos.isEmpty())
            return null;
        return dtos.stream()
                .map(t -> CategoryTranslation.builder()
                        .locale(t.getLocale())
                        .name(t.getName())
                        .build())
                .toList();
    }
}
