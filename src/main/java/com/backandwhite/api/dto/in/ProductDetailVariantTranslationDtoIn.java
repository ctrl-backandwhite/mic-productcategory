package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Variant translation")
public class ProductDetailVariantTranslationDtoIn {

    @NotBlank(message = "Locale is required")
    @Schema(description = "Language code", example = "en")
    private String locale;

    @Schema(description = "Translated variant name")
    private String variantName;
}
