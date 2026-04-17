package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product detail variant translation")
public class ProductDetailVariantTranslationDtoOut {

    @Schema(description = "Language code", example = "en")
    private String locale;

    @Schema(description = "Translated variant name")
    private String variantName;
}
