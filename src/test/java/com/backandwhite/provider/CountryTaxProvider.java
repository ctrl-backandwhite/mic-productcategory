package com.backandwhite.provider;

import com.backandwhite.api.dto.in.CountryTaxDtoIn;
import com.backandwhite.api.dto.out.CountryTaxDtoOut;
import com.backandwhite.domain.model.CountryTax;
import com.backandwhite.domain.valueobject.TaxType;
import com.backandwhite.infrastructure.db.postgres.entity.CountryTaxEntity;
import java.math.BigDecimal;
import java.util.List;

public final class CountryTaxProvider {

    public static final String TAX_ID = "tax-001";
    public static final String TAX_COUNTRY_CODE = "US";
    public static final String TAX_REGION = "CA";
    public static final BigDecimal TAX_RATE = new BigDecimal("8.250000");
    public static final TaxType TAX_TYPE = TaxType.PERCENTAGE;
    public static final String TAX_APPLIES_TO = "electronics";
    public static final Boolean TAX_INCLUDES_SHIPPING = true;
    public static final Boolean TAX_ACTIVE = true;

    private CountryTaxProvider() {
    }

    public static CountryTax countryTax() {
        return CountryTax.builder().id(TAX_ID).countryCode(TAX_COUNTRY_CODE).region(TAX_REGION).rate(TAX_RATE)
                .type(TAX_TYPE).appliesTo(TAX_APPLIES_TO).includesShipping(TAX_INCLUDES_SHIPPING).active(TAX_ACTIVE)
                .createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT).build();
    }

    public static CountryTaxEntity countryTaxEntity() {
        return CountryTaxEntity.builder().id(TAX_ID).countryCode(TAX_COUNTRY_CODE).region(TAX_REGION).rate(TAX_RATE)
                .type(TAX_TYPE).appliesTo(TAX_APPLIES_TO).includesShipping(TAX_INCLUDES_SHIPPING).active(TAX_ACTIVE)
                .build();
    }

    public static CountryTaxDtoIn countryTaxDtoIn() {
        return CountryTaxDtoIn.builder().country(TAX_COUNTRY_CODE).region(TAX_REGION).rate(TAX_RATE).type(TAX_TYPE)
                .appliesToCategories(List.of(TAX_APPLIES_TO)).active(TAX_ACTIVE).build();
    }

    public static CountryTaxDtoOut countryTaxDtoOut() {
        return CountryTaxDtoOut.builder().id(TAX_ID).country(TAX_COUNTRY_CODE).region(TAX_REGION).rate(TAX_RATE)
                .type(TAX_TYPE.name()).appliesToCategories(List.of(TAX_APPLIES_TO)).active(TAX_ACTIVE)
                .createdAt(AuditProvider.CREATED_AT).updatedAt(AuditProvider.UPDATED_AT).build();
    }
}
