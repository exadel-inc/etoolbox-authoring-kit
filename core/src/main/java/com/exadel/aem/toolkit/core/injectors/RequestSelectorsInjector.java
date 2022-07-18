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
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestSelectors;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Injects into a Sling model the value of the {@code selectors} property of the {@link SlingHttpServletRequest}
 * obtained via {@link org.apache.sling.api.request.RequestPathInfo}
 * @see RequestSelectors
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class RequestSelectorsInjector extends BaseInjectorTemplateMethod<RequestSelectors, SlingHttpServletRequest>{

    private static final Logger LOG = LoggerFactory.getLogger(RequestSelectorsInjector.class);

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
     * Attempts to inject a value into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve
     *                         annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return The value to inject, or null in case injection is not possible
     * @see Injector
     */
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        return super.getValue(adaptable,name,type,element,callbackRegistry);

    }
    @Override
    public RequestSelectors getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(RequestSelectors.class);
    }
    @Override
    public SlingHttpServletRequest getAdaptable(Object object) {
        return AdaptationUtil.getRequest(object);
    }
    @Override
    public Supplier<Object> getAnnotationValueSupplier(Object request, String name, Type type, AnnotatedElement element, DisposalCallbackRegistry disposalCallbackRegistry, RequestSelectors annotation) {

        return () -> {

            if (TypeUtil.isValidCollection(type, String.class)) {
                SlingHttpServletRequest slingHttpServletRequest = (SlingHttpServletRequest) request;
                return Arrays.asList(slingHttpServletRequest.getRequestPathInfo().getSelectors());
            }
            if (TypeUtil.isValidArray(type, String.class)) {
                SlingHttpServletRequest slingHttpServletRequest = (SlingHttpServletRequest) request;
                return slingHttpServletRequest.getRequestPathInfo().getSelectors();
            }
            if (TypeUtil.isValidObjectType(type, String.class)) {
                SlingHttpServletRequest slingHttpServletRequest = (SlingHttpServletRequest) request;
                return slingHttpServletRequest.getRequestPathInfo().getSelectorString();
            }
            return null;
        };
    }
    @Override
    public void defaultMessage() {
        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
    }
}
