package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjVariantDetailDto {

    private String vid;
    private String pid;
    private String variantNameEn;
    private String variantSku;
    private String variantImage;
    private String variantKey;
    private String variantUnit;
    private BigDecimal variantLength;
    private BigDecimal variantWidth;
    private BigDecimal variantHeight;
    private BigDecimal variantVolume;
    private BigDecimal variantWeight;
    private BigDecimal variantSellPrice;
    private BigDecimal variantSugSellPrice;
    private String variantStandard;
    private String createTime;
    private List<CjInventoryDto> inventories;
}
