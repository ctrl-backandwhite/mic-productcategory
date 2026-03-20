package com.backandwhite.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetail {

    private String pid;
    private String productNameEn;
    private String productSku;
    private String bigImage;
    private String productImage;
    private String productImageSet;
    private String productWeight;
    private String productUnit;
    private String productType;
    private String categoryId;
    private String categoryName;
    private String entryCode;
    private String entryNameEn;
    private String materialNameEn;
    private String materialKey;
    private String packingWeight;
    private String packingNameEn;
    private String packingKey;
    private String productKeyEn;
    private String productProEn;
    private String sellPrice;
    private String description;
    private String suggestSellPrice;
    private Integer listedNum;
    private String status;
    private String supplierName;
    private String supplierId;
    private Instant createrTime;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<ProductDetailTranslation> translations = new ArrayList<>();

    @Builder.Default
    private List<ProductDetailVariant> variants = new ArrayList<>();
}
