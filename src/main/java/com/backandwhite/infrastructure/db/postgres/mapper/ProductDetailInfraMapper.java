package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.*;
import com.backandwhite.infrastructure.db.postgres.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductDetailInfraMapper {

    // ── Domain ← Entity ────────────────────────────────────────────────────

    ProductDetail toDomain(ProductDetailEntity entity);

    List<ProductDetail> toDomainList(List<ProductDetailEntity> entities);

    @Mapping(target = "locale", source = "id.locale")
    ProductDetailTranslation toTranslationDomain(ProductDetailTranslationEntity entity);

    @Mapping(target = "retailPrice", ignore = true)
    ProductDetailVariant toVariantDomain(ProductDetailVariantEntity entity);

    @Mapping(target = "locale", source = "id.locale")
    ProductDetailVariantTranslation toVariantTranslationDomain(ProductDetailVariantTranslationEntity entity);

    ProductDetailVariantInventory toInventoryDomain(ProductDetailVariantInventoryEntity entity);

    // ── Domain → Entity ────────────────────────────────────────────────────

    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductDetailEntity toEntity(ProductDetail domain);

    @Mapping(target = "id", expression = "java(new ProductDetailTranslationId(pid, t.getLocale()))")
    @Mapping(target = "productDetail", ignore = true)
    ProductDetailTranslationEntity toTranslationEntity(ProductDetailTranslation t, @Context String pid);

    @Mapping(target = "productDetail", ignore = true)
    @Mapping(target = "pid", ignore = true)
    @Mapping(target = "translations", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductDetailVariantEntity toVariantEntity(ProductDetailVariant v);

    @Mapping(target = "id", expression = "java(new ProductDetailVariantTranslationId(vid, vt.getLocale()))")
    @Mapping(target = "variant", ignore = true)
    ProductDetailVariantTranslationEntity toVariantTranslationEntity(ProductDetailVariantTranslation vt,
            @Context String vid);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vid", ignore = true)
    @Mapping(target = "variant", ignore = true)
    ProductDetailVariantInventoryEntity toInventoryEntity(ProductDetailVariantInventory inv);

    List<ProductDetailVariantInventoryEntity> toInventoryEntityList(List<ProductDetailVariantInventory> inventories);

    // ── Wiring logic (sets parent refs & builds child lists) ───────────────

    default ProductDetailEntity toEntityWithChildren(ProductDetail domain) {
        ProductDetailEntity entity = toEntity(domain);

        if (domain.getTranslations() != null) {
            for (ProductDetailTranslation t : domain.getTranslations()) {
                ProductDetailTranslationEntity te = toTranslationEntity(t, domain.getPid());
                te.setProductDetail(entity);
                entity.getTranslations().add(te);
            }
        }

        if (domain.getVariants() != null) {
            for (ProductDetailVariant v : domain.getVariants()) {
                ProductDetailVariantEntity ve = toVariantEntity(v);
                ve.setProductDetail(entity);

                if (v.getTranslations() != null) {
                    for (ProductDetailVariantTranslation vt : v.getTranslations()) {
                        ProductDetailVariantTranslationEntity vte = toVariantTranslationEntity(vt, v.getVid());
                        vte.setVariant(ve);
                        ve.getTranslations().add(vte);
                    }
                }

                if (v.getInventories() != null) {
                    for (ProductDetailVariantInventory inv : v.getInventories()) {
                        ProductDetailVariantInventoryEntity ie = toInventoryEntity(inv);
                        ie.setVariant(ve);
                        ve.getInventories().add(ie);
                    }
                }

                entity.getVariants().add(ve);
            }
        }

        return entity;
    }
}
