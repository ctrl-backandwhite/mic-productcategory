package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product translation")
public class ProductTranslationDtoOut {

    @Schema(description = "Language code", example = "es")
    private String locale;

    @Schema(description = "Translated name", example = "Cotton T-shirt")
    private String name;
}
