package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjInventoryDto {

    private String countryCode;
    private Integer totalInventory;
    private Integer cjInventory;
    private Integer factoryInventory;
    private Integer verifiedWarehouse;
}
