package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjProductDetailDto {

    private String pid;
    private String productNameEn;
    private String productSku;
    private String bigImage;
    private String productImage;
    private List<String> productImageSet;
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
    private String status;
    private Integer listedNum;
    private String supplierName;
    private String supplierId;
    private String createrTime;
    private List<CjVariantDetailDto> variants;
}
