package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal payload used by the fine-grained PATCH endpoints that link a product
 * to a brand / warranty / category. A {@code null} value detaches the current
 * association (where applicable).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single-id payload for linking a product to another entity")
public class ProductLinkDtoIn {

    @Schema(description = "Target entity id (brand, warranty or category). Pass null to detach.")
    private String id;
}
