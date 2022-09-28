package com.exadel.aem.toolkit.core.injectors;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.injectors.EnumValue;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;

@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class ValueMapEnumValueInjector extends BaseInjector<EnumValue> {
    private static final Logger LOG = LoggerFactory.getLogger(ValueMapEnumValueInjector.class);

    public static final String NAME = "eak-etoolbox-enum-injector";

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    EnumValue getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(EnumValue.class);
    }

    @SuppressWarnings("uncheked")
    @Override
    Object getValue(
        Object adaptable,
        String name,
        Type type,
        EnumValue annotation,
        Object defaultValue) {

        if (!isValid(type)) {
            return null;
        }

        DeepReadValueMapDecorator valueMap = new DeepReadValueMapDecorator(
            AdaptationUtil.getResource(adaptable),
            AdaptationUtil.getValueMap(adaptable));

        String fieldName = annotation.name().isEmpty() ? name : annotation.name();
        Object object = valueMap.get(fieldName);
        String[] stringValue = object != null ? String.valueOf(object).split(",") : null;

        Class<?> componentType = TypeUtil.extractComponentType(type);
        if (componentType == null) {
            return null;
        }

        Object anEnum = getEnum((Class<?>) type, stringValue);
        if (anEnum == null && !annotation.fieldName().isEmpty()) {
            String paramName = annotation.fieldName();
            anEnum = getEnumByParam(componentType, paramName, stringValue);
        }

        if (anEnum == null && defaultValue != null) {
            anEnum = getEnum((Class<?>) type, (String[]) defaultValue);
        }

        return anEnum;
    }
    private boolean isValid(Type type) {
        return (type instanceof Class && ((Class<?>) type).isEnum()) || TypeUtil.isValidArray(type) || TypeUtil.isValidCollection(type);
    }

    private Object getEnum(Class<?> type, String[] stringValue) {
        if (stringValue == null) {
            return null;
        }

        Class<?> componentType = TypeUtil.extractComponentType(type);
        if(componentType == null) {
            return null;
        }

        if (stringValue.length == 1) {
            return getSingle(componentType, String.valueOf(stringValue[0]));
        } else {
            List<Object> objects = Arrays.asList(stringValue);
            Object[] array = Arrays.stream(componentType.getEnumConstants()).filter(e -> objects.contains(String.valueOf(e))).toArray();
            Object[] enumTypeArray = (Object[]) TypeUtil.transformArray(array, componentType);

            if (type.isArray()) {
                return enumTypeArray;
            } else {
                return Arrays.asList(enumTypeArray);
            }
        }
    }

    private Object getEnumByParam(Type type, String paramName, String[] stringValue) {
        if(stringValue == null) {
            return null;
        }

        Field param = Arrays.stream(((Class<?>) type).getDeclaredFields())
            .filter(x -> !x.isEnumConstant() && !x.isSynthetic())
            .filter(x -> StringUtils.equals(paramName, x.getName())).findFirst().orElse(null);

        if (param == null) {
            return null;
        }

        if (!param.isAccessible()) {
            param.setAccessible(true);
        }

        for (Object enumConstant : ((Class<?>) type).getEnumConstants()) {
            try {
                Object constant = param.get(enumConstant);
                if (StringUtils.equals(stringValue[0], String.valueOf(constant))) {
                    param.setAccessible(false);
                    return enumConstant;
                }
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage());
            }
        }
        return null;
    }

    private Object getSingle(Class<?> type, String stringValue) {
        return Arrays.stream(type.getEnumConstants())
            .filter(e -> StringUtils.equals(stringValue, String.valueOf(e)))
            .findFirst()
            .orElse(null);
    }
}
