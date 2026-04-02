package com.backandwhite.api.dto.in;

import com.backandwhite.domain.valureobject.WarrantyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una garantía")
public class WarrantyDtoIn {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Schema(description = "Nombre del plan de garantía", example = "Garantía Extendida Premium")
    private String name;

    @NotNull(message = "El tipo es obligatorio")
    @Schema(description = "Tipo de garantía", example = "EXTENDED")
    private WarrantyType type;

    @NotNull(message = "La duración en meses es obligatoria")
    @Min(value = 1, message = "La duración mínima es 1 mes")
    @Schema(description = "Duración en meses", example = "24")
    private Integer durationMonths;

    @Schema(description = "Cobertura del plan", example = "Cubre defectos de fabricación y fallos de hardware")
    private String coverage;

    @Schema(description = "Condiciones del plan", example = "No cubre daños por agua o caídas")
    private String conditions;

    @Schema(description = "Incluye mano de obra", example = "true")
    private Boolean includesLabor;

    @Schema(description = "Incluye piezas", example = "true")
    private Boolean includesParts;

    @Schema(description = "Incluye recogida a domicilio", example = "false")
    private Boolean includesPickup;

    @Schema(description = "Límite de reparaciones", example = "3")
    private Integer repairLimit;

    @Size(max = 32, message = "El teléfono no puede exceder 32 caracteres")
    @Schema(description = "Teléfono de contacto", example = "+34 900 123 456")
    private String contactPhone;

    @Email(message = "Email no válido")
    @Schema(description = "Email de contacto", example = "garantias@example.com")
    private String contactEmail;
}
