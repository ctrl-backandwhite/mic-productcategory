package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product detail translation")
public class ProductDetailTranslationDtoOut {

    @Schema(description = "Language code", example = "en")
    private String locale;

    @Schema(description = "Translated product name")
    private String productName;

    @Schema(description = "Translated entry name")
    private String entryName;

    @Schema(description = "Translated material name")
    private String materialName;

    @Schema(description = "Translated packing name")
    private String packingName;

    @Schema(description = "Translated product key")
    private String productKey;

    @Schema(description = "Translated product properties")
    private String productPro;
}
