package com.backandwhite.application.usecase.impl;

import com.backandwhite.application.service.StorageService;
import com.backandwhite.application.usecase.MediaAssetUseCase;
import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.repository.MediaAssetRepository;
import com.backandwhite.domain.valureobject.MediaCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class MediaAssetUseCaseImpl implements MediaAssetUseCase {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            "application/pdf", "video/mp4");
    private static final Set<String> IMAGE_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final MediaAssetRepository mediaAssetRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public MediaAsset upload(MultipartFile file, MediaCategory category, String alt, List<String> tags) {
        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo de 10MB");
        }

        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + contentType);
        }

        try {
            // Almacenar archivo principal
            String filename = storageService.store(file.getOriginalFilename(), contentType, file.getInputStream());
            String url = storageService.getUrl(filename);

            // Generar thumbnail si es imagen
            String thumbnailUrl = null;
            if (IMAGE_MIME_TYPES.contains(contentType)) {
                InputStream thumbStream = file.getInputStream();
                String thumbFilename = storageService.storeThumbnail(file.getOriginalFilename(), contentType,
                        thumbStream);
                thumbnailUrl = storageService.getThumbnailUrl(thumbFilename);
            }

            MediaAsset mediaAsset = MediaAsset.builder()
                    .filename(filename)
                    .originalName(file.getOriginalFilename())
                    .mimeType(contentType)
                    .sizeBytes(file.getSize())
                    .url(url)
                    .thumbnailUrl(thumbnailUrl)
                    .category(category != null ? category : MediaCategory.GENERAL)
                    .alt(alt)
                    .tags(tags != null ? tags : List.of())
                    .build();

            MediaAsset saved = mediaAssetRepository.save(mediaAsset);
            log.info("Media asset uploaded: {} ({})", saved.getOriginalName(), saved.getId());
            return saved;

        } catch (Exception e) {
            log.error("Error uploading media asset: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Error al subir el archivo", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MediaAsset> findAll(MediaCategory category, String mimeType, String tag,
            int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return mediaAssetRepository.findAll(category, mimeType, tag, PageRequest.of(page, size, sort));
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset findById(String id) {
        return mediaAssetRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("MediaAsset", id));
    }

    @Override
    @Transactional
    public MediaAsset updateMetadata(String id, MediaCategory category, String alt, List<String> tags) {
        MediaAsset existing = findById(id);
        existing.setCategory(category);
        existing.setAlt(alt);
        existing.setTags(tags != null ? tags : List.of());
        return mediaAssetRepository.update(existing);
    }

    @Override
    @Transactional
    public void delete(String id) {
        MediaAsset asset = findById(id);
        // Eliminar archivos del storage
        storageService.delete(asset.getFilename());
        if (asset.getThumbnailUrl() != null) {
            // Extraer nombre del thumbnail de la URL
            String thumbFilename = asset.getThumbnailUrl().substring(asset.getThumbnailUrl().lastIndexOf('/') + 1);
            storageService.deleteThumbnail(thumbFilename);
        }
        mediaAssetRepository.deleteById(id);
        log.info("Media asset deleted: {} ({})", asset.getOriginalName(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public MediaAsset findByFilename(String filename) {
        return mediaAssetRepository.findByFilename(filename)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("MediaAsset", filename));
    }

    @Override
    public InputStream loadFile(String filename) {
        return storageService.load(filename);
    }
}
