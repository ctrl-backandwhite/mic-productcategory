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
@Schema(description = "DTO para carga masiva de productos (CSV / JSON)")
public class BulkProductDtoIn {

    @NotNull(message = "Los productos son obligatorios")
    @Size(min = 1, message = "Se requiere al menos un producto")
    @Valid
    @Schema(description = "Lista de productos a crear")
    private List<ProductDtoIn> rows;
}
