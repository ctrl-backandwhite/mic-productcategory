package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category with its translated subcategories")
public class CategoryDtoOut {

    @Schema(description = "Category ID", example = "electronics")
    private String id;

    @Schema(description = "Parent ID", example = "root-id")
    private String parentId;

    @Schema(description = "Translated category name", example = "Electronics")
    private String name;

    @Schema(description = "Hierarchy level (1=root, 2=sub, 3=sub-sub)", example = "1")
    private Integer level;

    @Schema(description = "Publication status (DRAFT, PUBLISHED)", example = "PUBLISHED")
    private CategoryStatus status;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean active;

    @Schema(description = "Whether the category is featured", example = "false")
    private Boolean featured;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;

    @Schema(description = "Category translations")
    private List<CategoryTranslationDtoOut> translations;

    @Schema(description = "Subcategories of this category")
    private List<CategoryDtoOut> subCategories;
}
