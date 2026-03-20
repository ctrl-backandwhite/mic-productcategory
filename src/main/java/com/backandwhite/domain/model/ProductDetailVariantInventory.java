package com.backandwhite.domain.model;

import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVariantInventory {

    private Long id;
    private String vid;
    private String countryCode;
    private Integer totalInventory;
    private Integer cjInventory;
    private Integer factoryInventory;
    private Integer verifiedWarehouse;
}
