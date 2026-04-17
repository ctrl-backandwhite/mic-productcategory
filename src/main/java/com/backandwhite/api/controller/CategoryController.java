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
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxUser;
import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategorySyncResult;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.valueobject.CategoryStatus;
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
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Endpoints for category management")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CategorySyncUseCase categorySyncUseCase;
    private final CategoryApiMapper categoryApiMapper;

    // ── Listados ─────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping
    @Operation(summary = "List categories as tree", description = "Returns categories in hierarchical structure, with optional filters by status, active and locale")
    public ResponseEntity<List<CategoryDtoOut>> findByLocale(
            @Parameter(description = "Language code (e.g. es, en, pt-BR)", example = "es") @RequestParam(defaultValue = "en") String locale,
            @Parameter(description = "Filter by publication status (DRAFT, PUBLISHED)") @RequestParam(required = false) CategoryStatus status,
            @Parameter(description = "Filter by active (true or false)") @RequestParam(required = false) Boolean active) {

        List<Category> categories = categoryUseCase.findCategories(locale, status, active);
        return ResponseEntity.ok(categoryApiMapper.toDtoList(categories));
    }

    @NxUser
    @GetMapping("/paged")
    @Operation(summary = "List paginated categories", description = "Returns paginated categories with optional filters. Uses PageableUtils internally.")
    public ResponseEntity<PaginationDtoOut<CategoryDtoOut>> findPaged(
            @Parameter(description = "Language code", example = "es") @RequestParam(defaultValue = "en") String locale,
            @Parameter(description = "Filter by status (DRAFT, PUBLISHED)") @RequestParam(required = false) CategoryStatus status,
            @Parameter(description = "Filter by active") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Search by name (partial, case-insensitive)") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by level (1, 2, 3)") @RequestParam(required = false) Integer level,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "level") @RequestParam(defaultValue = "level") String sortBy,
            @Parameter(description = "Ascending order", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {

        Pageable pageable = PageableUtils.toPageable(page, size, sortBy, ascending);
        Page<Category> result = categoryUseCase.findCategoriesPaged(locale, status, active, name, level,
                pageable.getPageNumber(), pageable.getPageSize(), sortBy, ascending);

        return ResponseEntity.ok(PageableUtils.toResponse(result.map(categoryApiMapper::toDto)));
    }

    @NxUser
    @PostMapping("/search")
    @Operation(summary = "Paginated search with dynamic filters", description = """
            Paginated category listing with dynamic filters via reflection.
            Only non-null fields from the `filters` object are applied as equality predicates.
            Supports locale filtering (translations) + any DTO property.

            Example body:
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

        // toFilterMap extracts via reflection only non-null fields from the filter DTO
        // Resulting map: { status → PUBLISHED, active → true, level → 1 }
        // Passed to the use case which converts it to Specification with
        // FilterUtils.buildSpecification()
        Page<Category> result = categoryUseCase.findCategoriesPaged(request.getLocale(),
                request.getFilters() != null ? request.getFilters().getStatus() : null,
                request.getFilters() != null ? request.getFilters().getActive() : null, null,
                request.getFilters() != null ? request.getFilters().getLevel() : null, pageable.getPageNumber(),
                pageable.getPageSize(), request.getSortBy(), request.isAscending());

        return ResponseEntity.ok(PageableUtils.toResponse(result.map(categoryApiMapper::toDto)));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryDtoOut> getById(@Parameter(description = "Category ID") @PathVariable String id,
            @Parameter(description = "Language code", example = "es") @RequestParam(defaultValue = "en") String locale) {

        return ResponseEntity.ok(categoryApiMapper.toDto(categoryUseCase.findById(id, locale)));
    }

    @NxAdmin
    @PostMapping
    @Operation(summary = "Create category")
    public ResponseEntity<CategoryDtoOut> create(@Valid @RequestBody CategoryDtoIn dto) {
        Category created = categoryUseCase.create(categoryApiMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryApiMapper.toDto(created));
    }

    @NxAdmin
    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<CategoryDtoOut> update(@PathVariable String id, @Valid @RequestBody CategoryDtoIn dto) {

        Category updated = categoryUseCase.update(id, categoryApiMapper.toDomain(dto));
        return ResponseEntity.ok(categoryApiMapper.toDto(updated));
    }

    @NxAdmin
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @DeleteMapping
    @Operation(summary = "Bulk delete categories")
    public ResponseEntity<Void> deleteAll(@RequestBody List<String> ids) {
        categoryUseCase.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    // ── Estado / flags ───────────────────────────────────────────────────────

    @NxAdmin
    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish / unpublish category")
    public ResponseEntity<Void> publish(@PathVariable String id) {
        categoryUseCase.publishCategory(id);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/bulk-status")
    @Operation(summary = "Bulk update category status")
    public ResponseEntity<Void> bulkUpdateStatus(@Valid @RequestBody BulkStatusUpdateDtoIn body) {
        categoryUseCase.bulkUpdateStatus(body.getIds(), body.getStatus());
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/publish-all-drafts")
    @Operation(summary = "Publish all draft categories")
    public ResponseEntity<java.util.Map<String, Integer>> publishAllDrafts() {
        int count = categoryUseCase.publishAllDrafts();
        return ResponseEntity.ok(java.util.Map.of("updated", count));
    }

    @NxAdmin
    @PatchMapping("/{id}/active")
    @Operation(summary = "Activate / deactivate category")
    public ResponseEntity<Void> toggleActive(@PathVariable String id, @RequestParam boolean active) {
        categoryUseCase.toggleActive(id, active);
        return ResponseEntity.noContent().build();
    }

    @NxAdmin
    @PatchMapping("/{id}/featured")
    @Operation(summary = "Mark / unmark category as featured")
    public ResponseEntity<Void> toggleFeatured(@PathVariable String id, @RequestParam boolean featured) {
        categoryUseCase.toggleFeatured(id, featured);
        return ResponseEntity.noContent().build();
    }

    @NxUser
    @GetMapping("/featured")
    @Operation(summary = "List featured categories")
    public ResponseEntity<List<CategoryDtoOut>> findFeatured(@RequestParam(defaultValue = "en") String locale) {
        return ResponseEntity.ok(categoryApiMapper.toDtoList(categoryUseCase.findFeatured(locale)));
    }

    // ── Bulk / Sync ──────────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping("/bulk")
    @Operation(summary = "Bulk category upload")
    public ResponseEntity<BulkCategoryResultDtoOut> bulkCreate(@Valid @RequestBody BulkCategoryDtoIn dto) {

        List<CategoryUseCase.BulkCategoryRow> rows = dto.getRows().stream()
                .map(r -> new CategoryUseCase.BulkCategoryRow(toTranslations(r.getLevel1Translations()),
                        toTranslations(r.getLevel2Translations()), toTranslations(r.getLevel3Translations())))
                .toList();

        BulkCategoryResult result = categoryUseCase.bulkCreate(rows);

        return ResponseEntity.status(HttpStatus.CREATED).body(BulkCategoryResultDtoOut.builder()
                .created(result.getCreated()).skipped(result.getSkipped()).totalRows(result.getTotalRows()).build());
    }

    @NxAdmin
    @PostMapping("/sync")
    @Operation(summary = "Sync categories from CJ Dropshipping")
    public ResponseEntity<CategorySyncResultDtoOut> syncFromCjDropshipping() {
        CategorySyncResult result = categorySyncUseCase.syncFromCjDropshipping();
        return ResponseEntity.ok(CategorySyncResultDtoOut.builder().created(result.getCreated())
                .updated(result.getUpdated()).total(result.getTotal()).build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<CategoryTranslation> toTranslations(List<com.backandwhite.api.dto.in.CategoryTranslationDtoIn> dtos) {
        if (dtos == null || dtos.isEmpty())
            return null;
        return dtos.stream().map(t -> CategoryTranslation.builder().locale(t.getLocale()).name(t.getName()).build())
                .toList();
    }
}
