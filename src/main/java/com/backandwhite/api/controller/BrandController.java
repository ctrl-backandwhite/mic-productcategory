package com.backandwhite.api.controller;

import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.BrandDtoIn;
import com.backandwhite.api.dto.out.BrandDtoOut;
import com.backandwhite.api.mapper.BrandApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.BrandUseCase;
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

        @GetMapping
        @Operation(summary = "List paginated brands", description = "Returns paginated brands with optional filters by status and name")
        public ResponseEntity<PaginationDtoOut<BrandDtoOut>> findAll(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Filter by status (ACTIVE, INACTIVE)") @RequestParam(required = false) BrandStatus status,
                        @Parameter(description = "Search by name (partial, case-insensitive)") @RequestParam(required = false) String name,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Sort field", example = "name") @RequestParam(defaultValue = "name") String sortBy,
                        @Parameter(description = "Ascending order", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {
                Page<Brand> result = brandUseCase.findAll(status, name, page, size, sortBy, ascending);
                return ResponseEntity.ok(PageableUtils.toResponse(result.map(brandApiMapper::toDto)));
        }

        // ── CRUD ─────────────────────────────────────────────────────────────────

        @GetMapping("/{id}")
        @Operation(summary = "Get brand by ID")
        public ResponseEntity<BrandDtoOut> getById(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Brand ID") @PathVariable String id) {
                return ResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findById(id)));
        }

        @GetMapping("/slug/{slug}")
        @Operation(summary = "Get brand by slug", description = "Searches a brand by its URL-friendly slug")
        public ResponseEntity<BrandDtoOut> getBySlug(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Brand slug", example = "nike") @PathVariable String slug) {
                return ResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findBySlug(slug)));
        }

        @PostMapping
        @Operation(summary = "Create brand")
        public ResponseEntity<BrandDtoOut> create(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Valid @RequestBody BrandDtoIn dto) {
                Brand created = brandUseCase.create(brandApiMapper.toDomain(dto));
                return ResponseEntity.status(HttpStatus.CREATED).body(brandApiMapper.toDto(created));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update brand")
        public ResponseEntity<BrandDtoOut> update(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id,
                        @Valid @RequestBody BrandDtoIn dto) {
                Brand updated = brandUseCase.update(id, brandApiMapper.toDomain(dto));
                return ResponseEntity.ok(brandApiMapper.toDto(updated));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete brand")
        public ResponseEntity<Void> delete(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id) {
                brandUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }

        // ── Status ─────────────────────────────────────────────────────────────────────

        @PatchMapping("/{id}/status")
        @Operation(summary = "Toggle brand status (ACTIVE ↔ INACTIVE)")
        public ResponseEntity<Void> toggleStatus(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id) {
                brandUseCase.toggleStatus(id);
                return ResponseEntity.noContent().build();
        }
}
