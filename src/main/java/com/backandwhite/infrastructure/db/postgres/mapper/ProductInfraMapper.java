package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.*;
import com.backandwhite.infrastructure.db.postgres.entity.*;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductInfraMapper {

    // ── Domain ← Entity ────────────────────────────────────────────────────

    @Mapping(target = "name", expression = "java(getFirstTranslationName(entity))")
    @Mapping(target = "costPrice", ignore = true)
    @Mapping(target = "availableLocales", expression = "java(getAvailableLocales(entity))")
    Product toDomain(ProductEntity entity);

    List<Product> toDomainList(List<ProductEntity> entities);

    @Mapping(target = "locale", source = "id.locale")
    @Mapping(target = "name", source = "name")
    ProductTranslation toTranslationDomain(ProductTranslationEntity entity);

    @Mapping(target = "retailPrice", ignore = true)
    ProductDetailVariant toVariantDomain(ProductDetailVariantEntity entity);

    @Mapping(target = "locale", source = "id.locale")
    ProductDetailVariantTranslation toVariantTranslationDomain(ProductDetailVariantTranslationEntity entity);

    ProductDetailVariantInventory toInventoryDomain(ProductDetailVariantInventoryEntity entity);

    default String getFirstTranslationName(ProductEntity entity) {
        if (entity.getTranslations() == null || entity.getTranslations().isEmpty()) {
            return null;
        }
        return entity.getTranslations().getFirst().getName();
    }

    default List<String> getAvailableLocales(ProductEntity entity) {
        if (entity.getTranslations() == null || entity.getTranslations().isEmpty()) {
            return List.of();
        }
        return entity.getTranslations().stream().map(t -> t.getId().getLocale()).distinct().sorted().toList();
    }

    // ── Domain → Entity ────────────────────────────────────────────────────

    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "listedNum", expression = "java(domain.getListedNum() != null ? domain.getListedNum() : 0)")
    @Mapping(target = "warehouseInventoryNum", expression = "java(domain.getWarehouseInventoryNum() != null ? domain.getWarehouseInventoryNum() : 0)")
    @Mapping(target = "isVideo", expression = "java(domain.getIsVideo() != null ? domain.getIsVideo() : false)")
    ProductEntity toEntity(Product domain);

    @Mapping(target = "id", expression = "java(new ProductTranslationId(productId, t.getLocale()))")
    @Mapping(target = "product", ignore = true)
    ProductTranslationEntity toTranslationEntity(ProductTranslation t, @Context String productId);

    default ProductEntity toEntityWithChildren(Product domain) {
        ProductEntity entity = toEntity(domain);

        if (domain.getTranslations() != null) {
            for (ProductTranslation t : domain.getTranslations()) {
                ProductTranslationEntity te = toTranslationEntity(t, domain.getId());
                te.setProduct(entity);
                entity.getTranslations().add(te);
            }
        }

        return entity;
    }
}
