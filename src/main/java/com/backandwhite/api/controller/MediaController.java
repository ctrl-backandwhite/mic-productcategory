package com.backandwhite.api.controller;

import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.MediaAssetUpdateDtoIn;
import com.backandwhite.api.dto.out.MediaAssetDtoOut;
import com.backandwhite.api.mapper.MediaAssetApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.MediaAssetUseCase;
import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.valueobject.MediaCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/media")
@Tag(name = "Media", description = "Endpoints para gestión de la biblioteca de medios")
public class MediaController {

        private final MediaAssetUseCase mediaAssetUseCase;
        private final MediaAssetApiMapper mediaAssetApiMapper;

        // ── Upload ───────────────────────────────────────────────────────────────

        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Subir archivo", description = "Sube un archivo multimedia (max 10MB). Genera thumbnail para imágenes")
        public ResponseEntity<MediaAssetDtoOut> upload(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Archivo a subir") @RequestParam("file") MultipartFile file,
                        @Parameter(description = "Categoría del asset") @RequestParam(required = false) MediaCategory category,
                        @Parameter(description = "Texto alternativo") @RequestParam(required = false) String alt,
                        @Parameter(description = "Etiquetas (comma-separated)") @RequestParam(required = false) List<String> tags) {
                MediaAsset saved = mediaAssetUseCase.upload(file, category, alt, tags);
                return ResponseEntity.status(HttpStatus.CREATED).body(mediaAssetApiMapper.toDto(saved));
        }

        // ── Listado ──────────────────────────────────────────────────────────────

        @GetMapping
        @Operation(summary = "Listar media assets", description = "Devuelve assets paginados con filtros opcionales")
        public ResponseEntity<PaginationDtoOut<MediaAssetDtoOut>> findAll(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Filtrar por categoría") @RequestParam(required = false) MediaCategory category,
                        @Parameter(description = "Filtrar por tipo MIME (parcial)") @RequestParam(required = false) String mimeType,
                        @Parameter(description = "Filtrar por etiqueta") @RequestParam(required = false) String tag,
                        @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Orden ascendente", example = "false") @RequestParam(defaultValue = "false") boolean ascending) {
                Page<MediaAsset> result = mediaAssetUseCase.findAll(category, mimeType, tag, page, size, sortBy,
                                ascending);
                return ResponseEntity.ok(PageableUtils.toResponse(result.map(mediaAssetApiMapper::toDto)));
        }

        // ── Detalle ──────────────────────────────────────────────────────────────

        @GetMapping("/{id}")
        @Operation(summary = "Obtener media asset por ID")
        public ResponseEntity<MediaAssetDtoOut> getById(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID del asset") @PathVariable String id) {
                return ResponseEntity.ok(mediaAssetApiMapper.toDto(mediaAssetUseCase.findById(id)));
        }

        // ── Actualizar metadata ──────────────────────────────────────────────────

        @PutMapping("/{id}")
        @Operation(summary = "Actualizar metadatos", description = "Actualiza categoría, alt text y tags de un asset")
        public ResponseEntity<MediaAssetDtoOut> updateMetadata(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @PathVariable String id,
                        @Valid @RequestBody MediaAssetUpdateDtoIn dto) {
                MediaAsset updated = mediaAssetUseCase.updateMetadata(id, dto.getCategory(), dto.getAlt(),
                                dto.getTags());
                return ResponseEntity.ok(mediaAssetApiMapper.toDto(updated));
        }

        // ── Eliminar ─────────────────────────────────────────────────────────────

        @DeleteMapping("/{id}")
        @Operation(summary = "Eliminar media asset", description = "Elimina el archivo, thumbnail y registro de la BD")
        public ResponseEntity<Void> delete(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "ID del asset") @PathVariable String id) {
                mediaAssetUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }

        // ── Servir imagen ────────────────────────────────────────────────────────

        @GetMapping("/images/{filename}")
        @Operation(summary = "Servir imagen", description = "Sirve la imagen por filename (público, cacheable)")
        public ResponseEntity<InputStreamResource> serveImage(
                        @RequestHeader(AppConstants.HEADER_NX036_AUTH) String nxAuth,
                        @Parameter(description = "Nombre del archivo") @PathVariable String filename) {
                MediaAsset asset = mediaAssetUseCase.findByFilename(filename);
                InputStream inputStream = mediaAssetUseCase.loadFile(asset.getFilename());
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(asset.getMimeType()))
                                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                                .body(new InputStreamResource(inputStream));
        }
}
