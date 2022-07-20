package com.exadel.aem.toolkit.core.injectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;

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

    @Override
    public Object getValue(
        Object adaptable,
        String name,
        Type type,
        RequestAttribute annotation
    ) {
        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);

        return request.getAttribute(
            StringUtils.defaultIfEmpty(annotation.name(), name)
        );
    }

    @Override
    public RequestAttribute getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestAttribute.class);
    }
    @Override
    public void logError(Object message) {
        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
    }
}
