package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.CategoryStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private String id;
    private String parentId;
    private Integer level;
    private String name;
    private CategoryStatus status;
    private Boolean active;
    private Boolean featured;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastDiscoveredAt;

    @Builder.Default
    private List<CategoryTranslation> translations = new ArrayList<>();

    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();
}
