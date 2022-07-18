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
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;
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
public class ChildInjector extends BaseInjectorTemplateMethod<Child> {

    private static final Logger LOG = LoggerFactory.getLogger(ChildInjector.class);

    public static final String NAME = "eak-child-resource-injector";

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

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private AdapterManager adapterManager;

    @Override
    public Child getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(Child.class);
    }
    @Override
    public Supplier<Object> getAnnotationValueSupplier(SlingHttpServletRequest request, String name, Type type, Child annotation) {
        return () -> {

            Resource adaptableResource = request.getResource();

            String resourcePath = StringUtils.defaultIfBlank(annotation.name(), name);

            Resource currentResource = adaptableResource.getChild(resourcePath);

            if (currentResource == null) {
                return null;
            }

            Resource preparedResource = InstantiationUtil.getFilteredResource(
                currentResource,
                annotation.prefix(),
                annotation.postfix()
            );

            if (TypeUtil.isValidObjectType(type, Resource.class)) {

                return preparedResource;

            } else if (type instanceof Class) {

                if (TypeUtil.isSlingRequestAdapter(modelFactory, type)) {
                    return adapterManager.getAdapter(request, (Class<?>) type);
                }

                return preparedResource.adaptTo((Class<?>) type);
            }
            return null;
        };
    }
    @Override
    public void defaultMessage() {
        //LOG.debug("Failed to inject child resource by the name \"{}\"", resourcePath);
    }
}
