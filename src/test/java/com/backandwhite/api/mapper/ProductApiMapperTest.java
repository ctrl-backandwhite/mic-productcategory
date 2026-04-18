package com.backandwhite.api.mapper;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.in.ProductTranslationDtoIn;
import com.backandwhite.api.dto.out.ProductDetailVariantDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantInventoryDtoOut;
import com.backandwhite.api.dto.out.ProductDetailVariantTranslationDtoOut;
import com.backandwhite.api.dto.out.ProductDtoOut;
import com.backandwhite.api.dto.out.ProductTranslationDtoOut;
import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantInventory;
import com.backandwhite.domain.model.ProductDetailVariantTranslation;
import com.backandwhite.domain.model.ProductTranslation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductApiMapperTest {

    private final ProductApiMapper mapper = Mappers.getMapper(ProductApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        ProductDtoOut dto = mapper.toDto(product(CATEGORY_ID));
        assertThat(dto.getId()).isEqualTo(PRODUCT_ID);
        assertThat(dto.getSku()).isEqualTo(PRODUCT_SKU);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        assertThat(mapper.toDtoList(List.of(product(CATEGORY_ID)))).hasSize(1);
    }

    @Test
    void toTranslationDto_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDto(null)).isNull();
    }

    @Test
    void toTranslationDto_mapsFields() {
        ProductTranslation t = ProductTranslation.builder().locale("es").name("X").build();
        ProductTranslationDtoOut dto = mapper.toTranslationDto(t);
        assertThat(dto.getLocale()).isEqualTo("es");
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Product domain = mapper.toDomain(productDtoIn(CATEGORY_ID));
        assertThat(domain.getSku()).isEqualTo(PRODUCT_SKU);
        assertThat(domain.getCategoryId()).isEqualTo(CATEGORY_ID);
    }

    @Test
    void toDto_withVariants_mapsNested() {
        Product p = product(CATEGORY_ID).withVariants(
                List.of(com.backandwhite.domain.model.ProductDetailVariant.builder().vid("v1").pid("p1").build()));
        ProductDtoOut dto = mapper.toDto(p);
        assertThat(dto.getVariants()).hasSize(1);
    }

    @Test
    void toVariantDto_nullInput_returnsNull() {
        assertThat(mapper.toVariantDto(null)).isNull();
    }

    @Test
    void toVariantDto_mapsFields() {
        com.backandwhite.domain.model.ProductDetailVariant v = com.backandwhite.domain.model.ProductDetailVariant
                .builder().vid("v1").pid("p1").variantSku("SKU").build();
        assertThat(mapper.toVariantDto(v).getVid()).isEqualTo("v1");
    }

    @Test
    void toVariantTranslationDto_nullInput_returnsNull() {
        assertThat(mapper.toVariantTranslationDto(null)).isNull();
    }

    @Test
    void toInventoryDto_nullInput_returnsNull() {
        assertThat(mapper.toInventoryDto(null)).isNull();
    }

    @Test
    void toTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDomain(null)).isNull();
    }

    @Test
    void toTranslationDomain_mapsFields() {
        ProductTranslationDtoIn dto = ProductTranslationDtoIn.builder().locale("en").name("EN").build();
        ProductTranslation domain = mapper.toTranslationDomain(dto);
        assertThat(domain.getLocale()).isEqualTo("en");
    }

    @Test
    void moneyToString_nullInput_returnsNull() {
        assertThat(mapper.moneyToString(null)).isNull();
    }

    @Test
    void moneyToString_returnsPlainString() {
        assertThat(mapper.moneyToString(Money.of(new BigDecimal("12.50")))).isEqualTo("12.50");
    }

    @Test
    void moneyToBigDecimal_nullInput_returnsNull() {
        assertThat(mapper.moneyToBigDecimal(null)).isNull();
    }

    @Test
    void moneyToBigDecimal_returnsAmount() {
        assertThat(mapper.moneyToBigDecimal(Money.of(new BigDecimal("9.99")))).isEqualTo(new BigDecimal("9.99"));
    }

    @Test
    void bigDecimalToMoney_nullInput_returnsNull() {
        assertThat(mapper.bigDecimalToMoney(null)).isNull();
    }

    @Test
    void bigDecimalToMoney_returnsMoney() {
        Money money = mapper.bigDecimalToMoney(new BigDecimal("3.14"));
        assertThat(money).isNotNull();
        assertThat(money.getAmount()).isEqualByComparingTo("3.14");
    }

    // ── Full-field coverage ─────────────────────────────────────────────────

    @Test
    void toDto_fullProduct_mapsAllFields() {
        Product p = product(CATEGORY_ID);
        p.setBigImage("big-img");
        p.setCostPrice("5.00");
        p.setCostPriceRaw(new BigDecimal("5.00"));
        p.setSellPriceRaw(new BigDecimal("19.99"));
        p.setCurrencyCode("USD");
        p.setCurrencySymbol("$");
        p.setDescription("desc");
        p.setProductImageSet("img-set");

        ProductDtoOut dto = mapper.toDto(p);

        assertThat(dto.getBigImage()).isEqualTo("big-img");
        assertThat(dto.getCostPrice()).isEqualTo("5.00");
        assertThat(dto.getCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getCurrencySymbol()).isEqualTo("$");
        assertThat(dto.getDescription()).isEqualTo("desc");
        assertThat(dto.getProductImageSet()).isEqualTo("img-set");
        assertThat(dto.getSellPriceRaw()).isEqualByComparingTo("19.99");
        assertThat(dto.getCostPriceRaw()).isEqualByComparingTo("5.00");
    }

    @Test
    void toDto_productWithNullCollections_mappedAsNull() {
        Product p = Product.builder().id("p1").sku("s").translations(null).variants(null).build();
        ProductDtoOut dto = mapper.toDto(p);
        assertThat(dto.getTranslations()).isNull();
        assertThat(dto.getVariants()).isNull();
    }

    @Test
    void toDto_productWithEmptyCollections_mappedAsEmpty() {
        Product p = Product.builder().id("p1").sku("s").translations(new ArrayList<>()).variants(new ArrayList<>())
                .build();
        ProductDtoOut dto = mapper.toDto(p);
        assertThat(dto.getTranslations()).isEmpty();
        assertThat(dto.getVariants()).isEmpty();
    }

    @Test
    void toVariantDto_fullVariantWithNestedInventoriesAndTranslations() {
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().id(1L).vid("v1").countryCode("US")
                .totalInventory(10).cjInventory(5).factoryInventory(3).verifiedWarehouse(2).build();
        ProductDetailVariantTranslation vt = ProductDetailVariantTranslation.builder().locale("es")
                .variantName("nombre").build();
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").pid("p1").variantSku("SKU1")
                .variantNameEn("name-en").variantUnit("u").variantKey("k").variantImage("img")
                .variantLength(new BigDecimal("1")).variantWidth(new BigDecimal("2")).variantHeight(new BigDecimal("3"))
                .variantVolume(new BigDecimal("6")).variantWeight(new BigDecimal("0.5"))
                .variantSellPrice(Money.of(new BigDecimal("10"))).variantSugSellPrice(Money.of(new BigDecimal("12")))
                .retailPrice(Money.of(new BigDecimal("15"))).variantStandard("std").currencyCode("USD")
                .translations(new ArrayList<>(List.of(vt))).inventories(new ArrayList<>(List.of(inv))).build();

        ProductDetailVariantDtoOut dto = mapper.toVariantDto(v);

        assertThat(dto.getVid()).isEqualTo("v1");
        assertThat(dto.getVariantSellPrice()).isEqualByComparingTo("10");
        assertThat(dto.getVariantSugSellPrice()).isEqualByComparingTo("12");
        assertThat(dto.getRetailPrice()).isEqualByComparingTo("15");
        assertThat(dto.getCurrencyCode()).isEqualTo("USD");
        assertThat(dto.getInventories()).hasSize(1);
        assertThat(dto.getTranslations()).hasSize(1);
    }

    @Test
    void toVariantDto_nullCollections_mappedAsNull() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").pid("p1").translations(null).inventories(null)
                .build();
        ProductDetailVariantDtoOut dto = mapper.toVariantDto(v);
        assertThat(dto.getInventories()).isNull();
        assertThat(dto.getTranslations()).isNull();
    }

    @Test
    void toVariantDto_nullMoneyFields_handled() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").pid("p1").variantSellPrice(null)
                .variantSugSellPrice(null).retailPrice(null).build();
        ProductDetailVariantDtoOut dto = mapper.toVariantDto(v);
        assertThat(dto.getVariantSellPrice()).isNull();
        assertThat(dto.getVariantSugSellPrice()).isNull();
        assertThat(dto.getRetailPrice()).isNull();
    }

    @Test
    void toInventoryDto_fullFields_mapped() {
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().id(42L).vid("v1").countryCode("US")
                .totalInventory(100).cjInventory(50).factoryInventory(40).verifiedWarehouse(10).build();

        ProductDetailVariantInventoryDtoOut dto = mapper.toInventoryDto(inv);

        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getCountryCode()).isEqualTo("US");
        assertThat(dto.getTotalInventory()).isEqualTo(100);
        assertThat(dto.getCjInventory()).isEqualTo(50);
        assertThat(dto.getFactoryInventory()).isEqualTo(40);
        assertThat(dto.getVerifiedWarehouse()).isEqualTo(10);
    }

    @Test
    void toVariantTranslationDto_fullFields() {
        ProductDetailVariantTranslation t = ProductDetailVariantTranslation.builder().locale("fr").variantName("nom-fr")
                .build();

        ProductDetailVariantTranslationDtoOut dto = mapper.toVariantTranslationDto(t);

        assertThat(dto.getLocale()).isEqualTo("fr");
        assertThat(dto.getVariantName()).isEqualTo("nom-fr");
    }

    @Test
    void toTranslationDto_nullFields() {
        ProductTranslation t = ProductTranslation.builder().locale(null).name(null).build();
        ProductTranslationDtoOut dto = mapper.toTranslationDto(t);
        assertThat(dto.getLocale()).isNull();
        assertThat(dto.getName()).isNull();
    }

    @Test
    void toDomain_dtoWithNullTranslations_mappedAsNull() {
        com.backandwhite.api.dto.in.ProductDtoIn dto = com.backandwhite.api.dto.in.ProductDtoIn.builder().sku("s")
                .categoryId(CATEGORY_ID).translations(null).build();
        Product d = mapper.toDomain(dto);
        assertThat(d.getTranslations()).isNull();
    }
}
