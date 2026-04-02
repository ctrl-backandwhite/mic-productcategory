package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una marca")
public class BrandDtoIn {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Schema(description = "Nombre de la marca", example = "Nike")
    private String name;

    @NotBlank(message = "El slug es obligatorio")
    @Size(max = 255, message = "El slug no puede exceder 255 caracteres")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "El slug debe ser alfanumérico en minúsculas separado por guiones")
    @Schema(description = "Slug URL-friendly de la marca", example = "nike")
    private String slug;

    @Size(max = 500, message = "La URL del logo no puede exceder 500 caracteres")
    @Schema(description = "URL del logotipo de la marca", example = "https://cdn.example.com/logos/nike.png")
    private String logoUrl;

    @Size(max = 500, message = "La URL del sitio web no puede exceder 500 caracteres")
    @Schema(description = "Sitio web oficial de la marca", example = "https://www.nike.com")
    private String websiteUrl;

    @Schema(description = "Descripción de la marca", example = "Marca líder en calzado y ropa deportiva")
    private String description;
}
