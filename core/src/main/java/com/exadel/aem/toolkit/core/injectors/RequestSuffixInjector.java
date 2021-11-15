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
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.injectors.annotations.RequestSuffix;

@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + Integer.MAX_VALUE
)
public class RequestSuffixInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(RequestSuffixInjector.class);
    public static final String NAME = "request-suffix-injector";

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    public Object getValue(final @Nonnull Object adaptable,
                           final String name,
                           final @Nonnull Type type,
                           final AnnotatedElement element,
                           final @Nonnull DisposalCallbackRegistry callbackRegistry) {

        final RequestSuffix annotation = element.getAnnotation(RequestSuffix.class);

        if (annotation == null) {
            return null;
        }

        SlingHttpServletRequest request = InjectorUtils.getSlingHttpServletRequest(adaptable);
        if (request == null) {
            return null;
        }

        if (type.equals(String.class)) {
            return request.getRequestPathInfo().getSuffix();
        } else if (type.equals(Resource.class)) {
            return request.getRequestPathInfo().getSuffixResource();
        } else if (type.equals(Object.class)) {
            return request.getRequestPathInfo().getSuffix();
        }
        LOG.debug("RequestSuffixInjector doesn't support type {}", type);
        return null;
    }
}
