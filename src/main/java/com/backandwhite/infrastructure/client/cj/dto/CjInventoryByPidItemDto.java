package com.backandwhite.infrastructure.client.cj.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Individual inventory item returned by /product/stock/getInventoryByPid. Maps
 * to one warehouse/country entry for a specific variant (vid).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CjInventoryByPidItemDto {

    private String vid;
    private String sku;
    private Integer totalInventory;
    private Integer cjInventory;
    private Integer factoryInventory;
    private String countryCode;
}
