package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.AttributeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Attribute with its values")
public class AttributeDtoOut {

    @Schema(description = "Attribute ID")
    private String id;

    @Schema(description = "Attribute name", example = "Color")
    private String name;

    @Schema(description = "URL-friendly slug", example = "color")
    private String slug;

    @Schema(description = "Attribute type (TEXT, COLOR, SIZE, SELECT)")
    private AttributeType type;

    @Schema(description = "Number of products using this attribute")
    private Long usedInProducts;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;

    @Schema(description = "Attribute values")
    private List<AttributeValueDtoOut> values;
}
