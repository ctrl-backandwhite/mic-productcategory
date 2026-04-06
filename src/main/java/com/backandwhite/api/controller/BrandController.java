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
@Tag(name = "Brands", description = "Endpoints para gestión de marcas")
public class BrandController {

        private final BrandUseCase brandUseCase;
        private final BrandApiMapper brandApiMapper;

        // ── Listados ─────────────────────────────────────────────────────────────

        @GetMapping
        @Operation(summary = "Listar marcas paginadas", description = "Devuelve marcas paginadas con filtros opcionales por estado y nombre")
        public ResponseEntity<PaginationDtoOut<BrandDtoOut>> findAll(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Filtrar por estado (ACTIVE, INACTIVE)") @RequestParam(required = false) BrandStatus status,
                        @Parameter(description = "Buscar por nombre (parcial, case-insensitive)") @RequestParam(required = false) String name,
                        @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "name") @RequestParam(defaultValue = "name") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {
                Page<Brand> result = brandUseCase.findAll(status, name, page, size, sortBy, ascending);
                return ResponseEntity.ok(PageableUtils.toResponse(result.map(brandApiMapper::toDto)));
        }

        // ── CRUD ─────────────────────────────────────────────────────────────────

        @GetMapping("/{id}")
        @Operation(summary = "Obtener marca por ID")
        public ResponseEntity<BrandDtoOut> getById(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID de la marca") @PathVariable String id) {
                return ResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findById(id)));
        }

        @GetMapping("/slug/{slug}")
        @Operation(summary = "Obtener marca por slug", description = "Busca una marca por su slug URL-friendly")
        public ResponseEntity<BrandDtoOut> getBySlug(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Slug de la marca", example = "nike") @PathVariable String slug) {
                return ResponseEntity.ok(brandApiMapper.toDto(brandUseCase.findBySlug(slug)));
        }

        @PostMapping
        @Operation(summary = "Crear marca")
        public ResponseEntity<BrandDtoOut> create(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Valid @RequestBody BrandDtoIn dto) {
                Brand created = brandUseCase.create(brandApiMapper.toDomain(dto));
                return ResponseEntity.status(HttpStatus.CREATED).body(brandApiMapper.toDto(created));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar marca")
        public ResponseEntity<BrandDtoOut> update(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id,
                        @Valid @RequestBody BrandDtoIn dto) {
                Brand updated = brandUseCase.update(id, brandApiMapper.toDomain(dto));
                return ResponseEntity.ok(brandApiMapper.toDto(updated));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar marca")
        public ResponseEntity<Void> delete(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id) {
                brandUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }

        // ── Estado ───────────────────────────────────────────────────────────────

        @PatchMapping("/{id}/status")
        @Operation(summary = "Cambiar estado de marca (ACTIVE ↔ INACTIVE)")
        public ResponseEntity<Void> toggleStatus(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id) {
                brandUseCase.toggleStatus(id);
                return ResponseEntity.noContent().build();
        }
}
