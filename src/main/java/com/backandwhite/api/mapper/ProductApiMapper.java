package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.ProductDtoIn;
import com.backandwhite.api.dto.in.ProductTranslationDtoIn;
import com.backandwhite.api.dto.out.*;
import com.backandwhite.common.domain.valueobject.Money;
import com.backandwhite.domain.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductApiMapper {

    ProductDtoOut toDto(Product product);

    List<ProductDtoOut> toDtoList(List<Product> products);

    ProductTranslationDtoOut toTranslationDto(ProductTranslation translation);

    ProductDetailVariantDtoOut toVariantDto(ProductDetailVariant variant);

    ProductDetailVariantTranslationDtoOut toVariantTranslationDto(ProductDetailVariantTranslation translation);

    ProductDetailVariantInventoryDtoOut toInventoryDto(ProductDetailVariantInventory inventory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "costPrice", ignore = true)
    Product toDomain(ProductDtoIn dto);

    ProductTranslation toTranslationDomain(ProductTranslationDtoIn dto);

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
