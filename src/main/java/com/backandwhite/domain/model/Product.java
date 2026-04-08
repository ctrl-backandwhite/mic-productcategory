package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.ProductStatus;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String id;
    private String sku;
    private String categoryId;
    private String name;
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;
    private String bigImage;
    private String productImageSet;
    private String sellPrice;
    private String costPrice;
    private String productType;
    private String description;
    private Integer listedNum;
    private Integer warehouseInventoryNum;
    private Boolean isVideo;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<ProductTranslation> translations = new ArrayList<>();

    @Builder.Default
    private List<ProductDetailVariant> variants = new ArrayList<>();
}
