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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.injectors.annotations.RequestSelectors;

@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + Integer.MAX_VALUE
)
public class RequestSelectorsInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(RequestSelectorsInjector.class);
    public static final String NAME = "eak-request-selectors-injector";
    private static final String REQUEST_SELECTORS_ERROR_MESSAGE = "RequestSelectorsInjector doesn't support type {}";

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @CheckForNull
    @Override
    public Object getValue(final @Nonnull Object adaptable,
                           final String name,
                           final @Nonnull Type type,
                           final AnnotatedElement element,
                           final @Nonnull DisposalCallbackRegistry callbackRegistry) {

        RequestSelectors annotation = element.getDeclaredAnnotation(RequestSelectors.class);

        if (annotation == null) {
            return null;
        }

        SlingHttpServletRequest request = InjectorUtils.getSlingHttpServletRequest(adaptable);
        if (request == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            Class<?> collectionType = (Class<?>) ((ParameterizedType) type).getRawType();
            if (!(ClassUtils.isAssignable(collectionType, Collection.class))) {
                LOG.debug(REQUEST_SELECTORS_ERROR_MESSAGE, type);
                return null;
            }
            return Arrays.asList(request.getRequestPathInfo().getSelectors());
        } else if (((Class<?>) type).isArray() && ((Class<?>) type).getComponentType().equals(String.class)) {
            return request.getRequestPathInfo().getSelectors();
        } else if (type.equals(String.class) || type.equals(Object.class)) {
            return request.getRequestPathInfo().getSelectorString();
        }
        LOG.debug(REQUEST_SELECTORS_ERROR_MESSAGE, type);
        return null;
    }
}
