package com.backandwhite.api.controller;

import com.backandwhite.api.dto.in.BulkCategoryDtoIn;
import com.backandwhite.api.dto.in.CategoryDtoIn;
import com.backandwhite.api.dto.out.BulkCategoryResultDtoOut;
import com.backandwhite.api.dto.out.CategoryDtoOut;
import com.backandwhite.api.dto.out.CategorySyncResultDtoOut;
import com.backandwhite.api.dto.out.PagedCategoryDtoOut;
import com.backandwhite.api.mapper.CategoryApiMapper;
import com.backandwhite.application.usecase.CategorySyncUseCase;
import com.backandwhite.application.usecase.CategoryUseCase;
import com.backandwhite.domain.model.BulkCategoryResult;
import com.backandwhite.domain.model.Category;
import com.backandwhite.domain.model.CategorySyncResult;
import com.backandwhite.domain.model.CategoryTranslation;
import com.backandwhite.domain.valureobject.CategoryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

        @GetMapping
        @Operation(summary = "Listar categorías en árbol", description = "Devuelve categorías en estructura jerárquica, con filtros opcionales por estado, activo y locale")
        public ResponseEntity<List<CategoryDtoOut>> findByLocale(
                        @Parameter(description = "Código de idioma (ej: es, en, pt-BR)", example = "es") @RequestParam(defaultValue = "es") String locale,
                        @Parameter(description = "Filtrar por estado de publicación (DRAFT, PUBLISHED)") @RequestParam(required = false) CategoryStatus status,
                        @Parameter(description = "Filtrar por activo (true o false)") @RequestParam(required = false) Boolean active) {

                List<Category> categories = categoryUseCase.findCategories(locale, status, active);
                List<CategoryDtoOut> result = categoryApiMapper.toDtoList(categories);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/paged")
        @Operation(summary = "Listar categorías paginadas", description = "Devuelve categorías paginadas con filtros opcionales por locale, estado, activo, nombre y nivel")
        public ResponseEntity<PagedCategoryDtoOut> findPaged(
                        @Parameter(description = "Código de idioma", example = "es") @RequestParam(required = false) String locale,
                        @Parameter(description = "Filtrar por estado (DRAFT, PUBLISHED)") @RequestParam(required = false) CategoryStatus status,
                        @Parameter(description = "Filtrar por activo") @RequestParam(required = false) Boolean active,
                        @Parameter(description = "Buscar por nombre (parcial, case-insensitive)") @RequestParam(required = false) String name,
                        @Parameter(description = "Filtrar por nivel (1, 2, 3)") @RequestParam(required = false) Integer level,
                        @Parameter(description = "Número de página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "level") @RequestParam(defaultValue = "level") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "true") @RequestParam(defaultValue = "true") boolean ascending) {

                Page<Category> pagedResult = categoryUseCase.findCategoriesPaged(locale, status, active, name, level,
                                page,
                                size, sortBy, ascending);

                PagedCategoryDtoOut response = PagedCategoryDtoOut.builder()
                                .content(categoryApiMapper.toDtoList(pagedResult.getContent()))
                                .page(pagedResult.getNumber())
                                .size(pagedResult.getSize())
                                .totalElements(pagedResult.getTotalElements())
                                .totalPages(pagedResult.getTotalPages())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Obtener categoría por ID", description = "Devuelve una categoría con todas sus traducciones")
        public ResponseEntity<CategoryDtoOut> getById(
                        @Parameter(description = "ID de la categoría") @PathVariable String id,
                        @Parameter(description = "Código de idioma", example = "es") @RequestParam(defaultValue = "es") String locale) {

                Category category = categoryUseCase.findById(id, locale);
                return ResponseEntity.ok(categoryApiMapper.toDto(category));
        }

        @PostMapping
        @Operation(summary = "Crear categoría", description = "Crea una nueva categoría con sus traducciones. Se crea en estado DRAFT y activa por defecto.")
        public ResponseEntity<CategoryDtoOut> create(
                        @Valid @RequestBody CategoryDtoIn dto) {

                Category category = categoryApiMapper.toDomain(dto);
                Category created = categoryUseCase.create(category);
                return ResponseEntity.status(HttpStatus.CREATED).body(categoryApiMapper.toDto(created));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar categoría", description = "Actualiza parentId, level y traducciones de una categoría existente")
        public ResponseEntity<CategoryDtoOut> update(
                        @Parameter(description = "ID de la categoría") @PathVariable String id,
                        @Valid @RequestBody CategoryDtoIn dto) {

                Category category = categoryApiMapper.toDomain(dto);
                Category updated = categoryUseCase.update(id, category);
                return ResponseEntity.ok(categoryApiMapper.toDto(updated));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar categoría", description = "Elimina una categoría y sus traducciones en cualquier nivel. Las subcategorías huérfanas quedan sin padre.")
        public ResponseEntity<Void> delete(
                        @Parameter(description = "ID de la categoría") @PathVariable String id) {
                categoryUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping
        @Operation(summary = "Eliminar categorías masivo", description = "Elimina múltiples categorías y sus traducciones")
        public ResponseEntity<Void> deleteAll(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lista de IDs a eliminar") @RequestBody List<String> ids) {
                categoryUseCase.deleteAll(ids);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/publish")
        @Operation(summary = "Publicar categoría", description = "Cambia el estado de una categoría de DRAFT a PUBLISHED")
        public ResponseEntity<Void> publish(
                        @Parameter(description = "ID de la categoría") @PathVariable String id) {
                categoryUseCase.publishCategory(id);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/bulk-status")
        @Operation(summary = "Cambiar estado masivo de categorías", description = "Cambia el estado de múltiples categorías a DRAFT o PUBLISHED")
        public ResponseEntity<Void> bulkUpdateStatus(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "IDs y estado destino") @RequestBody java.util.Map<String, Object> body) {
                @SuppressWarnings("unchecked")
                List<String> ids = (List<String>) body.get("ids");
                String status = (String) body.get("status");
                categoryUseCase.bulkUpdateStatus(ids, status);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/active")
        @Operation(summary = "Activar/desactivar categoría", description = "Activa o desactiva una categoría")
        public ResponseEntity<Void> toggleActive(
                        @Parameter(description = "ID de la categoría") @PathVariable String id,
                        @Parameter(description = "Estado activo (true o false)") @RequestParam boolean active) {
                categoryUseCase.toggleActive(id, active);
                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/featured")
        @Operation(summary = "Marcar/desmarcar categoría como principal", description = "Marca o desmarca una categoría como principal/destacada")
        public ResponseEntity<Void> toggleFeatured(
                        @Parameter(description = "ID de la categoría") @PathVariable String id,
                        @Parameter(description = "Marcar como principal (true o false)") @RequestParam boolean featured) {
                categoryUseCase.toggleFeatured(id, featured);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/featured")
        @Operation(summary = "Listar categorías principales", description = "Devuelve las categorías marcadas como principales/destacadas")
        public ResponseEntity<List<CategoryDtoOut>> findFeatured(
                        @Parameter(description = "Código de idioma", example = "es") @RequestParam(defaultValue = "es") String locale) {
                List<Category> categories = categoryUseCase.findFeatured(locale);
                return ResponseEntity.ok(categoryApiMapper.toDtoList(categories));
        }

        @PostMapping("/bulk")
        @Operation(summary = "Carga masiva de categorías", description = "Crea categorías con hasta 3 niveles jerárquicos de forma masiva. Las categorías existentes se omiten.")
        public ResponseEntity<BulkCategoryResultDtoOut> bulkCreate(
                        @Valid @RequestBody BulkCategoryDtoIn dto) {

                List<CategoryUseCase.BulkCategoryRow> rows = dto.getRows().stream()
                                .map(r -> new CategoryUseCase.BulkCategoryRow(
                                                r.getLevel1Translations() != null
                                                                ? r.getLevel1Translations().stream()
                                                                                .map(t -> CategoryTranslation.builder()
                                                                                                .locale(t.getLocale())
                                                                                                .name(t.getName())
                                                                                                .build())
                                                                                .toList()
                                                                : List.of(),
                                                r.getLevel2Translations() != null
                                                                ? r.getLevel2Translations().stream()
                                                                                .map(t -> CategoryTranslation.builder()
                                                                                                .locale(t.getLocale())
                                                                                                .name(t.getName())
                                                                                                .build())
                                                                                .toList()
                                                                : null,
                                                r.getLevel3Translations() != null
                                                                ? r.getLevel3Translations().stream()
                                                                                .map(t -> CategoryTranslation.builder()
                                                                                                .locale(t.getLocale())
                                                                                                .name(t.getName())
                                                                                                .build())
                                                                                .toList()
                                                                : null))
                                .toList();

                BulkCategoryResult result = categoryUseCase.bulkCreate(rows);

                return ResponseEntity.status(HttpStatus.CREATED).body(BulkCategoryResultDtoOut.builder()
                                .created(result.getCreated())
                                .skipped(result.getSkipped())
                                .totalRows(result.getTotalRows())
                                .build());
        }

        @PostMapping("/sync")
        @Operation(summary = "Sincronizar categorías", description = "Sincroniza las categorías en inglés desde CJ Dropshipping. Crea nuevas o actualiza existentes.")
        public ResponseEntity<CategorySyncResultDtoOut> syncFromCjDropshipping() {
                CategorySyncResult result = categorySyncUseCase.syncFromCjDropshipping();
                return ResponseEntity.ok(CategorySyncResultDtoOut.builder()
                                .created(result.getCreated())
                                .updated(result.getUpdated())
                                .total(result.getTotal())
                                .build());
        }

}
