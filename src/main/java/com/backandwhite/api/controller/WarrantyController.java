package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.WarrantyDtoIn;
import com.backandwhite.api.dto.out.WarrantyDtoOut;
import com.backandwhite.api.mapper.WarrantyApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.WarrantyUseCase;
import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxUser;
import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.valueobject.WarrantyType;
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
@RequestMapping("/api/v1/warranties")
@Tag(name = "Warranties", description = "Endpoints for warranty plan management")
public class WarrantyController {

    private final WarrantyUseCase warrantyUseCase;
    private final WarrantyApiMapper warrantyApiMapper;

    // ── Listings ─────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping
    @Operation(summary = "List paginated warranties", description = "Returns warranties with optional filters by active status and type")
    public ResponseEntity<PaginationDtoOut<WarrantyDtoOut>> findAll(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Filter by type (MANUFACTURER, STORE, EXTENDED, LIMITED)") @RequestParam(required = false) WarrantyType type,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "name") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Ascending order", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {
        Page<Warranty> result = warrantyUseCase.findAll(active, type, page, size, sortBy, ascending);
        return ResponseEntity.ok(PageableUtils.toResponse(result.map(warrantyApiMapper::toDto)));
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping("/{id}")
    @Operation(summary = "Get warranty by ID")
    public ResponseEntity<WarrantyDtoOut> getById(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Parameter(description = "Warranty ID") @PathVariable String id) {
        return ResponseEntity.ok(warrantyApiMapper.toDto(warrantyUseCase.findById(id)));
    }

    @NxAdmin
    @PostMapping
    @Operation(summary = "Create warranty")
    public ResponseEntity<WarrantyDtoOut> create(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @Valid @RequestBody WarrantyDtoIn dto) {
        Warranty created = warrantyUseCase.create(warrantyApiMapper.toDomain(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(warrantyApiMapper.toDto(created));
    }

    @NxAdmin
    @PutMapping("/{id}")
    @Operation(summary = "Update warranty")
    public ResponseEntity<WarrantyDtoOut> update(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @PathVariable String id, @Valid @RequestBody WarrantyDtoIn dto) {
        Warranty updated = warrantyUseCase.update(id, warrantyApiMapper.toDomain(dto));
        return ResponseEntity.ok(warrantyApiMapper.toDto(updated));
    }

    @NxAdmin
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warranty")
    public ResponseEntity<Void> delete(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @PathVariable String id) {
        warrantyUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Status
    // ─────────────────────────────────────────────────────────────────────

    @NxAdmin
    @PatchMapping("/{id}/active")
    @Operation(summary = "Toggle warranty active/inactive status")
    public ResponseEntity<Void> toggleActive(@RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
            @PathVariable String id) {
        warrantyUseCase.toggleActive(id);
        return ResponseEntity.noContent().build();
    }
}
