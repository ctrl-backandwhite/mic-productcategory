package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.infrastructure.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
@Table(name = "product_details")
public class ProductDetailEntity extends AuditableEntity {

    @Id
    @Column(name = "pid", length = 64, nullable = false)
    private String pid;

    @Column(name = "product_name_en", length = 500)
    private String productNameEn;

    @Column(name = "product_sku", length = 64)
    private String productSku;

    @Column(name = "big_image", columnDefinition = "TEXT")
    private String bigImage;

    @Column(name = "product_image", columnDefinition = "TEXT")
    private String productImage;

    @Column(name = "product_image_set", columnDefinition = "TEXT")
    private String productImageSet;

    @Column(name = "product_weight", length = 50)
    private String productWeight;

    @Column(name = "product_unit", length = 50)
    private String productUnit;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "category_name", columnDefinition = "TEXT")
    private String categoryName;

    @Column(name = "entry_code", length = 50)
    private String entryCode;

    @Column(name = "entry_name_en", length = 255)
    private String entryNameEn;

    @Column(name = "material_name_en", columnDefinition = "TEXT")
    private String materialNameEn;

    @Column(name = "material_key", columnDefinition = "TEXT")
    private String materialKey;

    @Column(name = "packing_weight", length = 50)
    private String packingWeight;

    @Column(name = "packing_name_en", columnDefinition = "TEXT")
    private String packingNameEn;

    @Column(name = "packing_key", columnDefinition = "TEXT")
    private String packingKey;

    @Column(name = "product_key_en", length = 255)
    private String productKeyEn;

    @Column(name = "product_pro_en", columnDefinition = "TEXT")
    private String productProEn;

    @Column(name = "sell_price", length = 50)
    private String sellPrice;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "suggest_sell_price", length = 50)
    private String suggestSellPrice;

    @Column(name = "listed_num")
    @Builder.Default
    private Integer listedNum = 0;

    @Column(name = "status", length = 10)
    private String status;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "supplier_id", length = 64)
    private String supplierId;

    @Column(name = "creater_time")
    private Instant createrTime;

    @ToString.Exclude
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductDetailTranslationEntity> translations = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductDetailVariantEntity> variants = new ArrayList<>();
}
