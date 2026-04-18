package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantInventory;
import com.backandwhite.domain.model.ProductDetailVariantTranslation;
import com.backandwhite.domain.model.ProductTranslation;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantTranslationId;
import com.backandwhite.infrastructure.db.postgres.entity.ProductEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductTranslationId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductInfraMapperTest {

    private final ProductInfraMapper mapper = Mappers.getMapper(ProductInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields_withTranslations() {
        ProductEntity entity = productEntity(CATEGORY_ID);
        ProductTranslationEntity t = ProductTranslationEntity.builder().id(new ProductTranslationId(PRODUCT_ID, "es"))
                .name(PRODUCT_NAME_ES).product(entity).build();
        entity.setTranslations(List.of(t));
        Product d = mapper.toDomain(entity);
        assertThat(d.getId()).isEqualTo(PRODUCT_ID);
        assertThat(d.getName()).isEqualTo(PRODUCT_NAME_ES);
    }

    @Test
    void toDomain_noTranslations_nameIsNull() {
        ProductEntity entity = productEntity(CATEGORY_ID);
        entity.setTranslations(new ArrayList<>());
        Product d = mapper.toDomain(entity);
        assertThat(d.getName()).isNull();
    }

    @Test
    void getFirstTranslationName_nullList_returnsNull() {
        ProductEntity entity = ProductEntity.builder().id("id").build();
        entity.setTranslations(null);
        assertThat(mapper.getFirstTranslationName(entity)).isNull();
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(productEntity(CATEGORY_ID)))).hasSize(1);
    }

    @Test
    void toTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDomain(null)).isNull();
    }

    @Test
    void toTranslationDomain_mapsFields() {
        ProductTranslationEntity entity = ProductTranslationEntity.builder()
                .id(new ProductTranslationId(PRODUCT_ID, "es")).name(PRODUCT_NAME_ES).build();
        ProductTranslation t = mapper.toTranslationDomain(entity);
        assertThat(t.getLocale()).isEqualTo("es");
        assertThat(t.getName()).isEqualTo(PRODUCT_NAME_ES);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        ProductEntity e = mapper.toEntity(product(CATEGORY_ID));
        assertThat(e.getId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    void toEntity_nullDefaults_appliedForListedNumAndBooleans() {
        Product p = Product.builder().id(PRODUCT_ID).sku("S").listedNum(null).warehouseInventoryNum(null).isVideo(null)
                .build();
        ProductEntity e = mapper.toEntity(p);
        assertThat(e.getListedNum()).isZero();
        assertThat(e.getWarehouseInventoryNum()).isZero();
        assertThat(e.getIsVideo()).isFalse();
    }

    @Test
    void toTranslationEntity_mapsFields() {
        ProductTranslation t = ProductTranslation.builder().locale("es").name("X").build();
        ProductTranslationEntity e = mapper.toTranslationEntity(t, PRODUCT_ID);
        assertThat(e.getId().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(e.getId().getLocale()).isEqualTo("es");
    }

    @Test
    void toEntityWithChildren_wiresTranslations() {
        Product p = product(CATEGORY_ID);
        ProductEntity e = mapper.toEntityWithChildren(p);
        assertThat(e.getTranslations()).hasSize(3);
        assertThat(e.getTranslations().getFirst().getProduct()).isSameAs(e);
    }

    @Test
    void toEntityWithChildren_nullTranslations_noChildren() {
        Product p = Product.builder().id("p").sku("S").categoryId(CATEGORY_ID).translations(null).build();
        ProductEntity e = mapper.toEntityWithChildren(p);
        assertThat(e.getTranslations()).isEmpty();
    }

    // ── Variant domain mapping ───────────────────────────────────────────────

    @Test
    void toVariantDomain_nullInput_returnsNull() {
        assertThat(mapper.toVariantDomain(null)).isNull();
    }

    @Test
    void toVariantDomain_mapsAllFields() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        ProductDetailVariantEntity e = ProductDetailVariantEntity.builder().vid("v1").pid("p1")
                .status(ProductStatus.PUBLISHED).variantNameEn("name").variantSku("SKU1").variantUnit("u")
                .variantKey("k").variantImage("img").variantLength(new BigDecimal("1.0"))
                .variantWidth(new BigDecimal("2.0")).variantHeight(new BigDecimal("3.0"))
                .variantVolume(new BigDecimal("6.0")).variantWeight(new BigDecimal("0.5"))
                .variantSellPrice(Money.of(new BigDecimal("10.00")))
                .variantSugSellPrice(Money.of(new BigDecimal("15.00"))).variantStandard("std").createTime(now)
                .translations(new ArrayList<>()).inventories(new ArrayList<>()).build();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        ProductDetailVariant d = mapper.toVariantDomain(e);

        assertThat(d.getVid()).isEqualTo("v1");
        assertThat(d.getPid()).isEqualTo("p1");
        assertThat(d.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
        assertThat(d.getVariantSku()).isEqualTo("SKU1");
        assertThat(d.getVariantSellPrice().getAmount()).isEqualByComparingTo("10.00");
        assertThat(d.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toVariantDomain_withNestedTranslationsAndInventories() {
        ProductDetailVariantTranslationEntity t = ProductDetailVariantTranslationEntity.builder()
                .id(new ProductDetailVariantTranslationId("v1", "es")).variantName("nombre").build();
        ProductDetailVariantInventoryEntity inv = ProductDetailVariantInventoryEntity.builder().id(1L).vid("v1")
                .countryCode("US").totalInventory(100).cjInventory(50).factoryInventory(40).verifiedWarehouse(10)
                .build();
        ProductDetailVariantEntity e = ProductDetailVariantEntity.builder().vid("v1").pid("p1")
                .translations(new ArrayList<>(List.of(t))).inventories(new ArrayList<>(List.of(inv))).build();

        ProductDetailVariant d = mapper.toVariantDomain(e);

        assertThat(d.getTranslations()).hasSize(1);
        assertThat(d.getTranslations().getFirst().getLocale()).isEqualTo("es");
        assertThat(d.getInventories()).hasSize(1);
        assertThat(d.getInventories().getFirst().getTotalInventory()).isEqualTo(100);
    }

    @Test
    void toVariantTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toVariantTranslationDomain(null)).isNull();
    }

    @Test
    void toVariantTranslationDomain_mapsFields() {
        ProductDetailVariantTranslationEntity e = ProductDetailVariantTranslationEntity.builder()
                .id(new ProductDetailVariantTranslationId("v1", "en")).variantName("name-en").build();

        ProductDetailVariantTranslation t = mapper.toVariantTranslationDomain(e);

        assertThat(t.getLocale()).isEqualTo("en");
        assertThat(t.getVariantName()).isEqualTo("name-en");
    }

    @Test
    void toVariantTranslationDomain_nullId_localeIsNull() {
        ProductDetailVariantTranslationEntity e = ProductDetailVariantTranslationEntity.builder().id(null)
                .variantName("x").build();

        ProductDetailVariantTranslation t = mapper.toVariantTranslationDomain(e);

        assertThat(t.getLocale()).isNull();
        assertThat(t.getVariantName()).isEqualTo("x");
    }

    @Test
    void toInventoryDomain_nullInput_returnsNull() {
        assertThat(mapper.toInventoryDomain(null)).isNull();
    }

    @Test
    void toInventoryDomain_mapsAllFields() {
        ProductDetailVariantInventoryEntity e = ProductDetailVariantInventoryEntity.builder().id(42L).vid("v1")
                .countryCode("US").totalInventory(100).cjInventory(50).factoryInventory(40).verifiedWarehouse(10)
                .build();

        ProductDetailVariantInventory inv = mapper.toInventoryDomain(e);

        assertThat(inv.getId()).isEqualTo(42L);
        assertThat(inv.getVid()).isEqualTo("v1");
        assertThat(inv.getCountryCode()).isEqualTo("US");
        assertThat(inv.getTotalInventory()).isEqualTo(100);
        assertThat(inv.getCjInventory()).isEqualTo(50);
        assertThat(inv.getFactoryInventory()).isEqualTo(40);
        assertThat(inv.getVerifiedWarehouse()).isEqualTo(10);
    }

    @Test
    void toDomain_withVariantsInventoriesAndTranslations_allMapped() {
        ProductDetailVariantTranslationEntity vt = ProductDetailVariantTranslationEntity.builder()
                .id(new ProductDetailVariantTranslationId("v1", "es")).variantName("nombre").build();
        ProductDetailVariantInventoryEntity inv = ProductDetailVariantInventoryEntity.builder().id(1L).vid("v1")
                .countryCode("US").totalInventory(100).build();
        ProductDetailVariantEntity variant = ProductDetailVariantEntity.builder().vid("v1").pid(PRODUCT_ID)
                .variantSku("SKU-V1").translations(new ArrayList<>(List.of(vt)))
                .inventories(new ArrayList<>(List.of(inv))).build();
        ProductTranslationEntity pt = ProductTranslationEntity.builder().id(new ProductTranslationId(PRODUCT_ID, "es"))
                .name(PRODUCT_NAME_ES).build();
        ProductEntity entity = productEntity(CATEGORY_ID);
        entity.setTranslations(new ArrayList<>(List.of(pt)));
        entity.setVariants(new ArrayList<>(List.of(variant)));

        Product d = mapper.toDomain(entity);

        assertThat(d.getVariants()).hasSize(1);
        assertThat(d.getVariants().getFirst().getVid()).isEqualTo("v1");
        assertThat(d.getVariants().getFirst().getTranslations()).hasSize(1);
        assertThat(d.getVariants().getFirst().getInventories()).hasSize(1);
        assertThat(d.getTranslations()).hasSize(1);
    }

    @Test
    void toTranslationDomain_nullId_localeIsNull() {
        ProductTranslationEntity e = ProductTranslationEntity.builder().id(null).name("x").build();
        ProductTranslation t = mapper.toTranslationDomain(e);
        assertThat(t.getLocale()).isNull();
        assertThat(t.getName()).isEqualTo("x");
    }

    @Test
    void toDomain_nullCollections_mappedAsNull() {
        ProductEntity entity = productEntity(CATEGORY_ID);
        entity.setTranslations(null);
        entity.setVariants(null);
        Product d = mapper.toDomain(entity);
        assertThat(d.getTranslations()).isNull();
        assertThat(d.getVariants()).isNull();
    }

    @Test
    void toTranslationEntity_nullInput_returnsNull() {
        assertThat(mapper.toTranslationEntity(null, PRODUCT_ID)).isNull();
    }
}
