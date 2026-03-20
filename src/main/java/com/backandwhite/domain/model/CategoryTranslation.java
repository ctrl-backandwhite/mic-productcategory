package com.backandwhite.domain.model;

import lombok.*;

import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTranslation {

    private String locale;
    private String name;
}
