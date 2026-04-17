package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_translations", indexes = {
        @Index(name = "idx_product_translations_locale", columnList = "locale")})
public class ProductTranslationEntity {

    @EmbeddedId
    private ProductTranslationId id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
}
