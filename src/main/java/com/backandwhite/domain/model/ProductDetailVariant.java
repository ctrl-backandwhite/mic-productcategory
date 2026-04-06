package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVariant {

    private String vid;
    private String pid;
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;
    private String variantNameEn;
    private String variantSku;
    private String variantUnit;
    private String variantKey;
    private String variantImage;
    private BigDecimal variantLength;
    private BigDecimal variantWidth;
    private BigDecimal variantHeight;
    private BigDecimal variantVolume;
    private BigDecimal variantWeight;
    private BigDecimal variantSellPrice;
    private BigDecimal variantSugSellPrice;
    private String variantStandard;
    private Instant createTime;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<ProductDetailVariantTranslation> translations = new ArrayList<>();

    @Builder.Default
    private List<ProductDetailVariantInventory> inventories = new ArrayList<>();
}
