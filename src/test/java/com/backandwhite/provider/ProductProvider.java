package com.backandwhite.provider;

import com.backandwhite.api.dto.in.ProductDtoIn;
import com.backandwhite.api.dto.in.ProductTranslationDtoIn;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.dto.out.ProductTranslationDtoOut;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductTranslation;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;

import java.util.List;

public final class ProductProvider {

        public static final String PRODUCT_ID = "prod-shirt-001";
        public static final String PRODUCT_SKU = "SKU-SHIRT-001";
        public static final String PRODUCT_NAME_ES = "Camiseta de algodón";
        public static final String PRODUCT_NAME_EN = "Cotton T-Shirt";
        public static final String PRODUCT_NAME_PT = "Camiseta de algodão";
        public static final String PRODUCT_SELL_PRICE = "19.99";
        public static final String PRODUCT_TYPE = "ORDINARY_PRODUCT";
        public static final Integer PRODUCT_LISTED_NUM = 100;
        public static final Integer PRODUCT_WAREHOUSE_NUM = 500;
        public static final Boolean PRODUCT_IS_VIDEO = false;

        public static final String OTHER_PRODUCT_ID = "prod-shoes-002";
        public static final String OTHER_PRODUCT_SKU = "SKU-SHOES-002";
        public static final String OTHER_PRODUCT_NAME_ES = "Zapatillas deportivas";
        public static final String OTHER_PRODUCT_SELL_PRICE = "29.99";

        private ProductProvider() {
        }

        public static Product product(String categoryId) {
                return Product.builder()
                                .id(PRODUCT_ID)
                                .sku(PRODUCT_SKU)
                                .categoryId(categoryId)
                                .name(PRODUCT_NAME_ES)
                                .status(ProductStatus.DRAFT)
                                .sellPrice(PRODUCT_SELL_PRICE)
                                .productType(PRODUCT_TYPE)
                                .listedNum(PRODUCT_LISTED_NUM)
                                .warehouseInventoryNum(PRODUCT_WAREHOUSE_NUM)
                                .isVideo(PRODUCT_IS_VIDEO)
                                .createdAt(AuditProvider.CREATED_AT)
                                .updatedAt(AuditProvider.UPDATED_AT)
                                .translations(List.of(
                                                ProductTranslation.builder().locale("es").name(PRODUCT_NAME_ES).build(),
                                                ProductTranslation.builder().locale("en").name(PRODUCT_NAME_EN).build(),
                                                ProductTranslation.builder().locale("pt-BR").name(PRODUCT_NAME_PT)
                                                                .build()))
                                .build();
        }

        public static Product otherProduct(String categoryId) {
                return Product.builder()
                                .id(OTHER_PRODUCT_ID)
                                .sku(OTHER_PRODUCT_SKU)
                                .categoryId(categoryId)
                                .name(OTHER_PRODUCT_NAME_ES)
                                .status(ProductStatus.DRAFT)
                                .sellPrice(OTHER_PRODUCT_SELL_PRICE)
                                .productType(PRODUCT_TYPE)
                                .listedNum(50)
                                .warehouseInventoryNum(200)
                                .isVideo(PRODUCT_IS_VIDEO)
                                .createdAt(AuditProvider.CREATED_AT)
                                .updatedAt(AuditProvider.UPDATED_AT)
                                .translations(List.of(
                                                ProductTranslation.builder().locale("es").name(OTHER_PRODUCT_NAME_ES)
                                                                .build()))
                                .build();
        }

        public static ProductEntity productEntity(String categoryId) {
                return ProductEntity.builder()
                                .id(PRODUCT_ID)
                                .sku(PRODUCT_SKU)
                                .categoryId(categoryId)
                                .status(ProductStatus.DRAFT)
                                .sellPrice(PRODUCT_SELL_PRICE)
                                .productType(PRODUCT_TYPE)
                                .listedNum(PRODUCT_LISTED_NUM)
                                .warehouseInventoryNum(PRODUCT_WAREHOUSE_NUM)
                                .isVideo(PRODUCT_IS_VIDEO)
                                .build();
        }

        public static ProductDtoIn productDtoIn(String categoryId) {
                return ProductDtoIn.builder()
                                .sku(PRODUCT_SKU)
                                .categoryId(categoryId)
                                .sellPrice(PRODUCT_SELL_PRICE)
                                .productType(PRODUCT_TYPE)
                                .listedNum(PRODUCT_LISTED_NUM)
                                .warehouseInventoryNum(PRODUCT_WAREHOUSE_NUM)
                                .isVideo(PRODUCT_IS_VIDEO)
                                .translations(List.of(
                                                ProductTranslationDtoIn.builder().locale("es").name(PRODUCT_NAME_ES)
                                                                .build(),
                                                ProductTranslationDtoIn.builder().locale("en").name(PRODUCT_NAME_EN)
                                                                .build(),
                                                ProductTranslationDtoIn.builder().locale("pt-BR").name(PRODUCT_NAME_PT)
                                                                .build()))
                                .build();
        }

        public static ProductDtoIn otherProductDtoIn(String categoryId) {
                return ProductDtoIn.builder()
                                .sku(OTHER_PRODUCT_SKU)
                                .categoryId(categoryId)
                                .sellPrice(OTHER_PRODUCT_SELL_PRICE)
                                .productType(PRODUCT_TYPE)
                                .listedNum(50)
                                .warehouseInventoryNum(200)
                                .isVideo(PRODUCT_IS_VIDEO)
                                .translations(List.of(
                                                ProductTranslationDtoIn.builder().locale("es")
                                                                .name(OTHER_PRODUCT_NAME_ES).build()))
                                .build();
        }

        public static ProductDtoOut productDtoOut(String id, String categoryId) {
                return ProductDtoOut.builder()
                                .id(id)
                                .sku(PRODUCT_SKU)
                                .categoryId(categoryId)
                                .status(ProductStatus.DRAFT)
                                .name(PRODUCT_NAME_ES)
                                .sellPrice(PRODUCT_SELL_PRICE)
                                .productType(PRODUCT_TYPE)
                                .listedNum(PRODUCT_LISTED_NUM)
                                .warehouseInventoryNum(PRODUCT_WAREHOUSE_NUM)
                                .isVideo(PRODUCT_IS_VIDEO)
                                .translations(List.of(
                                                ProductTranslationDtoOut.builder().locale("es").name(PRODUCT_NAME_ES)
                                                                .build()))
                                .build();
        }
}
