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
package com.exadel.aem.toolkit.api.annotations.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {Injector.class},
    property = {Constants.SERVICE_RANKING + ":Integer=" + Integer.MIN_VALUE}
)
public class RequestSelectorsInjector implements Injector {

    public static final String NAME = "request-selectors-injector";
    private static final Logger LOG = LoggerFactory.getLogger(RequestSelectorsInjector.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getValue(final Object adaptable, final String name, final Type type,
                           final AnnotatedElement element, final DisposalCallbackRegistry callbackRegistry) {

        final RequestSelectors annotation = element.getAnnotation(RequestSelectors.class);

        if (annotation == null) {
            return null;
        }

        SlingHttpServletRequest request = InjectorUtils.getSlingHttpServletRequest(adaptable);
        if (request == null) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            Class<?> collectionType = (Class<?>) ((ParameterizedType) type).getRawType();
            if (!(collectionType.equals(Collection.class) || collectionType.equals(List.class))) {
                LOG.debug("RequestSelectorsInjector doesn't support Collection Type {}", type);
                return null;
            } else {
                return Arrays.asList(request.getRequestPathInfo().getSelectors());
            }
        } else if (((Class<?>) type).isArray() && type.equals(String[].class)) {
            return request.getRequestPathInfo().getSelectors();
        } else if (type.equals(String.class)) {
            return request.getRequestPathInfo().getSelectorString();
        } else {
            LOG.debug("RequestSelectorsInjector doesn't support Type {}", type);
            return null;
        }
    }
}
