package com.backandwhite.infrastructure.client.cj.mapper;

import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.*;
import com.backandwhite.infrastructure.client.cj.dto.CjInventoryDto;
import com.backandwhite.infrastructure.client.cj.dto.CjProductDetailDto;
import com.backandwhite.infrastructure.client.cj.dto.CjVariantDetailDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CjProductDetailMapper {

    // ── CjProductDetailDto → ProductDetail ───────────────────────────────────

    @Mapping(target = "productImageSet", source = "productImageSet", qualifiedByName = "joinImageSet")
    @Mapping(target = "listedNum", expression = "java(cj.getListedNum() != null ? cj.getListedNum() : 0)")
    @Mapping(target = "createrTime", source = "createrTime", qualifiedByName = "parseInstant")
    @Mapping(target = "translations", expression = "java(buildTranslations(cj))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductDetail toDomain(CjProductDetailDto cj);

    // ── CjProductDetailDto → Product (simplified model for sync) ─────────────

    @Mapping(target = "id", source = "pid")
    @Mapping(target = "sku", source = "productSku")
    @Mapping(target = "name", source = "productNameEn")
    @Mapping(target = "bigImage", source = "bigImage")
    @Mapping(target = "productImageSet", source = "productImageSet", qualifiedByName = "joinImageSet")
    @Mapping(target = "listedNum", expression = "java(cj.getListedNum() != null ? cj.getListedNum() : 0)")
    @Mapping(target = "translations", expression = "java(buildProductTranslations(cj))")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "warehouseInventoryNum", ignore = true)
    @Mapping(target = "isVideo", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "costPrice", ignore = true)
    Product toProduct(CjProductDetailDto cj);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "translations", expression = "java(buildVariantTranslations(v))")
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "parseInstant")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "retailPrice", ignore = true)
    ProductDetailVariant toVariantDomain(CjVariantDetailDto v);

    List<ProductDetailVariant> toVariantDomainList(List<CjVariantDetailDto> variants);

    ProductDetailVariantInventory toInventoryDomain(CjInventoryDto inv);

    List<ProductDetailVariantInventory> toInventoryDomainList(List<CjInventoryDto> inventories);

    @Named("joinImageSet")
    default String joinImageSet(List<String> imageSet) {
        if (imageSet == null || imageSet.isEmpty()) {
            return null;
        }
        return String.join(",", imageSet);
    }

    @Named("parseInstant")
    default Instant parseInstant(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        Instant parsed = tryParseOffsetDateTime(dateStr);
        return parsed != null ? parsed : tryParseEpochMillis(dateStr);
    }

    private static Instant tryParseOffsetDateTime(String dateStr) {
        try {
            return OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (Exception _) {
            return null;
        }
    }

    private static Instant tryParseEpochMillis(String dateStr) {
        try {
            return Instant.ofEpochMilli(Long.parseLong(dateStr));
        } catch (NumberFormatException _) {
            return null;
        }
    }

    default List<ProductDetailTranslation> buildTranslations(CjProductDetailDto cj) {
        List<ProductDetailTranslation> translations = new ArrayList<>();
        translations.add(ProductDetailTranslation.builder().locale("en").productName(cj.getProductNameEn())
                .entryName(cj.getEntryNameEn()).materialName(cj.getMaterialNameEn()).packingName(cj.getPackingNameEn())
                .productKey(cj.getProductKeyEn()).productPro(cj.getProductProEn()).build());
        return translations;
    }

    default List<ProductDetailVariantTranslation> buildVariantTranslations(CjVariantDetailDto v) {
        List<ProductDetailVariantTranslation> translations = new ArrayList<>();
        translations
                .add(ProductDetailVariantTranslation.builder().locale("en").variantName(v.getVariantNameEn()).build());
        return translations;
    }

    default List<ProductTranslation> buildProductTranslations(CjProductDetailDto cj) {
        String name = cj.getProductNameEn() != null ? cj.getProductNameEn() : "Unnamed Product";
        List<ProductTranslation> translations = new ArrayList<>();
        translations.add(ProductTranslation.builder().locale("en").name(name).build());
        return translations;
    }

    default Money stringToMoney(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return Money.of(new BigDecimal(value.trim()));
        } catch (NumberFormatException _) {
            return null;
        }
    }

    default Money bigDecimalToMoney(BigDecimal value) {
        return value != null ? Money.of(value) : null;
    }
}
