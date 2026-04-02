package com.backandwhite.infrastructure.db.postgres.repository.impl;

import com.backandwhite.common.exception.Message;
import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.repository.MediaAssetRepository;
import com.backandwhite.domain.valureobject.MediaCategory;
import com.backandwhite.infrastructure.db.postgres.entity.MediaAssetEntity;
import com.backandwhite.infrastructure.db.postgres.mapper.MediaAssetInfraMapper;
import com.backandwhite.infrastructure.db.postgres.repository.MediaAssetJpaRepository;
import com.backandwhite.infrastructure.db.postgres.specification.MediaAssetSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MediaAssetRepositoryImpl implements MediaAssetRepository {

    private final MediaAssetJpaRepository jpaRepository;
    private final MediaAssetInfraMapper mapper;

    @Override
    public Page<MediaAsset> findAll(MediaCategory category, String mimeType, String tag, Pageable pageable) {
        Specification<MediaAssetEntity> spec = MediaAssetSpecification.withFilters(category, mimeType, tag);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<MediaAsset> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<MediaAsset> findByFilename(String filename) {
        return jpaRepository.findByFilename(filename).map(mapper::toDomain);
    }

    @Override
    public MediaAsset save(MediaAsset mediaAsset) {
        mediaAsset.setId(UUID.randomUUID().toString());
        MediaAssetEntity entity = mapper.toEntity(mediaAsset);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public MediaAsset update(MediaAsset mediaAsset) {
        MediaAssetEntity existing = jpaRepository.findById(mediaAsset.getId())
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("MediaAsset", mediaAsset.getId()));

        existing.setAlt(mediaAsset.getAlt());
        existing.setTags(mediaAsset.getTags());
        existing.setCategory(mediaAsset.getCategory());

        return mapper.toDomain(jpaRepository.save(existing));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.findById(id)
                .orElseThrow(() -> Message.ENTITY_NOT_FOUND.toEntityNotFound("MediaAsset", id));
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByFilename(String filename) {
        return jpaRepository.existsByFilename(filename);
    }
}
