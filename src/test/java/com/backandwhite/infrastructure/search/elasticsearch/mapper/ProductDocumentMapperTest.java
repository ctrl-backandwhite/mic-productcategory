package com.backandwhite.infrastructure.search.elasticsearch.mapper;

import static com.backandwhite.provider.CategoryProvider.CATEGORY_ID;
import static com.backandwhite.provider.ProductProvider.product;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantInventory;
import com.backandwhite.domain.valueobject.ProductStatus;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductDocumentMapperTest {

    private final ProductDocumentMapper mapper = Mappers.getMapper(ProductDocumentMapper.class);

    @Test
    void fromProduct_nullInput_returnsNull() {
        assertThat(mapper.fromProduct(null)).isNull();
    }

    @Test
    void fromProduct_mapsFields() {
        Product p = product(CATEGORY_ID).withSellPrice("19.99").withCostPrice("10.00").withWarehouseInventoryNum(5);
        ProductDocument doc = mapper.fromProduct(p);
        assertThat(doc.getSku()).isNotNull();
        assertThat(doc.getPrice()).isEqualTo(19.99f);
        assertThat(doc.getStatus()).isEqualTo("DRAFT");
        assertThat(doc.getInStock()).isTrue();
        assertThat(doc.getHasDiscount()).isFalse();
    }

    @Test
    void fromProduct_withDiscount_calculatesCorrectly() {
        Product p = Product.builder().id("p1").sku("S").sellPrice("8").costPrice("10").warehouseInventoryNum(0).build();
        ProductDocument doc = mapper.fromProduct(p);
        assertThat(doc.getHasDiscount()).isTrue();
        assertThat(doc.getDiscountPercent()).isEqualTo(20.0f);
        assertThat(doc.getInStock()).isFalse();
    }

    @Test
    void fromProduct_nullStatus_returnsDraft() {
        Product p = Product.builder().id("p1").sku("S").status(null).build();
        ProductDocument doc = mapper.fromProduct(p);
        assertThat(doc.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void fromProduct_nullWarehouse_inStockFalse() {
        Product p = Product.builder().id("p1").sku("S").warehouseInventoryNum(null).build();
        ProductDocument doc = mapper.fromProduct(p);
        assertThat(doc.getInStock()).isFalse();
    }

    @Test
    void fromProductDetail_nullInput_returnsNull() {
        assertThat(mapper.fromProductDetail(null)).isNull();
    }

    @Test
    void fromProductDetail_mapsFields() {
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().vid("v1").countryCode("US")
                .totalInventory(10).build();
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").pid("p1").variantSku("SKU1")
                .variantSellPrice(Money.of(new BigDecimal("5.00"))).inventories(List.of(inv)).build();
        ProductDetail d = ProductDetail.builder().pid("pid-1").productNameEn("Name").productSku("SKU").sellPrice("8")
                .costPrice("10").bigImage("url").status(ProductStatus.PUBLISHED.name()).variants(List.of(v)).build();
        ProductDocument doc = mapper.fromProductDetail(d);
        assertThat(doc.getId()).isEqualTo("pid-1");
        assertThat(doc.getName()).isEqualTo("Name");
        assertThat(doc.getTotalStock()).isEqualTo(10);
        assertThat(doc.getInStock()).isTrue();
        assertThat(doc.getVariants()).hasSize(1);
    }

    @Test
    void toVariantDocument_nullInput_returnsNull() {
        assertThat(mapper.toVariantDocument(null)).isNull();
    }

    @Test
    void toVariantDocument_mapsFields() {
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().totalInventory(7).build();
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").variantSku("SKU")
                .variantSellPrice(Money.of(new BigDecimal("3.50"))).inventories(List.of(inv)).build();
        ProductDocument.VariantDocument vd = mapper.toVariantDocument(v);
        assertThat(vd.getVid()).isEqualTo("v1");
        assertThat(vd.getPrice()).isEqualTo(3.50f);
        assertThat(vd.getStock()).isEqualTo(7);
        assertThat(vd.getInStock()).isTrue();
    }

    @Test
    void toVariantDocument_nullPriceAndInventories() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("v1").variantSku("SKU").variantSellPrice(null)
                .inventories(null).build();
        ProductDocument.VariantDocument vd = mapper.toVariantDocument(v);
        assertThat(vd.getPrice()).isNull();
        assertThat(vd.getStock()).isZero();
        assertThat(vd.getInStock()).isFalse();
    }

    @Test
    void parsePrice_handlesVariousInputs() {
        assertThat(mapper.parsePrice(null)).isNull();
        assertThat(mapper.parsePrice("  ")).isNull();
        assertThat(mapper.parsePrice("not-a-number")).isNull();
        assertThat(mapper.parsePrice("12.5")).isEqualTo(12.5f);
        assertThat(mapper.parsePrice("9.99-17.97")).isEqualTo(9.99f);
    }

    @Test
    void hasDiscount_variousCases() {
        assertThat(mapper.hasDiscount(null, "10")).isFalse();
        assertThat(mapper.hasDiscount("10", null)).isFalse();
        assertThat(mapper.hasDiscount("10", "0")).isFalse();
        assertThat(mapper.hasDiscount("5", "10")).isTrue();
        assertThat(mapper.hasDiscount("10", "5")).isFalse();
    }

    @Test
    void calcDiscount_variousCases() {
        assertThat(mapper.calcDiscount(null, "10")).isNull();
        assertThat(mapper.calcDiscount("10", null)).isNull();
        assertThat(mapper.calcDiscount("10", "0")).isNull();
        assertThat(mapper.calcDiscount("5", "10")).isEqualTo(50f);
        assertThat(mapper.calcDiscount("10", "10")).isEqualTo(0f);
    }

    @Test
    void calcTotalStock_variousCases() {
        assertThat(mapper.calcTotalStock(null)).isZero();
        assertThat(mapper.calcTotalStock(List.of())).isZero();
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().totalInventory(3).build();
        ProductDetailVariant v = ProductDetailVariant.builder().inventories(List.of(inv)).build();
        assertThat(mapper.calcTotalStock(List.of(v))).isEqualTo(3);
    }

    @Test
    void calcVariantStock_nullInventoryValues() {
        ProductDetailVariantInventory inv1 = ProductDetailVariantInventory.builder().totalInventory(null).build();
        ProductDetailVariantInventory inv2 = ProductDetailVariantInventory.builder().totalInventory(5).build();
        assertThat(mapper.calcVariantStock(null)).isZero();
        assertThat(mapper.calcVariantStock(List.of())).isZero();
        assertThat(mapper.calcVariantStock(List.of(inv1, inv2))).isEqualTo(5);
    }
}
