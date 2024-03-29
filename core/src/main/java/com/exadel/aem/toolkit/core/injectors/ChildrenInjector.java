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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.exadel.aem.toolkit.api.annotations.injectors.Children;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.CastUtil;
import com.exadel.aem.toolkit.core.injectors.utils.InstantiationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Provides injecting into a Sling model a collection of resources or secondary models that are derived from resources
 * according to the type of the underlying array or else the parameter type of the underlying collection
 * @see Children
 * @see BaseInjector
 */
@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class ChildrenInjector extends BaseInjector<Children> {

    public static final String NAME = "eak-child-resources-injector";

    private static final Predicate<Resource> DEFAULT_FILTER = resource -> true;

    @Reference
    private AdapterManager adapterManager;

    @Reference
    private ModelFactory modelFactory;

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
    Children getManagedAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(Children.class);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Injectable getValue(Object adaptable, String name, Type type, Children annotation) {

        Resource adaptableResource = AdaptationUtil.getResource(adaptable);
        if (adaptableResource == null) {
            return Injectable.EMPTY;
        }

        if (!isSupportedCollectionOrElseSingularType(type) && !Object.class.equals(type)) {
            return Injectable.EMPTY;
        }

        String targetResourcePath = StringUtils.defaultIfBlank(annotation.name(), name);
        Resource currentResource = adaptableResource.getChild(targetResourcePath);
        if (currentResource == null) {
            return Injectable.EMPTY;
        }

        List<Object> children = getFilteredInjectables(adaptable, currentResource, type, annotation);
        if (CollectionUtils.isEmpty(children)) {
            return Injectable.EMPTY;
        }

        return CastUtil.toType(children, type);
    }

    /**
     * Retrieves the filtered and adapted list of child objects according to the {@code Children} annotation parameters
     * @param source          Initial {@link SlingHttpServletRequest} or {@link Resource} instance
     * @param currentResource Current {@code Resource}
     * @param type            The {@code Type} to adapt to
     * @param settingsHolder  {@code Children} annotation object containing the adaptation settings
     * @return {@code List<Object>} list of filtered and adapted objects, or an empty list
     */
    private List<Object> getFilteredInjectables(
        Object source,
        Resource currentResource,
        Type type,
        Children settingsHolder) {

        Predicate<Resource> resourceFilter = getResourceFilter(settingsHolder);
        List<Resource> filteredChildren = StreamSupport.stream(currentResource.getChildren().spliterator(), false)
            .filter(resourceFilter)
            .collect(Collectors.toList());
        return getAdaptedObjects(source, filteredChildren, type, settingsHolder);
    }

    /**
     * Retrieves the list of objects adapted from the initial {@code SlingHttpServletRequest} or {@code Resource} and
     * the retrieved or constructed child (relative) resources. The adaptation honors the optional properties filter
     * defined by the {@code prefix} and {@code postfix}
     * @param source         Initial {@link SlingHttpServletRequest} or {@link Resource} instance
     * @param childResources Collection of child (relative) resources retrieved or constructed for the current
     *                       adaptable
     * @param type           Type (parameter type) of receiving Java collection or array
     * @param settingsHolder {@code Children} annotation object containing the adaptation settings
     * @return List of adapted objects
     */
    private List<Object> getAdaptedObjects(Object source, List<Resource> childResources, Type type, Children settingsHolder) {
        Class<?> actualType = Object.class.equals(type)
            ? Resource.class
            : getElementTypeOrStandaloneType(type);
        if (actualType == null) {
            return Collections.emptyList();
        }
        return childResources.stream()
            .map(resource -> InstantiationUtil.getFilteredResource(resource, settingsHolder.prefix(), settingsHolder.postfix()))
            .map(resource -> getAdaptedObject(source, resource, actualType))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the object adapted from the initial {@code SlingHttpServletRequest} or {@code Resource} and the
     * retrieved or constructed child (relative) resource
     * @param source        Initial {@link SlingHttpServletRequest} or {@link Resource} instance
     * @param childResource {@code Resource} to be adapted
     * @param type          Type of the adaptation
     * @return The object that represents an adapted resource, or null if the adaptation failed
     */
    private Object getAdaptedObject(Object source, Resource childResource, Class<?> type) {
        if (Resource.class.equals(type) || Object.class.equals(type)) {
            return childResource;
        }
        if (source instanceof SlingHttpServletRequest && TypeUtil.isSlingRequestAdapter(modelFactory, type)) {
            return adapterManager.getAdapter(
                AdaptationUtil.getRequest((SlingHttpServletRequest) source, childResource),
                type);
        }
        return childResource.adaptTo(type);
    }

    /**
     * Retrieves combined resource predicate that originates from the {@link Children} annotation {@code filter}
     * parameter
     * @param annotation {@code Children} annotation object containing the adaptation settings
     * @return {@code List} of initialized predicate functions
     */
    private static Predicate<Resource> getResourceFilter(Children annotation) {
        if (ArrayUtils.isEmpty(annotation.filters())) {
            return DEFAULT_FILTER;
        }
        return Arrays.stream(annotation.filters())
            .filter(cls -> ClassUtils.isAssignable(cls, Predicate.class))
            .map(InstantiationUtil::getObjectInstance)
            .filter(Objects::nonNull)
            .map(filter -> (Predicate<Resource>) filter)
            .reduce(Predicate::and)
            .orElse(DEFAULT_FILTER);
    }

    /**
     * Gets whether the given {@code type} matches the following criteria: it is an array, or a parametrized object of
     * one of the types: {@code Collection}, {@code List}, or {@code Set}, or otherwise a non-collection type
     * @param value Type of the adaptation
     * @return True or false
     */
    private static boolean isSupportedCollectionOrElseSingularType(Type value) {
        if (TypeUtil.isSupportedCollectionOrArray(value, true)) {
            return true;
        }
        return value instanceof Class<?> && !((Class<?>) value).isArray();
    }

    /**
     * Attempts to retrieve an element type if the provided value represents a supported collection. Otherwise, returns
     * the given type itself
     * @param value Type of the adaptation
     * @return A nullable {@code Class} object
     */
    private static Class<?> getElementTypeOrStandaloneType(Type value) {
        Class<?> result = TypeUtil.getElementType(value);
        if (result != null) {
            return result;
        }
        return value instanceof Class<?> ? (Class<?>) value : null;
    }
}
