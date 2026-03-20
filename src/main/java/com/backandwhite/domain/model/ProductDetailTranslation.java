package com.backandwhite.domain.model;

import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailTranslation {

    private String locale;
    private String productName;
    private String entryName;
    private String materialName;
    private String packingName;
    private String productKey;
    private String productPro;
}
