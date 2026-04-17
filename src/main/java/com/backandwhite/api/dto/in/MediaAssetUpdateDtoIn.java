package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.MediaCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating media asset metadata")
public class MediaAssetUpdateDtoIn {

    @NotNull(message = "Category is required")
    @Schema(description = "Asset category", example = "PRODUCT")
    private MediaCategory category;

    @Schema(description = "Alternative text for accessibility", example = "Nike Air Max Product")
    private String alt;

    @Schema(description = "Asset tags")
    private List<String> tags;
}
