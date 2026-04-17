package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_detail_translations", indexes = {
        @Index(name = "idx_detail_translations_locale", columnList = "locale")})
public class ProductDetailTranslationEntity {

    @EmbeddedId
    private ProductDetailTranslationId id;

    @Column(name = "product_name", length = 500)
    private String productName;

    @Column(name = "entry_name", length = 255)
    private String entryName;

    @Column(name = "material_name", columnDefinition = "TEXT")
    private String materialName;

    @Column(name = "packing_name", columnDefinition = "TEXT")
    private String packingName;

    @Column(name = "product_key", length = 255)
    private String productKey;

    @Column(name = "product_pro", columnDefinition = "TEXT")
    private String productPro;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pid")
    @JoinColumn(name = "pid", nullable = false)
    private ProductDetailEntity productDetail;
}
