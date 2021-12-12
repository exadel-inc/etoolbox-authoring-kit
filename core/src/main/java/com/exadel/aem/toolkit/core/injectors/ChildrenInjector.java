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

import com.exadel.aem.toolkit.api.annotations.injectors.Children;

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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Injects into a Sling model the Collection of resources or adapted objects
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class ChildrenInjector implements Injector {

    public static final String NAME = "eak-children-resource-injector";
    private static final Logger LOG = LoggerFactory.getLogger(ChildrenInjector.class);
    private Children annotation;

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
     * Attempts to inject Collection of resources or adapted objects
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param varName          Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return Collection of {@code Resource} resources or adapted objects if successful. Otherwise, null is returned
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
            annotation = element.getDeclaredAnnotation(Children.class);
            if (annotation == null) {
                return null;
            }

            Resource currentNode = InjectorUtils.getResource(adaptable);
            if (currentNode == null) {
                return null;
            }

            if (!InjectorUtils.isTypeCollection(type)) {
                return null;
            }

            return getChildren(currentNode, type);
        } catch (Exception ex) {
            LOG.error("Failed to inject Children ", ex);
        }

        return null;
    }

    /**
     * Retrieves the filtered and adapted children according to the {@code Children} annotation parameters
     * @param currentNode {@code Resource} current resource node
     * @param type        {@code Type} the type to adapt to
     * @return {@code List<Object>} list of filtered and adapted objects. Otherwise, empty collection is returned
     */
    private List<Object> getChildren(Resource currentNode, Type type) {
        if (StringUtils.isNotBlank(annotation.name())) {
            Resource actualParent = currentNode.getChild(annotation.name());
            List<Predicate<Resource>> predicates = getAnnotationPredicates();
            return getFilteredList(actualParent, type, predicates);
        } else if (StringUtils.isNotBlank(annotation.namePrefix())) {
            Resource actualParent = InjectorUtils.getLastNodeParentResource(currentNode, annotation.namePrefix());
            List<Predicate<Resource>> predicates = getAnnotationPredicates();
            predicates.add(InjectorUtils.getPatternPredicate(annotation.namePrefix(), InjectorConstants.CHILD_INJECTOR_PREFIX_EXPR));
            return getFilteredList(actualParent, type, predicates);
        } else if (StringUtils.isNotBlank(annotation.namePostfix())) {
            Resource actualParent = InjectorUtils.getLastNodeParentResource(currentNode, annotation.namePostfix());
            List<Predicate<Resource>> predicates = getAnnotationPredicates();
            predicates.add(InjectorUtils.getPatternPredicate(annotation.namePostfix(), InjectorConstants.CHILD_INJECTOR_POSTFIX_EXPR));
            return getFilteredList(actualParent, type, predicates);
        } else {
            List<Predicate<Resource>> predicates = getAnnotationPredicates();
            return getFilteredList(currentNode, type, predicates);
        }
    }

    /**
     * Retrieves the list of initialized predicate functions from the annotation filter parameter
     * @return {@code List<Predicate<Resource>>} list of initialized predicate functions if success. Otherwise, empty list is returned
     */
    @SuppressWarnings("unchecked")
    private List<Predicate<Resource>> getAnnotationPredicates() {
        List<Predicate<Resource>> predicates = new ArrayList<>();
        if (annotation.filters().length > 0) {
            for (Class<? extends Predicate> filter : annotation.filters()) {
                Predicate<Resource> filterInstance = InjectorUtils.getObjectInstance(filter);
                if (filterInstance != null) {
                    predicates.add(filterInstance);
                }
            }
        }
        return predicates;
    }

    /**
     * Retrieves the filtered and adapted children from the current node
     * @param currentNode {@code Resource} current resource node
     * @param type        {@code Type} the type to adapt to
     * @param predicates  {@code ist<Predicate<Resource>>} list of predicates
     * @return {@code List<Object>} list of filtered and adapted objects. Otherwise, empty collection is returned
     */
    private List<Object> getFilteredList(Resource currentNode, Type type, List<Predicate<Resource>> predicates) {
        if (currentNode == null) {
            return Collections.emptyList();
        }

        Stream<Resource> stream = StreamSupport.stream(currentNode.getChildren().spliterator(), false);
        if (predicates != null && !predicates.isEmpty()) {
            for (Predicate<Resource> predicate : predicates) {
                stream = stream.filter(predicate);
            }
        }

        return stream
            .map(resource -> adaptTo(resource, type))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the adapted to given {@code Type} type class from given {@code Resource} resource
     * @param resource {@code Resource} resource to be adapted
     * @param type     {@code Type} the type to adapt to
     * @return {@code Object} the object representing an adapted class if success. Otherwise, null is returned
     */
    private Object adaptTo(Resource resource, Type type) {
        Class<?> actualType = InjectorUtils.getActualType((ParameterizedType) type);
        if (Resource.class.equals(actualType)) {
            return resource;
        } else if (actualType != null) {
            return resource.adaptTo(actualType);
        }
        return null;
    }
}
