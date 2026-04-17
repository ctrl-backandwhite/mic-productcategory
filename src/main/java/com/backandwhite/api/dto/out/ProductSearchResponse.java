package com.backandwhite.api.dto.out;

import java.util.List;
import java.util.Map;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {

    private List<ProductSearchHit> results;
    private long totalHits;
    private int page;
    private int size;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSearchHit {
        private String id;
        private String pid;
        private String name;
        private String description;
        private String categoryName;
        private String brandName;
        private Float price;
        private Float originalPrice;
        private Boolean inStock;
        private Integer totalStock;
        private String imageUrl;
        private String status;
        private Map<String, List<String>> highlights;
    }
}
