package com.backandwhite.api.controller;

import com.backandwhite.api.dto.PaginationDtoOut;
import com.backandwhite.api.dto.in.MediaAssetUpdateDtoIn;
import com.backandwhite.api.dto.out.MediaAssetDtoOut;
import com.backandwhite.api.mapper.MediaAssetApiMapper;
import com.backandwhite.api.util.PageableUtils;
import com.backandwhite.application.usecase.MediaAssetUseCase;
import com.backandwhite.common.security.annotation.NxAdmin;
import com.backandwhite.common.security.annotation.NxPublic;
import com.backandwhite.common.security.annotation.NxUser;
import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.valueobject.MediaCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/media")
@Tag(name = "Media", description = "Endpoints for media library management")
public class MediaController {

    private final MediaAssetUseCase mediaAssetUseCase;
    private final MediaAssetApiMapper mediaAssetApiMapper;

    // ── Upload ───────────────────────────────────────────────────────────────

    @NxAdmin
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "Uploads a multimedia file (max 10MB). Generates thumbnail for images")
    public ResponseEntity<MediaAssetDtoOut> upload(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Asset category") @RequestParam(required = false) MediaCategory category,
            @Parameter(description = "Alternative text") @RequestParam(required = false) String alt,
            @Parameter(description = "Tags (comma-separated)") @RequestParam(required = false) List<String> tags) {
        MediaAsset saved = mediaAssetUseCase.upload(file, category, alt, tags);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaAssetApiMapper.toDto(saved));
    }

    // ── Listado ──────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping
    @Operation(summary = "List media assets", description = "Returns paginated assets with optional filters")
    public ResponseEntity<PaginationDtoOut<MediaAssetDtoOut>> findAll(
            @Parameter(description = "Filter by category") @RequestParam(required = false) MediaCategory category,
            @Parameter(description = "Filter by MIME type (partial)") @RequestParam(required = false) String mimeType,
            @Parameter(description = "Filter by tag") @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {
        Page<MediaAsset> result = mediaAssetUseCase.findAll(category, mimeType, tag, page, size, sortBy, ascending);
        return ResponseEntity.ok(PageableUtils.toResponse(result.map(mediaAssetApiMapper::toDto)));
    }

    // ── Detalle ──────────────────────────────────────────────────────────────

    @NxUser
    @GetMapping("/{id}")
    @Operation(summary = "Get media asset by ID")
    public ResponseEntity<MediaAssetDtoOut> getById(@Parameter(description = "Asset ID") @PathVariable String id) {
        return ResponseEntity.ok(mediaAssetApiMapper.toDto(mediaAssetUseCase.findById(id)));
    }

    // ── Actualizar metadata ──────────────────────────────────────────────────

    @NxAdmin
    @PutMapping("/{id}")
    @Operation(summary = "Update metadata", description = "Updates category, alt text and tags of an asset")
    public ResponseEntity<MediaAssetDtoOut> updateMetadata(@PathVariable String id,
            @Valid @RequestBody MediaAssetUpdateDtoIn dto) {
        MediaAsset updated = mediaAssetUseCase.updateMetadata(id, dto.getCategory(), dto.getAlt(), dto.getTags());
        return ResponseEntity.ok(mediaAssetApiMapper.toDto(updated));
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    @NxAdmin
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete media asset", description = "Deletes the file, thumbnail and DB record")
    public ResponseEntity<Void> delete(@Parameter(description = "Asset ID") @PathVariable String id) {
        mediaAssetUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Servir imagen ────────────────────────────────────────────────────────

    @NxPublic
    @GetMapping("/images/{filename}")
    @Operation(summary = "Serve image", description = "Serves the image by filename (public, cacheable)")
    public ResponseEntity<InputStreamResource> serveImage(
            @Parameter(description = "File name") @PathVariable String filename) {
        MediaAsset asset = mediaAssetUseCase.findByFilename(filename);
        InputStream inputStream = mediaAssetUseCase.loadFile(asset.getFilename());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(asset.getMimeType()))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .body(new InputStreamResource(inputStream));
    }
}
