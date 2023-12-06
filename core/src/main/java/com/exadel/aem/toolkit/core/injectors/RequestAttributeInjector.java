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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.Injector;
import org.apache.sling.models.spi.injectorspecific.StaticInjectAnnotationProcessorFactory;
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
    service = {Injector.class, StaticInjectAnnotationProcessorFactory.class},
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class RequestAttributeInjector extends DefaultAwareInjector<RequestAttribute> {

    public static final String NAME = "eak-request-attribute-injector";

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
    @Nonnull
    @Override
    public Injectable getValue(Object adaptable, String name, Type type, RequestAttribute annotation) {
        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);
        if (request == null) {
            return Injectable.EMPTY;
        }
        Object value = getValue(request, annotation.name().isEmpty() ? name : annotation.name(), type);
        return Injectable.of(value);
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
    public RequestAttribute getManagedAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestAttribute.class);
    }
}
