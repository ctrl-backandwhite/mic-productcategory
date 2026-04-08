package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valueobject.TaxType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Regla de impuesto por país")
public class CountryTaxDtoIn {

    @NotBlank
    @Schema(description = "Código ISO del país (2-3 caracteres)", example = "ES")
    private String country;

    @Schema(description = "Estado o región (null = todo el país)", example = "Cataluña")
    private String region;

    @NotNull
    @DecimalMin("0.00")
    @Schema(description = "Tasa de impuesto (decimal, 0.21 = 21%)", example = "0.21")
    private BigDecimal rate;

    @NotNull
    @Schema(description = "Tipo de impuesto: PERCENTAGE o FIXED", example = "PERCENTAGE")
    private TaxType type;

    @Schema(description = "Categorías a las que aplica", example = "[\"General\"]")
    private List<String> appliesToCategories;

    @Schema(description = "Si la regla está activa", example = "true")
    private Boolean active;
}
