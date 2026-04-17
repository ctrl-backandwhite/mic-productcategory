package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Variant inventory by country")
public class ProductDetailVariantInventoryDtoOut {

    @Schema(description = "Inventory ID")
    private Long id;

    @Schema(description = "Country code", example = "US")
    private String countryCode;

    @Schema(description = "Total inventory")
    private Integer totalInventory;

    @Schema(description = "CJ inventory")
    private Integer cjInventory;

    @Schema(description = "Factory inventory")
    private Integer factoryInventory;

    @Schema(description = "Verified warehouse")
    private Integer verifiedWarehouse;
}
