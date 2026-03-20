package com.backandwhite.domain.model;

import com.backandwhite.domain.valureobject.CategoryStatus;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    private List<CategoryTranslation> translations = new ArrayList<>();

    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();
}
