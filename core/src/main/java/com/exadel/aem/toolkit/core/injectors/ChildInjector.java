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

import com.exadel.aem.toolkit.api.annotations.injectors.Child;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * Injects into a Sling model the Resource or adapted object
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class ChildInjector implements Injector {

    public static final String NAME = "eak-child-resource-injector";
    private static final Logger LOG = LoggerFactory.getLogger(ChildInjector.class);
    private Child annotation;

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
     * Attempts to inject {@code Resource} resource or adapted object
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param varName          Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return {@code Resource} resources or adapted object if successful. Otherwise, null is returned
     */
    @CheckForNull
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String varName,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        try {
            annotation = element.getDeclaredAnnotation(Child.class);
            if (annotation == null) {
                return null;
            }

            Resource currentNode = InjectorUtils.getResource(adaptable);
            if (currentNode == null) {
                return null;
            }

            Resource childResource = getChildResource(currentNode, varName);
            if (childResource == null) {
                return null;
            }

            if (Resource.class.equals(type)) {
                return childResource;
            } else if (type instanceof Class) {
                return childResource.adaptTo((Class<?>) type);
            }
        } catch (Exception ex) {
            LOG.error("Failed to inject Child ", ex);
        }

        return null;
    }

    /**
     * Retrieves the filtered child {@code Resource} resource object according to the {@code Child} annotation parameters
     * @param currentNode {@code Resource} current resource node
     * @param varName {@code String} Name of the Java class member to inject the value into
     * @return {@code Resource} resource object if success. Otherwise, null is returned
     */
    private Resource getChildResource(Resource currentNode, String varName) {
        if (StringUtils.isNotBlank(annotation.name())) {
            return currentNode.getChild(InjectorUtils.prepareRelativePath(annotation.name()));
        } else if (StringUtils.isNotBlank(annotation.namePrefix())) {
            Resource actualParent = InjectorUtils.getLastNodeParentResource(currentNode, annotation.namePrefix());
            return getFilteredResource(actualParent, InjectorUtils.getPatternPredicate(annotation.namePrefix(), InjectorConstants.CHILD_INJECTOR_PREFIX_EXPR));
        } else if (StringUtils.isNotBlank(annotation.namePostfix())) {
            Resource actualParent = InjectorUtils.getLastNodeParentResource(currentNode, annotation.namePostfix());
            return getFilteredResource(actualParent, InjectorUtils.getPatternPredicate(annotation.namePostfix(), InjectorConstants.CHILD_INJECTOR_POSTFIX_EXPR));
        } else {
            return currentNode.getChild(varName);
        }
    }

    /**
     * Retrieves first matched {@code Resource} resource
     * @param currentNode {@code Resource} current resource node
     * @param predicate {@code Predicate} predicate function
     * @return first matched {@code Resource} resource
     */
    private Resource getFilteredResource(Resource currentNode, Predicate<Resource> predicate) {
        if (currentNode == null) {
            return null;
        }

        return StreamSupport.stream(currentNode.getChildren().spliterator(), false)
            .filter(predicate)
            .findFirst()
            .orElse(null);
    }
}
