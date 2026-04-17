package com.backandwhite.api.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tax rule by country")
public class CountryTaxDtoOut {

    @Schema(description = "Rule ID")
    private String id;

    @Schema(description = "ISO country code")
    private String country;

    @Schema(description = "State or region")
    private String region;

    @Schema(description = "Tax rate (decimal)", example = "0.21")
    private BigDecimal rate;

    @Schema(description = "Type: PERCENTAGE or FIXED")
    private String type;

    @Schema(description = "Categories to which it applies")
    private List<String> appliesToCategories;

    @Schema(description = "Whether the rule is active")
    private Boolean active;

    @Schema(description = "Creation date")
    private Instant createdAt;

    @Schema(description = "Last update date")
    private Instant updatedAt;
}
