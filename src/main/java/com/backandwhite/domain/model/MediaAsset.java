package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.MediaCategory;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset {

    private String id;
    private String filename;
    private String originalName;
    private String mimeType;
    private Long sizeBytes;
    private String url;
    private String thumbnailUrl;
    private MediaCategory category;
    private String alt;
    private Integer width;
    private Integer height;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
