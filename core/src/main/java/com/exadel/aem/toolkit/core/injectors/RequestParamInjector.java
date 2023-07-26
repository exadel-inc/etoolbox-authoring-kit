/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestParam;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.CastUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Provides injecting into a Sling model the value of an HTTP request parameter (multiple parameters) obtained
 * via a {@code SlingHttpServletRequest} object
 * @see RequestParam
 * @see BaseInjector
 */
@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class RequestParamInjector extends BaseInjector<RequestParam> {

    public static final String NAME = "eak-request-parameter-injector";

    /**
     * Retrieves the name of the current instance
     * @return String value
     * @see Injector
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
    public RequestParam getManagedAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestParam.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(
        Object adaptable,
        String name,
        Type type,
        RequestParam annotation, AnnotatedElement element) {

        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);
        if (request == null) {
            return null;
        }
        return getValue(request, annotation.name().isEmpty() ? name : annotation.name(), type, element);
    }

    /**
     * Extracts a parameter value from the given {@link SlingHttpServletRequest} object and casts it to the given type
     * @param request A {@code SlingHttpServletRequest} instance
     * @param name    Name of the parameter
     * @param type    Type of the returned value
     * @return A nullable value
     */
    Object getValue(SlingHttpServletRequest request, String name, Type type, AnnotatedElement element) {
        if (RequestParameter.class.equals(type) || Object.class.equals(type)) {
            return request.getRequestParameter(name);
        } else if (TypeUtil.isArrayOfType(type, RequestParameter.class)) {
            return request.getRequestParameters(name);
        } else if (TypeUtil.isSupportedCollectionOfType(type, RequestParameter.class, false)) {
            return CastUtil.toType(request.getRequestParameterList(), type);
        } else if (RequestParameterMap.class.equals(type)) {
            return request.getRequestParameterMap();
        }

        Class<?> elementType = TypeUtil.getElementType(type);
        if (ClassUtils.isPrimitiveOrWrapper(elementType) || ClassUtils.isAssignable(elementType, String.class)) {
            Default defaultAnnotation = element.getDeclaredAnnotation(Default.class);
            String[] requestParameterValues = getRequestParameterValues(request, name);
            Object values = ArrayUtils.isEmpty(requestParameterValues) && defaultAnnotation != null ? getDefaultValue(defaultAnnotation) : requestParameterValues;
            return CastUtil.toType(values, type);
        }

        return null;
    }

    /**
     * Retrieves query parameter(-s) by the given name extracted from the current Sling request
     * @param request {@code SlingHttpServletRequest} instance
     * @param name    The parameter name to filter the request parameters with
     * @return A non-null string array (can be empty)
     */
    private String[] getRequestParameterValues(SlingHttpServletRequest request, String name) {
        return request
            .getRequestParameterList()
            .stream()
            .filter(requestParameter -> StringUtils.equals(name, requestParameter.getName()))
            .map(RequestParameter::getString)
            .filter(StringUtils::isNotEmpty)
            .toArray(String[]::new);
    }

    private Object getDefaultValue(Default defaultAnnotation) {
         if (ArrayUtils.isNotEmpty(defaultAnnotation.values())) {
             return defaultAnnotation.values();
         } else if (ArrayUtils.isNotEmpty(defaultAnnotation.booleanValues())) {
             return defaultAnnotation.booleanValues();
         } else if (ArrayUtils.isNotEmpty(defaultAnnotation.doubleValues())) {
             return defaultAnnotation.doubleValues();
             } else if (ArrayUtils.isNotEmpty(defaultAnnotation.floatValues())) {
             return defaultAnnotation.floatValues();
         } else if (ArrayUtils.isNotEmpty(defaultAnnotation.intValues())) {
             return defaultAnnotation.intValues();
         } else if (ArrayUtils.isNotEmpty(defaultAnnotation.longValues())) {
             return defaultAnnotation.longValues();
         } else if (ArrayUtils.isNotEmpty(defaultAnnotation.shortValues())) {
             return defaultAnnotation.shortValues();
         } else {
             return new String[0];
         }
    }

}
