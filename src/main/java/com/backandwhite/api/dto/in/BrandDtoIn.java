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
@Schema(description = "DTO for creating or updating a brand")
public class BrandDtoIn {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Brand name", example = "Nike")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*+$", message = "Slug must be lowercase alphanumeric separated by hyphens")
    @Schema(description = "URL-friendly brand slug", example = "nike")
    private String slug;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    @Schema(description = "Brand logo URL", example = "https://cdn.example.com/logos/nike.png")
    private String logoUrl;

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    @Schema(description = "Brand official website", example = "https://www.nike.com")
    private String websiteUrl;

    @Schema(description = "Brand description", example = "Leading brand in footwear and sportswear")
    private String description;
}
