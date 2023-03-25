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
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING
)
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
        String attributeName = annotation.name().isEmpty() ? name : annotation.name();
        return CastUtil.toType(request.getAttribute(attributeName), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestAttribute getAnnotationType(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestAttribute.class);
    }

}
