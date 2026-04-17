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
@Schema(description = "A CSV bulk upload row (level 1 required, levels 2 and 3 optional)")
public class BulkCategoryRowDtoIn {

    @NotNull(message = "Level 1 translations are required")
    @Size(min = 1, message = "At least one level 1 translation is required")
    @Valid
    @Schema(description = "Translations for the level 1 category")
    private List<CategoryTranslationDtoIn> level1Translations;

    @Valid
    @Schema(description = "Translations for the level 2 subcategory (optional)")
    private List<CategoryTranslationDtoIn> level2Translations;

    @Valid
    @Schema(description = "Translations for the level 3 subcategory (optional)")
    private List<CategoryTranslationDtoIn> level3Translations;
}
