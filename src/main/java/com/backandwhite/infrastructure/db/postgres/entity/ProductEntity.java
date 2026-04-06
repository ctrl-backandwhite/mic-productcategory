package com.backandwhite.infrastructure.db.postgres.entity;

import com.backandwhite.common.infrastructure.entity.AuditableEntity;
import com.backandwhite.domain.valueobject.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@With
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products", indexes = {
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_sku", columnList = "sku")
})
public class ProductEntity extends AuditableEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "sku", length = 64)
    private String sku;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "brand_id", length = 64)
    private String brandId;

    @Column(name = "warranty_id", length = 64)
    private String warrantyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "big_image", columnDefinition = "TEXT")
    private String bigImage;

    @Column(name = "product_image_set", columnDefinition = "TEXT")
    private String productImageSet;

    @Column(name = "sell_price", length = 50)
    private String sellPrice;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "listed_num")
    @Builder.Default
    private Integer listedNum = 0;

    @Column(name = "warehouse_inventory_num")
    @Builder.Default
    private Integer warehouseInventoryNum = 0;

    @Column(name = "is_video")
    @Builder.Default
    private Boolean isVideo = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CategoryEntity category;

    @ToString.Exclude
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductTranslationEntity> translations = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", referencedColumnName = "id", insertable = false, updatable = false)
    private List<ProductDetailVariantEntity> variants = new ArrayList<>();
}
