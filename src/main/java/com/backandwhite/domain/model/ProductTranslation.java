package com.backandwhite.domain.model;

import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTranslation {

    private String locale;
    private String name;
}
