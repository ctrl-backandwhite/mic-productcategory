package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating or updating a category")
public class CategoryDtoIn {

    @Schema(description = "Parent ID (null for root categories)", example = "electronics")
    private String parentId;

    @NotNull(message = "Level is required")
    @Schema(description = "Hierarchical level (1=root, 2=sub, 3=sub-sub)", example = "1")
    private Integer level;

    @NotNull(message = "Translations are required")
    @Size(min = 1, message = "At least one translation is required")
    @Valid
    @Schema(description = "Category name translations")
    private List<CategoryTranslationDtoIn> translations;
}
