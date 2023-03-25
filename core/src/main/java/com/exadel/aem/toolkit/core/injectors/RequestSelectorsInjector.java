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
import java.util.Arrays;
import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestSelectors;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Provides injecting into a Sling model the value of the {@code selectors} property of the {@link SlingHttpServletRequest}
 * obtained via {@link org.apache.sling.api.request.RequestPathInfo}
 * @see RequestSelectors
 * @see BaseInjector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING
)
public class RequestSelectorsInjector extends BaseInjector<RequestSelectors> {

    public static final String NAME = "eak-request-selectors-injector";

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

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestSelectors getAnnotationType(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestSelectors.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(
        Object adaptable,
        String name,
        Type type,
        RequestSelectors annotation) {

        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);

        if(request == null) {
            return null;
        }

        if (TypeUtil.isValidCollection(type, String.class)) {
            return Arrays.asList(request.getRequestPathInfo().getSelectors());
        }
        if (TypeUtil.isValidArray(type, String.class)) {
            return request.getRequestPathInfo().getSelectors();
        }
        if (TypeUtil.isValidObjectType(type, String.class)) {
            return request.getRequestPathInfo().getSelectorString();
        }

        return null;
    }

}
