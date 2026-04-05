package com.backandwhite.api.controller;

import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.WarrantyDtoIn;
import com.backandwhite.api.dto.out.WarrantyDtoOut;
import com.backandwhite.api.mapper.WarrantyApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.WarrantyUseCase;
import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.valureobject.WarrantyType;
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
@Tag(name = "Warranties", description = "Endpoints para gestión de planes de garantía")
public class WarrantyController {

        private final WarrantyUseCase warrantyUseCase;
        private final WarrantyApiMapper warrantyApiMapper;

        // ── Listados ─────────────────────────────────────────────────────────────

        @GetMapping
        @Operation(summary = "Listar garantías paginadas", description = "Devuelve garantías con filtros opcionales por estado activo y tipo")
        public ResponseEntity<PaginationDtoOut<WarrantyDtoOut>> findAll(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Filtrar por estado activo") @RequestParam(required = false) Boolean active,
                        @Parameter(description = "Filtrar por tipo (MANUFACTURER, STORE, EXTENDED, LIMITED)") @RequestParam(required = false) WarrantyType type,
                        @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "name") @RequestParam(defaultValue = "name") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {
                Page<Warranty> result = warrantyUseCase.findAll(active, type, page, size, sortBy, ascending);
                return ResponseEntity.ok(PageableUtils.toResponse(result.map(warrantyApiMapper::toDto)));
        }

        // ── CRUD ─────────────────────────────────────────────────────────────────

        @GetMapping("/{id}")
        @Operation(summary = "Obtener garantía por ID")
        public ResponseEntity<WarrantyDtoOut> getById(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID de la garantía") @PathVariable String id) {
                return ResponseEntity.ok(warrantyApiMapper.toDto(warrantyUseCase.findById(id)));
        }

        @PostMapping
        @Operation(summary = "Crear garantía")
        public ResponseEntity<WarrantyDtoOut> create(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Valid @RequestBody WarrantyDtoIn dto) {
                Warranty created = warrantyUseCase.create(warrantyApiMapper.toDomain(dto));
                return ResponseEntity.status(HttpStatus.CREATED).body(warrantyApiMapper.toDto(created));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar garantía")
        public ResponseEntity<WarrantyDtoOut> update(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id,
                        @Valid @RequestBody WarrantyDtoIn dto) {
                Warranty updated = warrantyUseCase.update(id, warrantyApiMapper.toDomain(dto));
                return ResponseEntity.ok(warrantyApiMapper.toDto(updated));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar garantía")
        public ResponseEntity<Void> delete(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id) {
                warrantyUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }

        // ── Estado ───────────────────────────────────────────────────────────────

        @PatchMapping("/{id}/active")
        @Operation(summary = "Toggle estado activo/inactivo de la garantía")
        public ResponseEntity<Void> toggleActive(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id) {
                warrantyUseCase.toggleActive(id);
                return ResponseEntity.noContent().build();
        }
}
