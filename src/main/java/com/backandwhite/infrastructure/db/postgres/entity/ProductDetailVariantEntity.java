package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.common.domain.valueobject.MoneyConverter;
import com.backandwhite.common.infrastructure.entity.AuditableEntity;
import com.backandwhite.domain.valueobject.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@With
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_detail_variants", indexes = {
        @Index(name = "idx_detail_variants_pid", columnList = "pid"),
        @Index(name = "idx_detail_variants_sku", columnList = "variant_sku")
})
public class ProductDetailVariantEntity extends AuditableEntity {

    @Id
    @Column(name = "vid", length = 64, nullable = false)
    private String vid;

    @Column(name = "pid", length = 64, nullable = false, insertable = false, updatable = false)
    private String pid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "variant_name_en", length = 500)
    private String variantNameEn;

    @Column(name = "variant_sku", length = 64)
    private String variantSku;

    @Column(name = "variant_unit", length = 20)
    private String variantUnit;

    @Column(name = "variant_key", length = 255)
    private String variantKey;

    @Column(name = "variant_image", columnDefinition = "TEXT")
    private String variantImage;

    @Column(name = "variant_length", precision = 10, scale = 2)
    private BigDecimal variantLength;

    @Column(name = "variant_width", precision = 10, scale = 2)
    private BigDecimal variantWidth;

    @Column(name = "variant_height", precision = 10, scale = 2)
    private BigDecimal variantHeight;

    @Column(name = "variant_volume", precision = 10, scale = 2)
    private BigDecimal variantVolume;

    @Column(name = "variant_weight", precision = 10, scale = 2)
    private BigDecimal variantWeight;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "variant_sell_price", precision = 10, scale = 2)
    private Money variantSellPrice;

    @Convert(converter = MoneyConverter.class)
    @Column(name = "variant_sug_sell_price", precision = 10, scale = 2)
    private Money variantSugSellPrice;

    @Column(name = "variant_standard", length = 255)
    private String variantStandard;

    @Column(name = "create_time")
    private Instant createTime;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", nullable = false)
    private ProductDetailEntity productDetail;

    @ToString.Exclude
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductDetailVariantTranslationEntity> translations = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductDetailVariantInventoryEntity> inventories = new ArrayList<>();
}
