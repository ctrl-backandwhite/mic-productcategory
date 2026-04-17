package com.backandwhite.provider;

import com.backandwhite.api.dto.out.MediaAssetDtoOut;
import com.backandwhite.domain.model.MediaAsset;
import com.backandwhite.domain.valueobject.MediaCategory;
import com.backandwhite.infrastructure.db.postgres.entity.MediaAssetEntity;
import java.util.List;

public final class MediaAssetProvider {

    public static final String MEDIA_ID = "media-001";
    public static final String MEDIA_FILENAME = "product-001.jpg";
    public static final String MEDIA_ORIGINAL_NAME = "photo.jpg";
    public static final String MEDIA_MIME_TYPE = "image/jpeg";
    public static final Long MEDIA_SIZE_BYTES = 204800L;
    public static final String MEDIA_URL = "https://cdn.example.com/product-001.jpg";
    public static final String MEDIA_THUMBNAIL_URL = "https://cdn.example.com/thumb/product-001.jpg";
    public static final MediaCategory MEDIA_CATEGORY = MediaCategory.PRODUCT;
    public static final String MEDIA_ALT = "Product image";
    public static final Integer MEDIA_WIDTH = 1920;
    public static final Integer MEDIA_HEIGHT = 1080;
    public static final List<String> MEDIA_TAGS = List.of("product", "hero");

    private MediaAssetProvider() {
    }

    public static MediaAsset mediaAsset() {
        return MediaAsset.builder().id(MEDIA_ID).filename(MEDIA_FILENAME).originalName(MEDIA_ORIGINAL_NAME)
                .mimeType(MEDIA_MIME_TYPE).sizeBytes(MEDIA_SIZE_BYTES).url(MEDIA_URL).thumbnailUrl(MEDIA_THUMBNAIL_URL)
                .category(MEDIA_CATEGORY).alt(MEDIA_ALT).width(MEDIA_WIDTH).height(MEDIA_HEIGHT).tags(MEDIA_TAGS)
                .createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT).build();
    }

    public static MediaAssetEntity mediaAssetEntity() {
        return MediaAssetEntity.builder().id(MEDIA_ID).filename(MEDIA_FILENAME).originalName(MEDIA_ORIGINAL_NAME)
                .mimeType(MEDIA_MIME_TYPE).sizeBytes(MEDIA_SIZE_BYTES).url(MEDIA_URL).thumbnailUrl(MEDIA_THUMBNAIL_URL)
                .category(MEDIA_CATEGORY).alt(MEDIA_ALT).width(MEDIA_WIDTH).height(MEDIA_HEIGHT).tags(MEDIA_TAGS)
                .build();
    }

    public static MediaAssetDtoOut mediaAssetDtoOut() {
        return MediaAssetDtoOut.builder().id(MEDIA_ID).filename(MEDIA_FILENAME).originalName(MEDIA_ORIGINAL_NAME)
                .mimeType(MEDIA_MIME_TYPE).sizeBytes(MEDIA_SIZE_BYTES).url(MEDIA_URL).thumbnailUrl(MEDIA_THUMBNAIL_URL)
                .category(MEDIA_CATEGORY).tags(MEDIA_TAGS).alt(MEDIA_ALT).width(MEDIA_WIDTH).height(MEDIA_HEIGHT)
                .createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT).build();
    }
}
