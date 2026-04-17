package com.backandwhite.api.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a product review")
public class ReviewDtoIn {

    @NotBlank(message = "Author name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Author name", example = "John Smith")
    private String authorName;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Minimum rating is 1")
    @Max(value = 5, message = "Maximum rating is 5")
    @Schema(description = "Rating from 1 to 5 stars", example = "4")
    private Integer rating;

    @Size(max = 500, message = "Title must not exceed 500 characters")
    @Schema(description = "Review title", example = "Excellent product")
    private String title;

    @Schema(description = "Review body", example = "The quality exceeded my expectations...")
    private String body;

    @Schema(description = "Attached image URLs")
    private List<String> images;
}
