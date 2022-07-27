package com.exadel.aem.toolkit.core.injectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class RequestAttributeInjector extends BaseInjectorTemplateMethod<RequestAttribute> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestAttributeInjector.class);

    public static final String NAME = "eak-request-attribute-injector";


    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(
        Object adaptable,
        String name,
        Type type,
        RequestAttribute annotation) {

        Object result;

        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);
        name = StringUtils.defaultIfEmpty(annotation.name(), name);
        Object attribute = request.getAttribute(name);

        if (Objects.isNull(attribute)) {
            return null;
        }

        result = getByType(attribute, Integer.class, type)
            .orElse(getByType(attribute, Long.class, type)
                .orElse(getByType(attribute, Boolean.class, type)
                    .orElse(getByType(attribute, Calendar.class, type)
                        .orElse(getByType(attribute, String.class, type)
                            .orElse(attribute)))));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestAttribute getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestAttribute.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logError(Type type) {
        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
    }

    /**
     * Attempts to cast object to one of supported type
     * @param attribute        A {@link SlingHttpServletRequest} attribute value
     * @param type             Type of receiving Java class member
     * @param comparingType    The value to compare to determine the type of the variable from the request.
     *                         Valid class types:
     *                         Object
     *                         Custom object
     *                         Array of Object, Integer, Long, Boolean, Calendar + primitives
     *                         List of Object, Integer, Long, Boolean, Calendar
     * @return {@code Optional} wrapped object
     */
    private <T> Optional<Object> getByType(Object attribute,
                                           Class<T> comparingType,
                                           Type type) {

        Optional<Object> result = Optional.empty();

        if (TypeUtil.isValidObjectType(type, comparingType)) {
            return Optional.ofNullable(attribute);

        } else if (TypeUtil.isValidArray(type, comparingType)) {
            Class<?> componentType = ((Class<?>) type).getComponentType();

            if (componentType.isPrimitive()) {
                return Optional.of(unwrapArray(attribute, componentType));
            } else {
                return Optional.of(wrapArray(attribute, componentType));
            }

        } else if (TypeUtil.isValidCollection(type, comparingType)) {
            List<T> attributesList;

            if (attribute.getClass().equals(Object[].class)) {
                Object[] array = (Object[]) attribute;
                T[] parametrizedArray = (T[]) wrapArray(array, comparingType);
                attributesList = Arrays.asList(parametrizedArray);
            } else {
                attributesList = (List<T>) attribute;
            }

            if (CollectionUtils.isNotEmpty(attributesList)) {
                return Optional.of(attributesList);
            }
        }

        return result;
    }

    private Object unwrapArray(Object wrapperArray, Class<?> primitiveType) {
        int length = Array.getLength(wrapperArray);
        Object primitiveArray = Array.newInstance(primitiveType, length);

        for (int i = 0; i < length; ++i) {
            Array.set(primitiveArray, i, Array.get(wrapperArray, i));
        }

        return primitiveArray;
    }

    private Object wrapArray(Object primitiveArray, Class<?> wrapperType) {
        int length = Array.getLength(primitiveArray);
        Object wrapperArray = Array.newInstance(wrapperType, length);

        for (int i = 0; i < length; ++i) {
            Array.set(wrapperArray, i, Array.get(primitiveArray, i));
        }

        return wrapperArray;
    }
}
