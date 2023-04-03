package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.CastUtil;

/**
 * Provides injecting into a Sling model the value of an HTTP request attribute obtained via a {@code
 * SlingHttpServletRequest} object
 * @see RequestAttribute
 * @see BaseInjector
 */
@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class RequestAttributeInjector extends BaseInjector<RequestAttribute> {

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
    public Object getValue(Object adaptable, String name, Type type, RequestAttribute annotation) {
        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);
        if (request == null) {
            return null;
        }
        return getValue(request, annotation.name().isEmpty() ? name : annotation.name(), type);
    }

    /**
     * Extracts an attribute value from the given {@link SlingHttpServletRequest} object and casts it to the given type
     * @param request A {@code SlingHttpServletRequest} instance
     * @param name    Name of the parameter
     * @param type    Type of the returned value
     * @return A nullable value
     */
    Object getValue(SlingHttpServletRequest request, String name, Type type) {
        return CastUtil.toType(request.getAttribute(name), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestAttribute getAnnotationType(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestAttribute.class);
    }

}
