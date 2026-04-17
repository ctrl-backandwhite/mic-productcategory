package com.backandwhite.provider;

import com.backandwhite.api.dto.in.BrandDtoIn;
import com.backandwhite.api.dto.out.BrandDtoOut;
import com.backandwhite.domain.model.Brand;
import com.backandwhite.domain.valueobject.BrandStatus;
import com.backandwhite.infrastructure.db.postgres.entity.BrandEntity;

public final class BrandProvider {

    public static final String BRAND_ID = "brand-001";
    public static final String BRAND_NAME = "TechBrand";
    public static final String BRAND_SLUG = "techbrand";
    public static final String BRAND_LOGO_URL = "https://cdn.example.com/logo.png";
    public static final String BRAND_WEBSITE_URL = "https://techbrand.com";
    public static final String BRAND_DESCRIPTION = "Premium tech accessories";
    public static final BrandStatus BRAND_STATUS = BrandStatus.ACTIVE;
    public static final Long BRAND_PRODUCT_COUNT = 42L;

    private BrandProvider() {
    }

    public static Brand brand() {
        return Brand.builder().id(BRAND_ID).name(BRAND_NAME).slug(BRAND_SLUG).logoUrl(BRAND_LOGO_URL)
                .websiteUrl(BRAND_WEBSITE_URL).description(BRAND_DESCRIPTION).status(BRAND_STATUS)
                .productCount(BRAND_PRODUCT_COUNT).createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT).build();
    }

    public static BrandEntity brandEntity() {
        return BrandEntity.builder().id(BRAND_ID).name(BRAND_NAME).slug(BRAND_SLUG).logoUrl(BRAND_LOGO_URL)
                .websiteUrl(BRAND_WEBSITE_URL).description(BRAND_DESCRIPTION).status(BRAND_STATUS).build();
    }

    public static BrandDtoIn brandDtoIn() {
        return BrandDtoIn.builder().name(BRAND_NAME).slug(BRAND_SLUG).logoUrl(BRAND_LOGO_URL)
                .websiteUrl(BRAND_WEBSITE_URL).description(BRAND_DESCRIPTION).build();
    }

    public static BrandDtoOut brandDtoOut() {
        return BrandDtoOut.builder().id(BRAND_ID).name(BRAND_NAME).slug(BRAND_SLUG).logoUrl(BRAND_LOGO_URL)
                .websiteUrl(BRAND_WEBSITE_URL).description(BRAND_DESCRIPTION).status(BRAND_STATUS)
                .productCount(BRAND_PRODUCT_COUNT).createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT).build();
    }
}
