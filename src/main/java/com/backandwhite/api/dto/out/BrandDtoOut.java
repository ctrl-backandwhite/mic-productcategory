package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.BrandStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Brand with full details")
public class BrandDtoOut {

    @Schema(description = "Brand ID")
    private String id;

    @Schema(description = "Brand name", example = "Nike")
    private String name;

    @Schema(description = "URL-friendly slug", example = "nike")
    private String slug;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Official website")
    private String websiteUrl;

    @Schema(description = "Brand description")
    private String description;

    @Schema(description = "Brand status (ACTIVE, INACTIVE)")
    private BrandStatus status;

    @Schema(description = "Number of associated products")
    private Long productCount;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;
}
