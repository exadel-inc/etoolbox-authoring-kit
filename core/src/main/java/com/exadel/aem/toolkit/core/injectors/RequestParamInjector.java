package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.injectors.annotations.RequestParam;

@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + Integer.MAX_VALUE
)
public class RequestParamInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(RequestParamInjector.class);
    public static final String NAME = "eak-request-parameter-injector";

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }


    @CheckForNull
    @Override
    public Object getValue(final @Nonnull Object adaptable,
                           final String name,
                           final @Nonnull Type type,
                           final @Nonnull AnnotatedElement element,
                           final @Nonnull DisposalCallbackRegistry callbackRegistry) {

        final RequestParam annotation = element.getAnnotation(RequestParam.class);

        if (annotation == null) {
            return null;
        }

        String paramName = annotation.name().isEmpty() ? name : annotation.name();

        SlingHttpServletRequest request = InjectorUtils.getSlingHttpServletRequest(adaptable);
        if (request == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            Class<?> collectionType = (Class<?>) ((ParameterizedType) type).getRawType();
            if (!(ClassUtils.isAssignable(collectionType, Collection.class))) {
                LOG.debug("RequestParameterInjector doesn't support Collection Type {}", type);
                return null;
            }
            return request.getRequestParameterList();
        } else if (((Class<?>) type).isArray() && ((Class<?>) type).getComponentType().equals(RequestParameter.class)) {
            return request.getRequestParameters(paramName);
        } else if (type.equals(RequestParameterMap.class)) {
            return request.getRequestParameterMap();
        } else if (type.equals(String.class) || type.equals(Object.class)) {
            return request.getParameter(paramName);
        } else if (type.equals(RequestParameter.class)) {
            return request.getRequestParameter(paramName);
        }
        LOG.debug("RequestParameterInjector doesn't support type {}", type);
        return null;
    }
}
