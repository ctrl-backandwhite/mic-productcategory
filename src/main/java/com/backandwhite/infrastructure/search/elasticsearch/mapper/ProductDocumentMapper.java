package com.backandwhite.infrastructure.search.elasticsearch.mapper;

import com.backandwhite.domain.model.Product;
import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantInventory;
import com.backandwhite.infrastructure.search.elasticsearch.document.ProductDocument;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductDocumentMapper {

    // ── Product (catalog table) → ProductDocument ────────────────────────────

    @Mapping(target = "pid", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "sku", source = "sku")
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "brandName", ignore = true)
    @Mapping(target = "brandSlug", ignore = true)
    @Mapping(target = "status", expression = "java(product.getStatus() != null ? product.getStatus().name() : \"DRAFT\")")
    @Mapping(target = "price", expression = "java(parsePrice(product.getSellPrice()))")
    @Mapping(target = "originalPrice", expression = "java(parsePrice(product.getCostPrice()))")
    @Mapping(target = "hasDiscount", expression = "java(hasDiscount(product.getSellPrice(), product.getCostPrice()))")
    @Mapping(target = "discountPercent", expression = "java(calcDiscount(product.getSellPrice(), product.getCostPrice()))")
    @Mapping(target = "totalStock", source = "warehouseInventoryNum")
    @Mapping(target = "inStock", expression = "java(product.getWarehouseInventoryNum() != null && product.getWarehouseInventoryNum() > 0)")
    @Mapping(target = "imageUrl", source = "bigImage")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "variants", source = "variants")
    @Mapping(target = "syncedAt", expression = "java(java.time.Instant.now())")
    ProductDocument fromProduct(Product product);

    // ── ProductDetail (CJ synced detail table) → ProductDocument ─────────────

    @Mapping(target = "id", source = "pid")
    @Mapping(target = "pid", source = "pid")
    @Mapping(target = "name", source = "productNameEn")
    @Mapping(target = "sku", source = "productSku")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "categoryName", source = "categoryName")
    @Mapping(target = "brandName", ignore = true)
    @Mapping(target = "brandSlug", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "price", expression = "java(parsePrice(detail.getSellPrice()))")
    @Mapping(target = "originalPrice", expression = "java(parsePrice(detail.getCostPrice()))")
    @Mapping(target = "hasDiscount", expression = "java(hasDiscount(detail.getSellPrice(), detail.getCostPrice()))")
    @Mapping(target = "discountPercent", expression = "java(calcDiscount(detail.getSellPrice(), detail.getCostPrice()))")
    @Mapping(target = "totalStock", expression = "java(calcTotalStock(detail.getVariants()))")
    @Mapping(target = "inStock", expression = "java(calcTotalStock(detail.getVariants()) > 0)")
    @Mapping(target = "imageUrl", source = "bigImage")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "variants", source = "variants")
    @Mapping(target = "syncedAt", expression = "java(java.time.Instant.now())")
    ProductDocument fromProductDetail(ProductDetail detail);

    // ── Variant mapping ──────────────────────────────────────────────────────

    @Mapping(target = "vid", source = "vid")
    @Mapping(target = "sku", source = "variantSku")
    @Mapping(target = "name", source = "variantNameEn")
    @Mapping(target = "price", expression = "java(variant.getVariantSellPrice() != null ? variant.getVariantSellPrice().getAmount().floatValue() : null)")
    @Mapping(target = "stock", expression = "java(calcVariantStock(variant.getInventories()))")
    @Mapping(target = "inStock", expression = "java(calcVariantStock(variant.getInventories()) > 0)")
    ProductDocument.VariantDocument toVariantDocument(ProductDetailVariant variant);

    // ── Helper methods ───────────────────────────────────────────────────────

    default Float parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isBlank())
            return null;
        try {
            // Handle range format like "9.99-17.97" — take the minimum (first) value
            String trimmed = priceStr.trim();
            int dashIdx = trimmed.indexOf('-', 1);
            if (dashIdx > 0) {
                trimmed = trimmed.substring(0, dashIdx);
            }
            return Float.parseFloat(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    default Boolean hasDiscount(String sellPrice, String costPrice) {
        Float sell = parsePrice(sellPrice);
        Float cost = parsePrice(costPrice);
        if (sell == null || cost == null || cost <= 0)
            return false;
        return sell < cost;
    }

    default Float calcDiscount(String sellPrice, String costPrice) {
        Float sell = parsePrice(sellPrice);
        Float cost = parsePrice(costPrice);
        if (sell == null || cost == null || cost <= 0)
            return null;
        if (sell >= cost)
            return 0f;
        return ((cost - sell) / cost) * 100f;
    }

    default Integer calcTotalStock(List<ProductDetailVariant> variants) {
        if (variants == null || variants.isEmpty())
            return 0;
        return variants.stream().mapToInt(v -> calcVariantStock(v.getInventories())).sum();
    }

    default Integer calcVariantStock(List<ProductDetailVariantInventory> inventories) {
        if (inventories == null || inventories.isEmpty())
            return 0;
        return inventories.stream().mapToInt(i -> i.getTotalInventory() != null ? i.getTotalInventory() : 0).sum();
    }
}
