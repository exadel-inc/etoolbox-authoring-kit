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
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;

/**
 * Injector implementation for `@Child`
 * Injects into a Sling model a child resource or adapted object
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class ChildInjector implements Injector {

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

    /**
     * Attempts to inject {@code Resource} or adapted object
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve annotation objects
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

        Resource currentResource = InjectorUtils.getResource(adaptable);
        if (currentResource == null) {
            return null;
        }

        Resource childResource = getChildResource(currentResource, name, annotation);
        if (childResource == null) {
            return null;
        }

        if (Resource.class.equals(type)) {
            return childResource;
        } else if (type instanceof Class) {
            return childResource.adaptTo((Class<?>) type);
        }

        LOG.debug("Failed to inject child");
        return null;
    }

    /**
     * Retrieves the child {@code Resource} object according to the {@code Child} annotation parameters
     * @param currentResource  Current {@code Resource}
     * @param name        {@code String} Name of the Java class member to inject the value into
     * @param annotation  annotation objects
     * @return {@code Resource} object if success. Otherwise, null is returned
     */
    private Resource getChildResource(Resource currentResource, String name, Child annotation) {
        if (StringUtils.isNotBlank(annotation.name())) {
            return currentResource.getChild(InjectorUtils.prepareRelativePath(annotation.name()));

        } else if (StringUtils.isNotBlank(annotation.prefix())) {
            Resource actualParent = InjectorUtils.getLastParentResource(currentResource, annotation.prefix());
            return getFilteredResource(actualParent, InjectorUtils.getPatternPredicate(annotation.prefix(), InjectorConstants.CHILD_INJECTOR_PREFIX_EXPR));

        } else if (StringUtils.isNotBlank(annotation.postfix())) {
            Resource actualParent = InjectorUtils.getLastParentResource(currentResource, annotation.postfix());
            return getFilteredResource(actualParent, InjectorUtils.getPatternPredicate(annotation.postfix(), InjectorConstants.CHILD_INJECTOR_POSTFIX_EXPR));
        }

        return currentResource.getChild(name);
    }

    /**
     * Retrieves first matched {@code Resource}
     * @param currentResource current {@code Resource}
     * @param predicate       {@code Predicate} function
     * @return first matched {@code Resource}
     */
    private Resource getFilteredResource(Resource currentResource, Predicate<Resource> predicate) {
        if (currentResource == null) {
            return null;
        }

        return StreamSupport.stream(currentResource.getChildren().spliterator(), false)
            .filter(predicate)
            .findFirst()
            .orElse(null);
    }
}
