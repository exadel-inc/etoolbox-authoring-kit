package com.exadel.aem.toolkit.core.injectors;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;

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

    @SuppressWarnings("unchecked")
    @Override
    Object getValue(Object adaptable, String name, Type type, EnumValue annotation) {
        if (!(type instanceof Class) || !((Class<?>) type).isEnum()) {
            LOG.warn("The annotated member is not of an enum type");
            return null;
        }

        DeepReadValueMapDecorator valueMap = new DeepReadValueMapDecorator(
            AdaptationUtil.getResource(adaptable),
            AdaptationUtil.getValueMap(adaptable));

        String fieldName = annotation.name().isEmpty() ? name : annotation.name();
        Object o = valueMap.get(fieldName);
        String stringValue = String.valueOf(o);

        Object anEnum = getEnum((Class<?>) type, stringValue);
        if (anEnum == null && !annotation.fieldName().isEmpty()) {
            String paramName = annotation.fieldName();
            anEnum = getEnumByParam(type, paramName, stringValue);
        }

        return anEnum;
    }
    private Object getEnumByParam(Type type, String paramName, String stringValue) {
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
                if (StringUtils.equals(String.valueOf(stringValue), String.valueOf(constant))) {
                    param.setAccessible(false);
                    return enumConstant;
                }
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage());
            }
        }
        return null;
    }

    private Object getEnum(Class<?> type, String stringValue) {
        return Arrays.stream(type.getEnumConstants())
            .filter(e -> StringUtils.equals(stringValue, String.valueOf(e)))
            .findFirst()
            .orElse(null);
    }
}
