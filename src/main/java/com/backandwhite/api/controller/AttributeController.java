package com.backandwhite.api.controller;

import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.AttributeDtoIn;
import com.backandwhite.api.dto.out.AttributeDtoOut;
import com.backandwhite.api.mapper.AttributeApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.AttributeUseCase;
import com.backandwhite.domain.model.Attribute;
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
@RequestMapping("/api/v1/attributes")
@Tag(name = "Attributes", description = "Endpoints para gestión de atributos de producto")
public class AttributeController {

        private final AttributeUseCase attributeUseCase;
        private final AttributeApiMapper attributeApiMapper;

        // ── Listados ─────────────────────────────────────────────────────────────

        @GetMapping
        @Operation(summary = "Listar atributos paginados", description = "Devuelve atributos paginados con sus valores")
        public ResponseEntity<PaginationDtoOut<AttributeDtoOut>> findAll(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Buscar por nombre (parcial, case-insensitive)") @RequestParam(required = false) String name,
                        @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "name") @RequestParam(defaultValue = "name") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {
                Page<Attribute> result = attributeUseCase.findAll(name, page, size, sortBy, ascending);
                return ResponseEntity.ok(PageableUtils.toResponse(result.map(attributeApiMapper::toDto)));
        }

        // ── CRUD ─────────────────────────────────────────────────────────────────

        @GetMapping("/{id}")
        @Operation(summary = "Obtener atributo por ID", description = "Devuelve el atributo con sus valores")
        public ResponseEntity<AttributeDtoOut> getById(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID del atributo") @PathVariable String id) {
                return ResponseEntity.ok(attributeApiMapper.toDto(attributeUseCase.findById(id)));
        }

        @PostMapping
        @Operation(summary = "Crear atributo con valores")
        public ResponseEntity<AttributeDtoOut> create(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Valid @RequestBody AttributeDtoIn dto) {
                Attribute created = attributeUseCase.create(attributeApiMapper.toDomain(dto));
                return ResponseEntity.status(HttpStatus.CREATED).body(attributeApiMapper.toDto(created));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar atributo", description = "Sincroniza valores: añade nuevos, actualiza existentes, elimina los no presentes")
        public ResponseEntity<AttributeDtoOut> update(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id,
                        @Valid @RequestBody AttributeDtoIn dto) {
                Attribute updated = attributeUseCase.update(id, attributeApiMapper.toDomain(dto));
                return ResponseEntity.ok(attributeApiMapper.toDto(updated));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar atributo")
        public ResponseEntity<Void> delete(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id) {
                attributeUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }
}
