package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_detail_variant_translations", indexes = {
        @Index(name = "idx_detail_variant_trans_locale", columnList = "locale")})
public class ProductDetailVariantTranslationEntity {

    @EmbeddedId
    private ProductDetailVariantTranslationId id;

    @Column(name = "variant_name", length = 500)
    private String variantName;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vid")
    @JoinColumn(name = "vid", nullable = false)
    private ProductDetailVariantEntity variant;
}
