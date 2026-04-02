package com.backandwhite.domain.repository;

import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.valureobject.MediaCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MediaAssetRepository {

    Page<MediaAsset> findAll(MediaCategory category, String mimeType, String tag, Pageable pageable);

    Optional<MediaAsset> findById(String id);

    Optional<MediaAsset> findByFilename(String filename);

    MediaAsset save(MediaAsset mediaAsset);

    MediaAsset update(MediaAsset mediaAsset);

    void deleteById(String id);

    boolean existsByFilename(String filename);
}
