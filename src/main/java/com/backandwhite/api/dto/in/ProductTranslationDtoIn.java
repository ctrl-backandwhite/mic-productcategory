package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product name translation")
public class ProductTranslationDtoIn {

    @NotBlank(message = "Locale is required")
    @Schema(description = "Language code (e.g.: es, en, pt-BR)", example = "es")
    private String locale;

    @NotBlank(message = "Name is required")
    @Schema(description = "Product name in the specified language", example = "Cotton T-shirt")
    private String name;
}
