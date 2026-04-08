package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.TaxType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Regla de impuesto por país")
public class CountryTaxDtoOut {

    @Schema(description = "ID de la regla")
    private String id;

    @Schema(description = "Código ISO del país")
    private String country;

    @Schema(description = "Estado o región")
    private String region;

    @Schema(description = "Tasa de impuesto (decimal)", example = "0.21")
    private BigDecimal rate;

    @Schema(description = "Tipo: PERCENTAGE o FIXED")
    private String type;

    @Schema(description = "Categorías a las que aplica")
    private List<String> appliesToCategories;

    @Schema(description = "Si la regla está activa")
    private Boolean active;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de actualización")
    private Instant updatedAt;
}
