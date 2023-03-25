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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.InstantiationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Injects into a Sling model a child resource or a secondary model that is adapted from a child resource
 * @see Child
 * @see BaseInjector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING
)
public class ChildInjector extends BaseInjector<Child> {

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
     * {@inheritDoc}
     */
    @Override
    Child getAnnotationType(AnnotatedElement element) {
        return element.getDeclaredAnnotation(Child.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(Object adaptable, String name, Type type, Child annotation) {

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
            annotation.postfix()
        );

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
        return null;
    }
}
