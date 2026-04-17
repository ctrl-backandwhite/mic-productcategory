package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category translation")
public class CategoryTranslationDtoOut {

    @Schema(description = "Language code", example = "es")
    private String locale;

    @Schema(description = "Translated name", example = "Electronics")
    private String name;
}
