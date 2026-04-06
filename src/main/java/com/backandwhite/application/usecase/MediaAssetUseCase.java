package com.backandwhite.application.usecase;

import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.valueobject.MediaCategory;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface MediaAssetUseCase {

    MediaAsset upload(MultipartFile file, MediaCategory category, String alt, java.util.List<String> tags);

    Page<MediaAsset> findAll(MediaCategory category, String mimeType, String tag, int page, int size, String sortBy,
            boolean ascending);

    MediaAsset findById(String id);

    MediaAsset updateMetadata(String id, MediaCategory category, String alt, java.util.List<String> tags);

    void delete(String id);

    MediaAsset findByFilename(String filename);

    java.io.InputStream loadFile(String filename);
}
