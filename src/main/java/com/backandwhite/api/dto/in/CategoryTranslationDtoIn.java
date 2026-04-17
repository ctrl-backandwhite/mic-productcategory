package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category name translation")
public class CategoryTranslationDtoIn {

    @NotBlank(message = "Locale is required")
    @Schema(description = "Language code (e.g.: es, en, pt-BR)", example = "es")
    private String locale;

    @NotBlank(message = "Name is required")
    @Schema(description = "Category name in the specified language", example = "Electronics")
    private String name;
}
