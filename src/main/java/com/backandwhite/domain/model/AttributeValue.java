package com.backandwhite.domain.model;

import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue {

    private String id;
    private String attributeId;
    private String value;
    private String colorHex;
    private Integer position;
}
