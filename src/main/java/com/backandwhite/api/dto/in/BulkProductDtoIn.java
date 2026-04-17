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
@Schema(description = "DTO for bulk product upload (CSV / JSON)")
public class BulkProductDtoIn {

    @NotNull(message = "Products are required")
    @Size(min = 1, message = "At least one product is required")
    @Valid
    @Schema(description = "List of products to create")
    private List<ProductDtoIn> rows;
}
