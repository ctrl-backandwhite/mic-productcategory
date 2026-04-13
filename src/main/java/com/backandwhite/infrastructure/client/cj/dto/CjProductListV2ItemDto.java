package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single product item from CJ's listV2 response.
 * Field names match CJ's actual API response (e.g. "id", "nameEn").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjProductListV2ItemDto {

    private String id;
    private String nameEn;
    private String sku;
    private String spu;
    private String bigImage;
    private String sellPrice;
    private String nowPrice;
    private String discountPrice;
    private String discountPriceRate;
    private Integer listedNum;
    private String categoryId;
    private String threeCategoryName;
    private String twoCategoryId;
    private String twoCategoryName;
    private String oneCategoryId;
    private String oneCategoryName;
    private Integer addMarkStatus;
    private Integer isVideo;
    private String productType;
    private String supplierName;
    private Long createAt;
    private Integer warehouseInventoryNum;
}
