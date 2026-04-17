package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.TaxType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tax rule by country")
public class CountryTaxDtoIn {

    @NotBlank
    @Schema(description = "ISO country code (2-3 characters)", example = "ES")
    private String country;

    @Schema(description = "State or region (null = entire country)", example = "Catalonia")
    private String region;

    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "Tax rate (decimal, 0.21 = 21%)", example = "0.21")
    private BigDecimal rate;

    @NotNull
    @Schema(description = "Tax type: PERCENTAGE or FIXED", example = "PERCENTAGE")
    private TaxType type;

    @Schema(description = "Categories to which it applies", example = "[\"General\"]")
    private List<String> appliesToCategories;

    @Schema(description = "Whether the rule is active", example = "true")
    private Boolean active;
}
