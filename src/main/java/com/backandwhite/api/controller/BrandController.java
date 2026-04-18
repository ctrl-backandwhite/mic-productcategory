package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BrandDtoIn;
import com.backandwhite.api.dto.out.BrandDtoOut;
import com.backandwhite.api.mapper.BrandApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.BrandUseCase;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxUser;
import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.valueobject.BrandStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/brands")
@Tag(name = "Brands", description = "Endpoints for brand management")
public class BrandController {

    private final BrandUseCase brandUseCase;
    private final BrandApiMapper brandApiMapper;

    // ── Listings ─────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping
    @Operation(summary = "List paginated brands", description = "Returns paginated brands with optional filters by status and name")
    public ResponseEntity<PaginationDtoOut<BrandDtoOut>> findAll(
            @Parameter(description = "Filter by status (ACTIVE, INACTIVE)") @RequestParam(required = false) BrandStatus status,
            @Parameter(description = "Search by name (partial, case-insensitive)") @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {
        Page<Brand> result = brandUseCase.findAll(status, name, page, size, sortBy, ascending);
        return ResponseEntity.ok(PageableUtils.toResponse(result.map(brandApiMapper::toDto)));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<BrandDtoOut> getById(@Parameter(description = "Brand ID") @PathVariable String id) {
        return ResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findById(id)));
    }

    @NxUser
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get brand by slug", description = "Searches a brand by its URL-friendly slug")
    public ResponseEntity<BrandDtoOut> getBySlug(
            @Parameter(description = "Brand slug", example = "nike") @PathVariable String slug) {
        return ResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findBySlug(slug)));
    }

    @NxAdmin
    @PostMapping
    @Operation(summary = "Create brand")
    public ResponseEntity<BrandDtoOut> create(@Valid @RequestBody BrandDtoIn dto) {
        Brand created = brandUseCase.create(brandApiMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(brandApiMapper.toDto(created));
    }

    @NxAdmin
    @PutMapping("/{id}")
    @Operation(summary = "Update brand")
    public ResponseEntity<BrandDtoOut> update(@PathVariable String id, @Valid @RequestBody BrandDtoIn dto) {
        Brand updated = brandUseCase.update(id, brandApiMapper.toDomain(dto));
        return ResponseEntity.ok(brandApiMapper.toDto(updated));
    }

    @NxAdmin
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete brand")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        brandUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Status
    // ─────────────────────────────────────────────────────────────────────

    @NxAdmin
    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle brand status (ACTIVE ↔ INACTIVE)")
    public ResponseEntity<Void> toggleStatus(@PathVariable String id) {
        brandUseCase.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }
}
