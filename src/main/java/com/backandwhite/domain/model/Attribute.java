package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.AttributeType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

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
