package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Inventario de variante por país")
public class ProductDetailVariantInventoryDtoOut {

    @Schema(description = "ID del inventario")
    private Long id;

    @Schema(description = "Código de país", example = "US")
    private String countryCode;

    @Schema(description = "Inventario total")
    private Integer totalInventory;

    @Schema(description = "Inventario CJ")
    private Integer cjInventory;

    @Schema(description = "Inventario de fábrica")
    private Integer factoryInventory;

    @Schema(description = "Almacén verificado")
    private Integer verifiedWarehouse;
}
