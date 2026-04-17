package com.backandwhite.provider;

import com.backandwhite.api.dto.in.WarrantyDtoIn;
import com.backandwhite.api.dto.out.WarrantyDtoOut;
import com.backandwhite.domain.model.Warranty;
import com.backandwhite.domain.valueobject.WarrantyType;
import com.backandwhite.infrastructure.db.postgres.entity.WarrantyEntity;

public final class WarrantyProvider {

    public static final String WARRANTY_ID = "warranty-001";
    public static final String WARRANTY_NAME = "Standard Warranty";
    public static final WarrantyType WARRANTY_TYPE = WarrantyType.MANUFACTURER;
    public static final Integer WARRANTY_DURATION_MONTHS = 12;
    public static final String WARRANTY_COVERAGE = "Full hardware coverage";
    public static final String WARRANTY_CONDITIONS = "Normal use only";
    public static final Boolean WARRANTY_INCLUDES_LABOR = true;
    public static final Boolean WARRANTY_INCLUDES_PARTS = true;
    public static final Boolean WARRANTY_INCLUDES_PICKUP = false;
    public static final Integer WARRANTY_REPAIR_LIMIT = 3;
    public static final String WARRANTY_CONTACT_PHONE = "+1-800-555-0100";
    public static final String WARRANTY_CONTACT_EMAIL = "warranty@test.com";
    public static final Boolean WARRANTY_ACTIVE = true;
    public static final Long WARRANTY_PRODUCTS_COUNT = 10L;

    private WarrantyProvider() {
    }

    public static Warranty warranty() {
        return Warranty.builder().id(WARRANTY_ID).name(WARRANTY_NAME).type(WARRANTY_TYPE)
                .durationMonths(WARRANTY_DURATION_MONTHS).coverage(WARRANTY_COVERAGE).conditions(WARRANTY_CONDITIONS)
                .includesLabor(WARRANTY_INCLUDES_LABOR).includesParts(WARRANTY_INCLUDES_PARTS)
                .includesPickup(WARRANTY_INCLUDES_PICKUP).repairLimit(WARRANTY_REPAIR_LIMIT)
                .contactPhone(WARRANTY_CONTACT_PHONE).contactEmail(WARRANTY_CONTACT_EMAIL).active(WARRANTY_ACTIVE)
                .productsCount(WARRANTY_PRODUCTS_COUNT).createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT).build();
    }

    public static WarrantyEntity warrantyEntity() {
        return WarrantyEntity.builder().id(WARRANTY_ID).name(WARRANTY_NAME).type(WARRANTY_TYPE)
                .durationMonths(WARRANTY_DURATION_MONTHS).coverage(WARRANTY_COVERAGE).conditions(WARRANTY_CONDITIONS)
                .includesLabor(WARRANTY_INCLUDES_LABOR).includesParts(WARRANTY_INCLUDES_PARTS)
                .includesPickup(WARRANTY_INCLUDES_PICKUP).repairLimit(WARRANTY_REPAIR_LIMIT)
                .contactPhone(WARRANTY_CONTACT_PHONE).contactEmail(WARRANTY_CONTACT_EMAIL).active(WARRANTY_ACTIVE)
                .build();
    }

    public static WarrantyDtoIn warrantyDtoIn() {
        return WarrantyDtoIn.builder().name(WARRANTY_NAME).type(WARRANTY_TYPE).durationMonths(WARRANTY_DURATION_MONTHS)
                .coverage(WARRANTY_COVERAGE).conditions(WARRANTY_CONDITIONS).includesLabor(WARRANTY_INCLUDES_LABOR)
                .includesParts(WARRANTY_INCLUDES_PARTS).includesPickup(WARRANTY_INCLUDES_PICKUP)
                .repairLimit(WARRANTY_REPAIR_LIMIT).contactPhone(WARRANTY_CONTACT_PHONE)
                .contactEmail(WARRANTY_CONTACT_EMAIL).build();
    }

    public static WarrantyDtoOut warrantyDtoOut() {
        return WarrantyDtoOut.builder().id(WARRANTY_ID).name(WARRANTY_NAME).type(WARRANTY_TYPE)
                .durationMonths(WARRANTY_DURATION_MONTHS).coverage(WARRANTY_COVERAGE).conditions(WARRANTY_CONDITIONS)
                .includesLabor(WARRANTY_INCLUDES_LABOR).includesParts(WARRANTY_INCLUDES_PARTS)
                .includesPickup(WARRANTY_INCLUDES_PICKUP).repairLimit(WARRANTY_REPAIR_LIMIT)
                .contactPhone(WARRANTY_CONTACT_PHONE).contactEmail(WARRANTY_CONTACT_EMAIL).active(WARRANTY_ACTIVE)
                .productsCount(WARRANTY_PRODUCTS_COUNT).createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT).build();
    }
}
