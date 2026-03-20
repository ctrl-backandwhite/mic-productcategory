package com.backandwhite.domain.model;

import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVariantTranslation {

    private String locale;
    private String variantName;
}
