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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestParameterMap;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestParam;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;
/**
 * Provides injecting into a Sling model the value of an HTTP request parameter (multiple parameters) obtained
 * via a {@code SlingHttpServletRequest} object
 * @see RequestParam
 * @see BaseInjector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING
)
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
    public RequestParam getAnnotationType(AnnotatedElement element) {
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
        RequestParam annotation) {

        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);
        if(request == null) {
            return null;
        }

        String paramName = annotation.name().isEmpty() ? name : annotation.name();

        if (TypeUtil.isValidObjectType(type, String.class)) {
            return request.getParameter(paramName);

        } else if (TypeUtil.isValidArray(type, String.class)) {
            return getFilteredRequestParameters(request, paramName)
                .map(RequestParameter::getString)
                .toArray(String[]::new);

        } else if (TypeUtil.isValidCollection(type, String.class)) {
            return getFilteredRequestParameters(request, paramName)
                .map(RequestParameter::getString)
                .collect(Collectors.toList());

        } else if (TypeUtil.isValidObjectType(type, RequestParameter.class)) {
            return request.getRequestParameter(paramName);

        } else if (TypeUtil.isValidCollection(type, RequestParameter.class)) {
            return request.getRequestParameterList();

        } else if (TypeUtil.isValidArray(type, RequestParameter.class)) {
            return request.getRequestParameters(paramName);

        } else if (TypeUtil.isValidObjectType(type, RequestParameterMap.class)) {
            return request.getRequestParameterMap();
        }

        return null;
    }

    /**
     * Retrieves the stream of {@link RequestParameter} objects extracted from the current Sling request filtered with
     * the given parameter name
     * @param request {@code SlingHttpServletRequest} instance
     * @param name    The parameter name to filter the request parameters with
     * @return {@code Stream} object containing matched request parameters
     */
    private Stream<RequestParameter> getFilteredRequestParameters(SlingHttpServletRequest request, String name) {
        return request
            .getRequestParameterList()
            .stream()
            .filter(requestParameter -> StringUtils.equals(name, requestParameter.getName()));
    }

}
