package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.MediaCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Media asset")
public class MediaAssetDtoOut {

    @Schema(description = "Asset ID")
    private String id;

    @Schema(description = "Generated filename")
    private String filename;

    @Schema(description = "Original filename")
    private String originalName;

    @Schema(description = "MIME type", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "Size in bytes")
    private Long sizeBytes;

    @Schema(description = "File URL")
    private String url;

    @Schema(description = "Thumbnail URL")
    private String thumbnailUrl;

    @Schema(description = "Category")
    private MediaCategory category;

    @Schema(description = "Tags")
    private List<String> tags;

    @Schema(description = "Alternative text")
    private String alt;

    @Schema(description = "Width in pixels")
    private Integer width;

    @Schema(description = "Height in pixels")
    private Integer height;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;
}
