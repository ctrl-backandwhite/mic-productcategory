package com.backandwhite.api.dto.out;

import com.backandwhite.domain.valueobject.WarrantyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Plan de garantía")
public class WarrantyDtoOut {

    @Schema(description = "ID de la garantía")
    private String id;

    @Schema(description = "Nombre del plan")
    private String name;

    @Schema(description = "Tipo de garantía")
    private WarrantyType type;

    @Schema(description = "Duración en meses")
    private Integer durationMonths;

    @Schema(description = "Cobertura")
    private String coverage;

    @Schema(description = "Condiciones")
    private String conditions;

    @Schema(description = "Incluye mano de obra")
    private Boolean includesLabor;

    @Schema(description = "Incluye piezas")
    private Boolean includesParts;

    @Schema(description = "Incluye recogida a domicilio")
    private Boolean includesPickup;

    @Schema(description = "Límite de reparaciones")
    private Integer repairLimit;

    @Schema(description = "Teléfono de contacto")
    private String contactPhone;

    @Schema(description = "Email de contacto")
    private String contactEmail;

    @Schema(description = "Estado activo/inactivo")
    private Boolean active;

    @Schema(description = "Cantidad de productos asociados")
    private Long productsCount;

    @Schema(description = "Fecha de creación")
    private Instant createdAt;

    @Schema(description = "Fecha de última actualización")
    private Instant updatedAt;
}
