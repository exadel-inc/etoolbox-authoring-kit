package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;
/**
 * Injects into a Sling model the value of a HTTP request attribute
 * via a {@code SlingHttpServletRequest} object
 * @see RequestAttribute
 * @see BaseInjector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class RequestAttributeInjector extends BaseInjector<RequestAttribute> {

    public static final String NAME = "eak-request-attribute-injector";

    public static final Class<?>[] ALLOWED_TYPES = new Class[] {
        Integer.class,
        Long.class,
        Boolean.class,
        Calendar.class,
        String.class
    };

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

        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);
        if (Objects.isNull(request)) {
            return null;
        }

        String attributeName = StringUtils.defaultIfBlank(annotation.name(), name);
        Object attribute = request.getAttribute(attributeName);
        if (Objects.isNull(attribute)) {
            return null;
        }

        return Stream.of(ALLOWED_TYPES)
            .map(clazz -> getByType(attribute, clazz, type))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestAttribute getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestAttribute.class);
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
                //unwrap array
                return Optional.of(transformArray(attribute, componentType));
            } else {
                //wrap array
                return Optional.of(transformArray(attribute, componentType));
            }

        } else if (TypeUtil.isValidCollection(type, comparingType)) {
            List<T> attributesList;

            if (attribute.getClass().equals(Object[].class)) {
                Object[] array = (Object[]) attribute;
                //unwrap array
                T[] parametrizedArray = (T[]) transformArray(array, comparingType);
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

    private Object transformArray(Object primitiveArray, Class<?> wrapperType) {
        int length = Array.getLength(primitiveArray);
        Object wrapperArray = Array.newInstance(wrapperType, length);

        for (int i = 0; i < length; ++i) {
            Array.set(wrapperArray, i, Array.get(primitiveArray, i));
        }

        return wrapperArray;
    }
}
