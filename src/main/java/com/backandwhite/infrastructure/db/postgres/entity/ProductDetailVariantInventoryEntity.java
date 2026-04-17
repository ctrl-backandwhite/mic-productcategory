package com.backandwhite.infrastructure.db.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@With
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_detail_variant_inventories", indexes = {@Index(name = "idx_detail_inv_vid", columnList = "vid")})
public class ProductDetailVariantInventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "vid", length = 64, nullable = false, insertable = false, updatable = false)
    private String vid;

    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Column(name = "total_inventory")
    private Integer totalInventory;

    @Column(name = "cj_inventory")
    private Integer cjInventory;

    @Column(name = "factory_inventory")
    private Integer factoryInventory;

    @Column(name = "verified_warehouse")
    private Integer verifiedWarehouse;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vid", nullable = false)
    private ProductDetailVariantEntity variant;
}
