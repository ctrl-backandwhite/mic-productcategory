package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para carga masiva de variantes (CSV / JSON)")
public class BulkVariantDtoIn {

    @NotNull(message = "Las variantes son obligatorias")
    @Size(min = 1, message = "Se requiere al menos una variante")
    @Valid
    @Schema(description = "Lista de variantes a crear")
    private List<ProductDetailVariantDtoIn> rows;
}
