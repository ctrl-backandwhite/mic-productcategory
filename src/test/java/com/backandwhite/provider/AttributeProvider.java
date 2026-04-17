package com.backandwhite.provider;

import com.backandwhite.api.dto.in.AttributeDtoIn;
import com.backandwhite.api.dto.in.AttributeValueDtoIn;
import com.backandwhite.api.dto.out.AttributeDtoOut;
import com.backandwhite.api.dto.out.AttributeValueDtoOut;
import com.backandwhite.domain.model.Attribute;
import com.backandwhite.domain.model.AttributeValue;
import com.backandwhite.domain.valueobject.AttributeType;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeEntity;
import com.backandwhite.infrastructure.db.postgres.entity.AttributeValueEntity;
import java.util.List;

public final class AttributeProvider {

    public static final String ATTRIBUTE_ID = "attr-color-001";
    public static final String ATTRIBUTE_NAME = "Color";
    public static final String ATTRIBUTE_SLUG = "color";
    public static final AttributeType ATTRIBUTE_TYPE = AttributeType.COLOR;
    public static final Long ATTRIBUTE_USED_IN_PRODUCTS = 15L;

    public static final String VALUE_ID = "val-red-001";
    public static final String VALUE_TEXT = "Red";
    public static final String VALUE_COLOR_HEX = "#FF0000";
    public static final Integer VALUE_POSITION = 0;

    private AttributeProvider() {
    }

    public static Attribute attribute() {
        return Attribute.builder().id(ATTRIBUTE_ID).name(ATTRIBUTE_NAME).slug(ATTRIBUTE_SLUG).type(ATTRIBUTE_TYPE)
                .usedInProducts(ATTRIBUTE_USED_IN_PRODUCTS).createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT)
                .values(List.of(AttributeValue.builder().id(VALUE_ID).attributeId(ATTRIBUTE_ID).value(VALUE_TEXT)
                        .colorHex(VALUE_COLOR_HEX).position(VALUE_POSITION).build()))
                .build();
    }

    public static AttributeEntity attributeEntity() {
        AttributeEntity entity = AttributeEntity.builder().id(ATTRIBUTE_ID).name(ATTRIBUTE_NAME).slug(ATTRIBUTE_SLUG)
                .type(ATTRIBUTE_TYPE).build();
        entity.setValues(List.of(AttributeValueEntity.builder().id(VALUE_ID).value(VALUE_TEXT).colorHex(VALUE_COLOR_HEX)
                .position(VALUE_POSITION).attribute(entity).build()));
        return entity;
    }

    public static AttributeDtoIn attributeDtoIn() {
        return AttributeDtoIn.builder().name(ATTRIBUTE_NAME).slug(ATTRIBUTE_SLUG).type(ATTRIBUTE_TYPE)
                .values(List.of(AttributeValueDtoIn.builder().value(VALUE_TEXT).colorHex(VALUE_COLOR_HEX).build()))
                .build();
    }

    public static AttributeDtoOut attributeDtoOut() {
        return AttributeDtoOut.builder().id(ATTRIBUTE_ID).name(ATTRIBUTE_NAME).slug(ATTRIBUTE_SLUG).type(ATTRIBUTE_TYPE)
                .usedInProducts(ATTRIBUTE_USED_IN_PRODUCTS).createdAt(AuditProvider.CREATED_AT)
                .updatedAt(AuditProvider.UPDATED_AT).values(List.of(AttributeValueDtoOut.builder().id(VALUE_ID)
                        .value(VALUE_TEXT).colorHex(VALUE_COLOR_HEX).position(VALUE_POSITION).build()))
                .build();
    }
}
