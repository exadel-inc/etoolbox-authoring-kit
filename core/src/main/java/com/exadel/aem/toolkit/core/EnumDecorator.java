package com.exadel.aem.toolkit.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class EnumDecorator {

    private final Class<? extends Enum<?>> enumToWrap;


    public EnumDecorator(Class<? extends Enum<?>> enumToWrap) {
        this.enumToWrap = enumToWrap;
    }

    public Object getEnumFieldValue(String fieldName, String constantName) {
        try {
            Field field = enumToWrap.getField(fieldName);

            Enum<?> anEnum = getEnumConstantByName(constantName);

            return field.get(anEnum);

        } catch (Exception e) {
            return null;
        }
    }

    public Object  returnValueByMethodInvoke(String methodName, String constantName) {
        try {
            Method declaredMethod = enumToWrap.getMethod(methodName);

            Enum<?> anEnum = getEnumConstantByName(constantName);

            return declaredMethod.invoke(anEnum);

        } catch (Exception e) {
          return null;
        }
    }

    public Enum<?> getEnumConstantByName(String constantName) {
        return Arrays.stream(enumToWrap.getEnumConstants())
            .filter(constant -> constantName.equals(String.valueOf(constant)))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
