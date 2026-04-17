package com.backandwhite.infrastructure.search.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/product-settings.json")
public class ProductDocument {

        @Id
        private String id;

        @Field(type = FieldType.Keyword)
        private String pid;

        @Field(type = FieldType.Keyword)
        private String sku;

        @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "product_analyzer", searchAnalyzer = "search_analyzer"), otherFields = {
                        @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "search_analyzer"),
                        @InnerField(suffix = "keyword", type = FieldType.Keyword)
        })
        private String name;

        @Field(type = FieldType.Text, analyzer = "product_analyzer")
        private String description;

        @Field(type = FieldType.Keyword)
        private String categoryId;

        @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "product_analyzer"), otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
        private String categoryName;

        @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "product_analyzer"), otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
        private String brandName;

        @Field(type = FieldType.Keyword)
        private String brandSlug;

        @Field(type = FieldType.Keyword)
        private String status;

        @Field(type = FieldType.Float)
        private Float price;

        @Field(type = FieldType.Float)
        private Float originalPrice;

        @Field(type = FieldType.Boolean)
        private Boolean hasDiscount;

        @Field(type = FieldType.Float)
        private Float discountPercent;

        @Field(type = FieldType.Integer)
        private Integer totalStock;

        @Field(type = FieldType.Boolean)
        private Boolean inStock;

        @Field(type = FieldType.Keyword, index = false)
        private String imageUrl;

        @Field(type = FieldType.Keyword)
        private List<String> tags;

        @Field(type = FieldType.Nested)
        private List<VariantDocument> variants;

        @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
        private Instant createdAt;

        @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
        private Instant updatedAt;

        @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
        private Instant syncedAt;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VariantDocument {

                @Field(type = FieldType.Keyword)
                private String vid;

                @Field(type = FieldType.Keyword)
                private String sku;

                @Field(type = FieldType.Text, analyzer = "product_analyzer")
                private String name;

                @Field(type = FieldType.Float)
                private Float price;

                @Field(type = FieldType.Integer)
                private Integer stock;

                @Field(type = FieldType.Boolean)
                private Boolean inStock;
        }
}
