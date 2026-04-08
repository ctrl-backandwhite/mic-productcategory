package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.*;
import com.backandwhite.api.dto.out.*;
import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductDetailApiMapper {

    // ── Domain → DTO Out ────────────────────────────────────────────────────

    ProductDetailDtoOut toDto(ProductDetail detail);

    ProductDetailTranslationDtoOut toTranslationDto(ProductDetailTranslation translation);

    ProductDetailVariantDtoOut toVariantDto(ProductDetailVariant variant);

    List<ProductDetailVariantDtoOut> toVariantDtoList(List<ProductDetailVariant> variants);

    ProductDetailVariantTranslationDtoOut toVariantTranslationDto(ProductDetailVariantTranslation translation);

    ProductDetailVariantInventoryDtoOut toInventoryDto(ProductDetailVariantInventory inventory);

    // ── DTO In → Domain ─────────────────────────────────────────────────────

    @Mapping(target = "vid", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "retailPrice", ignore = true)
    ProductDetailVariant toVariantDomain(ProductDetailVariantDtoIn dto);

    ProductDetailVariantTranslation toVariantTranslationDomain(ProductDetailVariantTranslationDtoIn dto);

    @Mapping(target = "vid", ignore = true)
    ProductDetailVariantInventory toInventoryDomain(ProductDetailVariantInventoryDtoIn dto);

    default String moneyToString(Money money) {
        return money != null ? money.toPlainString() : null;
    }

    default BigDecimal moneyToBigDecimal(Money money) {
        return money != null ? money.getAmount() : null;
    }

    default Money bigDecimalToMoney(BigDecimal value) {
        return value != null ? Money.of(value) : null;
    }
}
