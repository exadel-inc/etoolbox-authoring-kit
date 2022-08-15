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
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.InstantiationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Injects into a Sling model a child resource or a secondary model that is adapted from a child resource
 * @see Child
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class ChildInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(ChildInjector.class);

    public static final String NAME = "eak-child-resource-injector";

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private AdapterManager adapterManager;

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
     * Attempts to inject a value into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve
     *                         annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return {@code Resource} or adapted object if successful. Otherwise, null is returned
     */
    @CheckForNull
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        Child annotation = element.getDeclaredAnnotation(Child.class);
        if (annotation == null) {
            return null;
        }

        Resource adaptableResource = AdaptationUtil.getResource(adaptable);
        if (adaptableResource == null) {
            return null;
        }

        String resourcePath = StringUtils.defaultIfBlank(annotation.name(), name);
        Resource currentResource = adaptableResource.getChild(resourcePath);
        if (currentResource == null) {
            return null;
        }

        Resource preparedResource = InstantiationUtil.getFilteredResource(
            currentResource,
            annotation.prefix(),
            annotation.postfix());

        if (TypeUtil.isValidObjectType(type, Resource.class)) {
            return preparedResource;
        } else if (type instanceof Class) {
            if (adaptable instanceof SlingHttpServletRequest && TypeUtil.isSlingRequestAdapter(modelFactory, type)) {
                return adapterManager.getAdapter(
                    AdaptationUtil.getRequest((SlingHttpServletRequest) adaptable, preparedResource),
                    (Class<?>) type);
            }
            return preparedResource.adaptTo((Class<?>) type);
        }

        LOG.debug("Failed to inject child resource by the name \"{}\"", resourcePath);
        return null;
    }
}
