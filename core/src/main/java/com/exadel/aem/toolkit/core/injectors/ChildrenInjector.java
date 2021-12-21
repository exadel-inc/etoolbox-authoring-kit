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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
 * Injector implementation for {@code @Children}
 * Injects into a Sling model a collection of children, all elements in the collection will be adapted
 * to the collection's parameterized type if success, otherwise null returned.
 * All parameters in the injected elements will be filtered according to annotation options
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

        Resource adaptableResource = InjectorUtils.getResource(adaptable);
        if (adaptableResource == null) {
            return null;
        }

        if (!InjectorUtils.isTypeCollection(type)) {
            return null;
        }

        String resourcePath = StringUtils.defaultIfBlank(annotation.name(), name);
        Resource currentResource = adaptableResource.getChild(resourcePath);
        if (currentResource == null) {
            return null;
        }

        List<Object> childrenList = getFilteredChildrenList(currentResource, type, annotation);
        if (CollectionUtils.isNotEmpty(childrenList)) {
            return childrenList;
        }

        LOG.debug("Failed to inject child");
        return null;
    }

    /**
     * Retrieves the filtered and adapted list of children objects according to the {@code Children} annotation parameters
     * @param currentResource Current {@code Resource}
     * @param type            The {@code Type} to adapt to
     * @param annotation      Annotation objects
     * @return {@code List<Object>} list of filtered and adapted objects. Otherwise, empty list is returned
     */
    private List<Object> getFilteredChildrenList(Resource currentResource, Type type, Children annotation) {
        List<Predicate<Resource>> filters = getAnnotationFilters(annotation);
        List<Predicate<String>> propertiesPredicates = InjectorUtils.getPropertiesPredicates(annotation.prefix(), annotation.postfix());

        List<Resource> filteredResourceList = getFilteredResourceList(currentResource, filters);
        return getFilteredObjectsList(filteredResourceList, propertiesPredicates, type);
    }

    /**
     * Retrieves the list of initialized predicate functions from the annotation filter parameter
     * @return {@code List<Predicate<Resource>>} of initialized predicate functions
     */
    private List<Predicate<Resource>> getAnnotationFilters(Children annotation) {
        if (annotation.filters().length == 0) {
            List<Predicate<Resource>> filters = new ArrayList<>();
            filters.add(resource -> true);
            return filters;
        }

        return Arrays.stream(annotation.filters())
            .map(InjectorUtils::getObjectInstance)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the filtered children resources of the current resource
     * @param currentResource Current {@code Resource} to be filtered
     * @param predicates      {@code List<Predicate<Resource>>} of predicates
     * @return {@code List<Object>} of filtered children resources.
     */
    private List<Resource> getFilteredResourceList(Resource currentResource, List<Predicate<Resource>> predicates) {
        return StreamSupport.stream(currentResource.getChildren().spliterator(), false)
            .filter(resource -> predicates.stream().anyMatch(f -> f.test(resource)))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of adapted objects whose properties have been filtered according to properties predicated
     * @param resourceList         List of resources
     * @param propertiesPredicates List of properties predicates
     * @param type                 Type of receiving Java class member
     * @return List of adapted objects
     */
    private List<Object> getFilteredObjectsList(List<Resource> resourceList, List<Predicate<String>> propertiesPredicates, Type type) {
        final Class<?> actualType = InjectorUtils.getActualType((ParameterizedType) type);
        return resourceList.stream()
            .map(resource -> InjectorUtils.createFilteredResource(resource, propertiesPredicates))
            .map(item -> getAdaptedObject(item, actualType))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the adapted to given {@code Type} type class from given {@code Resource}
     * @param resource   {@code Resource} to be adapted
     * @param actualType Actual type parameter
     * @return {@code Object} the object representing an adapted class if success. Otherwise, null is returned
     */
    private Object getAdaptedObject(Resource resource, Class<?> actualType) {
        if (Resource.class.equals(actualType)) {
            return resource;
        }
        return resource.adaptTo(actualType);
    }
}
