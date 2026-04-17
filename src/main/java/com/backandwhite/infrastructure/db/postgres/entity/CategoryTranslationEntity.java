package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "category_translations", indexes = {@Index(name = "idx_cat_translations_locale", columnList = "locale")})
public class CategoryTranslationEntity {

    @EmbeddedId
    private CategoryTranslationId id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
}
