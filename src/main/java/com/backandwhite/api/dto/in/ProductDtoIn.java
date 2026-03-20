package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar un producto")
public class ProductDtoIn {

    @Schema(description = "SKU del producto", example = "SKU-001")
    private String sku;

    @NotNull(message = "El categoryId es obligatorio")
    @Schema(description = "ID de la categoría a la que pertenece", example = "cat-001")
    private String categoryId;

    @Schema(description = "URL de la imagen principal")
    private String bigImage;

    @Schema(description = "Precio de venta", example = "19.99")
    private String sellPrice;

    @Schema(description = "Tipo de producto (ej: NORMAL)", example = "NORMAL")
    private String productType;

    @Schema(description = "Cantidad listada", example = "100")
    private Integer listedNum;

    @Schema(description = "Inventario en almacén", example = "500")
    private Integer warehouseInventoryNum;

    @Schema(description = "Descripción del producto")
    private String description;

    @Schema(description = "URLs de imágenes del producto (separadas por coma)")
    private String productImageSet;

    @Schema(description = "Tiene video", example = "false")
    private Boolean isVideo;

    @NotNull(message = "Las traducciones son obligatorias")
    @Size(min = 1, message = "Se requiere al menos una traducción")
    @Valid
    @Schema(description = "Traducciones del producto")
    private List<ProductTranslationDtoIn> translations;
}
