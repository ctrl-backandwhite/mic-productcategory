package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.infrastructure.entity.AuditableEntity;
import com.backandwhite.domain.valureobject.MediaCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@With
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "media_assets")
public class MediaAssetEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "filename", nullable = false, unique = true, length = 512)
    private String filename;

    @Column(name = "original_name", nullable = false, length = 512)
    private String originalName;

    @Column(name = "mime_type", nullable = false, length = 128)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Column(name = "thumbnail_url", length = 1024)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private MediaCategory category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "alt", columnDefinition = "TEXT")
    private String alt;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;
}
