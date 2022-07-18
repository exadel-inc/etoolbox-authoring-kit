package com.exadel.aem.toolkit.core.injectors;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;

@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class RequestAttributeInjector extends BaseInjectorTemplateMethod<RequestAttribute, SlingHttpServletRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestAttributeInjector.class);

    public static final String NAME = "eak-request-attribute-injector";


    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @CheckForNull
    @Override
    public Object getValue(

        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry disposalCallbackRegistry

    ) {
        return super.getValue(adaptable, name, type, element, disposalCallbackRegistry);
    }
    @Override
    public Supplier<Object> getAnnotationValueSupplier(
        Object request,
        String name,
        Type type,
        AnnotatedElement element,
        DisposalCallbackRegistry disposalCallbackRegistry,
        RequestAttribute annotation
    ) {
        String finalName = StringUtils.defaultIfEmpty(annotation.name(), name);

        return () -> getValueFromRequest((SlingHttpServletRequest) request,finalName);
    }

    @Override
    public RequestAttribute getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestAttribute.class);
    }
    @Override
    public SlingHttpServletRequest getAdaptable(Object object) {
        return AdaptationUtil.getRequest(object);
    }
    @Override
    public void defaultMessage() {
        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
    }


    private Object getValueFromRequest(
        SlingHttpServletRequest request,
        String paramName) {
        return request.getAttribute(paramName);
    }
}
