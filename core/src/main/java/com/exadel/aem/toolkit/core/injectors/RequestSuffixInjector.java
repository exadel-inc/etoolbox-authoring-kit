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
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestSuffix;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
/**
 * Injects into a Sling model the value of the {@code suffix} or {@code suffixResource} properties
 * of the {@link SlingHttpServletRequest} obtained via {@link org.apache.sling.api.request.RequestPathInfo}
 * @see RequestSuffix
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class RequestSuffixInjector extends BaseInjectorTemplateMethod<RequestSuffix> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestSuffixInjector.class);

    public static final String NAME = "eak-request-suffix-injector";

    /**
     * Retrieves the name of the current instance
     * @return String value
     * @see Injector
     */
    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    public RequestSuffix getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestSuffix.class);
    }

    @Override
    public Object getValue(Object adaptable, String name, Type type, RequestSuffix annotation) {

        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);

        if(request == null) {
            return null;
        }

        if (TypeUtil.isValidObjectType(type, String.class)) {
            return request.getRequestPathInfo().getSuffix();

        } else if (type.equals(Resource.class)) {
            return request.getRequestPathInfo().getSuffixResource();
        }
        return null;
    }

    @Override
    public void logError(Object message) {
        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
    }
}
