package com.backandwhite.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapperTestUtils {

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set field '" + fieldName + "'", e);
        }
    }
}
