package com.exadel.aem.toolkit.core.injectors;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.exadel.aem.toolkit.core.EnumDecorator;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;
import org.apache.sling.models.annotations.Default;
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

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    EnumValue getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(EnumValue.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Object getDefaultValue(AnnotatedElement element, Type type) {
        Default defaultAnnotation = element.getAnnotation(Default.class);
        if (defaultAnnotation != null && isValid(type)) {
            return defaultAnnotation.values();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
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

        name = annotation.name().isEmpty() ? name : annotation.name();
        Object object = valueMap.get(name);
        String[] stringValues = object != null ? String.valueOf(object).split(",") : new String[]{};

        Class<?> componentType = TypeUtil.extractComponentType(type);
        if (componentType == null) {
            return null;
        }

        EnumDecorator decorator = new EnumDecorator((Class<? extends Enum<?>>) componentType);

        Object[] constants;
        if (!annotation.fieldName().isEmpty()) {
            constants = Arrays.stream(componentType.getEnumConstants())
                .filter(e -> String.valueOf(decorator.getEnumFieldValue(annotation.fieldName(), String.valueOf(e))).equals(stringValues[0]))
                .toArray();
        } else if (!annotation.methodName().isEmpty()) {
            constants = Arrays.stream(componentType.getEnumConstants())
                .filter(e -> String.valueOf(decorator.returnValueByMethodInvoke(annotation.fieldName(), String.valueOf(e))).equals(stringValues[0]))
                .toArray();
        } else {
            constants = Arrays.stream(stringValues)
                .map(decorator::getEnumConstantByName)
                .toArray();
        }

        if (constants.length == 0 && defaultValue != null) {
            constants = Arrays.stream((String[]) defaultValue)
                .map(decorator::getEnumConstantByName)
                .toArray();
        }
        Object[] targetTypeArray = (Object[]) TypeUtil.transformArray(constants, componentType);

        if (TypeUtil.isValidArray(type)) {
            return targetTypeArray;
        } else if (TypeUtil.isValidCollection(type)) {
            return Arrays.asList(targetTypeArray);
        } else {
            return targetTypeArray[0];
        }
    }


    /**
     * Checks the {@code type} is an enumeration. If the {@code type} is a collection, the elements of the array/collection are checked.
     * @param type Type of receiving Java class member
     * @return true/false
     */
    private boolean isValid(Type type) {
        return (type instanceof Class && ((Class<?>) type).isEnum()) || TypeUtil.isValidArray(type) || TypeUtil.isValidCollection(type);
    }
}
