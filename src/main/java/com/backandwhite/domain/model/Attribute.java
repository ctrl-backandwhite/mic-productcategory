package com.backandwhite.domain.model;

import com.backandwhite.domain.valureobject.AttributeType;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    private String id;
    private String name;
    private String slug;
    private AttributeType type;
    private Long usedInProducts;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<AttributeValue> values = new ArrayList<>();
}
