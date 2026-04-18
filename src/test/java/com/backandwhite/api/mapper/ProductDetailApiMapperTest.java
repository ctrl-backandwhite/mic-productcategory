package com.backandwhite.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.in.ProductDetailVariantDtoIn;
import com.backandwhite.api.dto.in.ProductDetailVariantInventoryDtoIn;
import com.backandwhite.api.dto.in.ProductDetailVariantTranslationDtoIn;
import com.backandwhite.api.dto.out.ProductDetailDtoOut;
import com.backandwhite.api.dto.out.ProductDetailTranslationDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantInventoryDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantTranslationDtoOut;
import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailTranslation;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantInventory;
import com.backandwhite.domain.model.ProductDetailVariantTranslation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductDetailApiMapperTest {

    private final ProductDetailApiMapper mapper = Mappers.getMapper(ProductDetailApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        ProductDetail detail = ProductDetail.builder().pid("pid-1").productNameEn("Name").build();
        ProductDetailDtoOut dto = mapper.toDto(detail);
        assertThat(dto.getPid()).isEqualTo("pid-1");
    }

    @Test
    void toTranslationDto_mapsFields() {
        ProductDetailTranslation t = ProductDetailTranslation.builder().locale("es").productName("X").build();
        ProductDetailTranslationDtoOut dto = mapper.toTranslationDto(t);
        assertThat(dto.getLocale()).isEqualTo("es");
    }

    @Test
    void toTranslationDto_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDto(null)).isNull();
    }

    @Test
    void toVariantDto_mapsFields() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid-1").pid("pid-1")
                .variantSellPrice(Money.of(new BigDecimal("5.00"))).build();
        ProductDetailVariantDtoOut dto = mapper.toVariantDto(v);
        assertThat(dto.getVid()).isEqualTo("vid-1");
    }

    @Test
    void toVariantDto_nullInput_returnsNull() {
        assertThat(mapper.toVariantDto(null)).isNull();
    }

    @Test
    void toVariantDtoList_nullInput_returnsNull() {
        assertThat(mapper.toVariantDtoList(null)).isNull();
    }

    @Test
    void toVariantDtoList_mapsList() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid-1").pid("pid-1").build();
        assertThat(mapper.toVariantDtoList(List.of(v))).hasSize(1);
    }

    @Test
    void toVariantTranslationDto_nullInput_returnsNull() {
        assertThat(mapper.toVariantTranslationDto(null)).isNull();
    }

    @Test
    void toVariantTranslationDto_mapsFields() {
        ProductDetailVariantTranslation t = ProductDetailVariantTranslation.builder().locale("es").build();
        ProductDetailVariantTranslationDtoOut dto = mapper.toVariantTranslationDto(t);
        assertThat(dto.getLocale()).isEqualTo("es");
    }

    @Test
    void toInventoryDto_nullInput_returnsNull() {
        assertThat(mapper.toInventoryDto(null)).isNull();
    }

    @Test
    void toInventoryDto_mapsFields() {
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().vid("v1").countryCode("US")
                .totalInventory(10).build();
        ProductDetailVariantInventoryDtoOut dto = mapper.toInventoryDto(inv);
        assertThat(dto.getCountryCode()).isEqualTo("US");
    }

    @Test
    void toVariantDomain_nullInput_returnsNull() {
        assertThat(mapper.toVariantDomain(null)).isNull();
    }

    @Test
    void toVariantDomain_mapsFields() {
        ProductDetailVariantDtoIn dto = ProductDetailVariantDtoIn.builder().pid("pid-1").variantNameEn("name")
                .variantSku("SKU-1").build();
        ProductDetailVariant v = mapper.toVariantDomain(dto);
        assertThat(v.getPid()).isEqualTo("pid-1");
        assertThat(v.getVariantSku()).isEqualTo("SKU-1");
    }

    @Test
    void toVariantTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toVariantTranslationDomain(null)).isNull();
    }

    @Test
    void toVariantTranslationDomain_mapsFields() {
        ProductDetailVariantTranslationDtoIn dto = ProductDetailVariantTranslationDtoIn.builder().locale("es").build();
        ProductDetailVariantTranslation t = mapper.toVariantTranslationDomain(dto);
        assertThat(t.getLocale()).isEqualTo("es");
    }

    @Test
    void toInventoryDomain_nullInput_returnsNull() {
        assertThat(mapper.toInventoryDomain(null)).isNull();
    }

    @Test
    void toInventoryDomain_mapsFields() {
        ProductDetailVariantInventoryDtoIn dto = ProductDetailVariantInventoryDtoIn.builder().countryCode("US")
                .totalInventory(10).build();
        ProductDetailVariantInventory i = mapper.toInventoryDomain(dto);
        assertThat(i.getCountryCode()).isEqualTo("US");
    }

    @Test
    void moneyToString_mapsValues() {
        assertThat(mapper.moneyToString(null)).isNull();
        assertThat(mapper.moneyToString(Money.of(new BigDecimal("1.23")))).isEqualTo("1.23");
    }

    @Test
    void moneyToBigDecimal_mapsValues() {
        assertThat(mapper.moneyToBigDecimal(null)).isNull();
        assertThat(mapper.moneyToBigDecimal(Money.of(new BigDecimal("1.23")))).isEqualByComparingTo("1.23");
    }

    @Test
    void bigDecimalToMoney_mapsValues() {
        assertThat(mapper.bigDecimalToMoney(null)).isNull();
        assertThat(mapper.bigDecimalToMoney(new BigDecimal("1.23")).getAmount()).isEqualByComparingTo("1.23");
    }

    @Test
    void toDto_fullDetail_mapsAllFields() {
        ProductDetail detail = ProductDetail.builder().pid("pid-1").bigImage("big").categoryId("cat-1")
                .categoryName("Category").costPrice("5.00").costPriceRaw(new BigDecimal("5.00")).currencyCode("USD")
                .currencySymbol("$").description("desc").entryCode("ec").entryNameEn("en").listedNum(10)
                .materialKey("mk").materialNameEn("mat").packingKey("pk").packingNameEn("pac").packingWeight("1.0")
                .productImage("img").productImageSet("imgset").productKeyEn("pk-en").productNameEn("name-en")
                .productProEn("pro-en").productSku("SKU-X").productType("T").productUnit("u").productWeight("2")
                .sellPrice("19.99").sellPriceRaw(new BigDecimal("19.99")).suggestSellPrice("22").supplierId("sup-id")
                .supplierName("sup").build();

        ProductDetailDtoOut dto = mapper.toDto(detail);

        assertThat(dto.getBigImage()).isEqualTo("big");
        assertThat(dto.getCategoryName()).isEqualTo("Category");
        assertThat(dto.getCostPrice()).isEqualTo("5.00");
        assertThat(dto.getCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getCurrencySymbol()).isEqualTo("$");
        assertThat(dto.getEntryCode()).isEqualTo("ec");
        assertThat(dto.getMaterialKey()).isEqualTo("mk");
        assertThat(dto.getPackingKey()).isEqualTo("pk");
        assertThat(dto.getProductKeyEn()).isEqualTo("pk-en");
        assertThat(dto.getSupplierName()).isEqualTo("sup");
        assertThat(dto.getSuggestSellPrice()).isEqualTo("22");
    }

    @Test
    void toDto_emptyCollections_mappedAsEmpty() {
        ProductDetail detail = ProductDetail.builder().pid("p1").translations(new ArrayList<>())
                .variants(new ArrayList<>()).build();
        ProductDetailDtoOut dto = mapper.toDto(detail);
        assertThat(dto.getTranslations()).isEmpty();
        assertThat(dto.getVariants()).isEmpty();
    }

    @Test
    void toDto_nullCollections_mappedAsNull() {
        ProductDetail detail = ProductDetail.builder().pid("p1").translations(null).variants(null).build();
        ProductDetailDtoOut dto = mapper.toDto(detail);
        assertThat(dto.getTranslations()).isNull();
        assertThat(dto.getVariants()).isNull();
    }

    @Test
    void toTranslationDto_fullFields() {
        ProductDetailTranslation t = ProductDetailTranslation.builder().entryName("entry").locale("es")
                .materialName("material").packingName("packing").productKey("pk").productName("pname").productPro("pro")
                .build();
        ProductDetailTranslationDtoOut dto = mapper.toTranslationDto(t);
        assertThat(dto.getEntryName()).isEqualTo("entry");
        assertThat(dto.getMaterialName()).isEqualTo("material");
        assertThat(dto.getPackingName()).isEqualTo("packing");
        assertThat(dto.getProductKey()).isEqualTo("pk");
        assertThat(dto.getProductPro()).isEqualTo("pro");
    }

    @Test
    void toVariantDto_fullVariantWithMoney() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").pid("p1")
                .variantSellPrice(Money.of(new BigDecimal("10"))).variantSugSellPrice(Money.of(new BigDecimal("12")))
                .retailPrice(Money.of(new BigDecimal("15"))).variantNameEn("en").variantSku("SKU").variantKey("k")
                .variantImage("img").variantUnit("u").variantStandard("std").variantLength(new BigDecimal("1"))
                .variantWidth(new BigDecimal("2")).variantHeight(new BigDecimal("3")).variantVolume(new BigDecimal("6"))
                .variantWeight(new BigDecimal("0.5")).currencyCode("USD").translations(null).inventories(null).build();

        ProductDetailVariantDtoOut dto = mapper.toVariantDto(v);

        assertThat(dto.getVariantSellPrice()).isEqualByComparingTo("10");
        assertThat(dto.getVariantSugSellPrice()).isEqualByComparingTo("12");
        assertThat(dto.getRetailPrice()).isEqualByComparingTo("15");
        assertThat(dto.getCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getTranslations()).isNull();
        assertThat(dto.getInventories()).isNull();
    }

    @Test
    void toVariantDto_emptyCollections_mappedAsEmpty() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").pid("p1").translations(new ArrayList<>())
                .inventories(new ArrayList<>()).build();
        ProductDetailVariantDtoOut dto = mapper.toVariantDto(v);
        assertThat(dto.getTranslations()).isEmpty();
        assertThat(dto.getInventories()).isEmpty();
    }

    @Test
    void toInventoryDto_fullFields() {
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().id(42L).vid("v1").countryCode("US")
                .totalInventory(100).cjInventory(50).factoryInventory(40).verifiedWarehouse(10).build();
        ProductDetailVariantInventoryDtoOut dto = mapper.toInventoryDto(inv);
        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getCjInventory()).isEqualTo(50);
        assertThat(dto.getFactoryInventory()).isEqualTo(40);
        assertThat(dto.getVerifiedWarehouse()).isEqualTo(10);
    }

    @Test
    void toVariantDomain_fullDtoIn() {
        ProductDetailVariantDtoIn dto = ProductDetailVariantDtoIn.builder().pid("p1").variantNameEn("name")
                .variantSku("SKU").variantKey("k").variantImage("img").variantUnit("u").variantStandard("std")
                .variantLength(new BigDecimal("1")).variantWidth(new BigDecimal("2")).variantHeight(new BigDecimal("3"))
                .variantVolume(new BigDecimal("6")).variantWeight(new BigDecimal("0.5"))
                .variantSellPrice(new BigDecimal("10")).variantSugSellPrice(new BigDecimal("12")).build();
        ProductDetailVariant v = mapper.toVariantDomain(dto);
        assertThat(v.getVariantSellPrice().getAmount()).isEqualByComparingTo("10");
        assertThat(v.getVariantSugSellPrice().getAmount()).isEqualByComparingTo("12");
        assertThat(v.getVariantKey()).isEqualTo("k");
    }

    @Test
    void toInventoryDomain_fullDtoIn() {
        ProductDetailVariantInventoryDtoIn dto = ProductDetailVariantInventoryDtoIn.builder().countryCode("US")
                .totalInventory(100).cjInventory(50).factoryInventory(40).verifiedWarehouse(10).build();
        ProductDetailVariantInventory i = mapper.toInventoryDomain(dto);
        assertThat(i.getCjInventory()).isEqualTo(50);
        assertThat(i.getVerifiedWarehouse()).isEqualTo(10);
    }
}
