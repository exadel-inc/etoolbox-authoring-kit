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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.injectors.Children;

/**
 * Injector implementation for `@Children`
 * Injects into a Sling model a collection of children, all elements in the collection will be adapted
 * to the collection's parameterized type if success, otherwise null returned.
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class ChildrenInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(ChildrenInjector.class);

    public static final String NAME = "eak-children-resource-injector";

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
     * Attempts to inject collection of resources or adapted objects
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return Collection of {@code Resource} resources or adapted objects if successful. Otherwise, null is returned
     */
    @CheckForNull
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        Children annotation = element.getDeclaredAnnotation(Children.class);
        if (annotation == null) {
            return null;
        }

        Resource currentResource = InjectorUtils.getResource(adaptable);
        if (currentResource == null) {
            return null;
        }

        if (!InjectorUtils.isTypeCollection(type)) {
            return null;
        }

        List<Object> childrenList = getChildren(currentResource, type, annotation);
        if (childrenList != null && !childrenList.isEmpty()) {
            return childrenList;
        }

        LOG.debug("Failed to inject child");
        return null;
    }

    /**
     * Retrieves the filtered and adapted list of children objects according to the {@code Children} annotation parameters
     * @param currentResource current {@code Resource}
     * @param type            the {@code Type} to adapt to
     * @param annotation      annotation objects
     * @return {@code List<Object>} list of filtered and adapted objects. Otherwise, empty list is returned
     */
    private List<Object> getChildren(Resource currentResource, Type type, Children annotation) {
        List<Predicate<Resource>> predicates = getAnnotationPredicates(annotation);

        if (StringUtils.isNotBlank(annotation.name())) {
            Resource actualParent = currentResource.getChild(InjectorUtils.prepareRelativePath(annotation.name()));
            return getFilteredList(actualParent, type, predicates);

        } else if (StringUtils.isNotBlank(annotation.prefix())) {
            Resource actualParent = InjectorUtils.getLastParentResource(currentResource, annotation.prefix());
            predicates.add(InjectorUtils.getPatternPredicate(annotation.prefix(), InjectorConstants.CHILD_INJECTOR_PREFIX_EXPR));
            return getFilteredList(actualParent, type, predicates);

        } else if (StringUtils.isNotBlank(annotation.postfix())) {
            Resource actualParent = InjectorUtils.getLastParentResource(currentResource, annotation.postfix());
            predicates.add(InjectorUtils.getPatternPredicate(annotation.postfix(), InjectorConstants.CHILD_INJECTOR_POSTFIX_EXPR));
            return getFilteredList(actualParent, type, predicates);
        }

        return getFilteredList(currentResource, type, predicates);
    }

    /**
     * Retrieves the list of initialized predicate functions from the annotation filter parameter
     * @return {@code List<Predicate<Resource>>} of initialized predicate functions if success. Otherwise, empty list is returned
     */
    @SuppressWarnings("unchecked")
    private List<Predicate<Resource>> getAnnotationPredicates(Children annotation) {
        return Arrays.stream(annotation.filters())
            .map(InjectorUtils::getObjectInstance)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the filtered and adapted children from the current resource
     * @param currentResource current {@code Resource}
     * @param type            the {@code Type} to adapt to
     * @param predicates      {@code List<Predicate<Resource>>} of predicates
     * @return {@code List<Object>} of filtered and adapted objects. Otherwise, empty list is returned
     */
    private List<Object> getFilteredList(Resource currentResource, Type type, List<Predicate<Resource>> predicates) {
        if (currentResource == null) {
            return Collections.emptyList();
        }

        final Class<?> actualType = InjectorUtils.getActualType((ParameterizedType) type);

        Stream<Resource> stream = StreamSupport.stream(currentResource.getChildren().spliterator(), false);
        if (CollectionUtils.isNotEmpty(predicates)) {
            for (Predicate<Resource> predicate : predicates) {
                stream = stream.filter(predicate);
            }
        }

        return stream
            .map(resource -> getAdaptedObject(resource, actualType))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the adapted to given {@code Type} type class from given {@code Resource}
     * @param resource   {@code Resource} to be adapted
     * @param actualType actual type parameter
     * @return {@code Object} the object representing an adapted class if success. Otherwise, null is returned
     */
    private Object getAdaptedObject(Resource resource, Class<?> actualType) {
        if (Resource.class.equals(actualType)) {
            return resource;
        } else if (actualType != null) {
            return resource.adaptTo(actualType);
        }
        return null;
    }
}
