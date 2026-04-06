package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.BrandStatus;
import lombok.*;

import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    private String id;
    private String name;
    private String slug;
    private String logoUrl;
    private String websiteUrl;
    private String description;
    private BrandStatus status;
    private Long productCount;
    private Instant createdAt;
    private Instant updatedAt;
}
