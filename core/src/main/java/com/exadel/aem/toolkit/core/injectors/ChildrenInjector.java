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
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
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

import com.exadel.aem.toolkit.api.annotations.injectors.Children;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.InstantiationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;

/**
 * Injects into a Sling model a collection of resources or secondary models that are derived from resources according to
 * the type of the underlying array or the parameter type of the underlying collection
 * @see Children
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class ChildrenInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(ChildrenInjector.class);

    public static final String NAME = "eak-children-resource-injector";

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
     * Attempts to inject the collection of values into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve
     *                         annotation objects
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

        Resource adaptableResource = AdaptationUtil.getResource(adaptable);
        if (adaptableResource == null) {
            return null;
        }

        if (!TypeUtil.isValidCollection(type) && !TypeUtil.isValidArray(type)) {
            return null;
        }

        String resourcePath = StringUtils.defaultIfBlank(annotation.name(), name);
        Resource currentResource = adaptableResource.getChild(resourcePath);
        if (currentResource == null) {
            return null;
        }

        List<Object> children = getFilteredInjectables(adaptable, currentResource, type, annotation);
        if (CollectionUtils.isEmpty(children)) {
            LOG.debug("Failed to inject child resources for the name \"{}\"", resourcePath);
            return null;
        }

        if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
            return toArray(children, (Class<?>) type);
        }
        return children;
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
        final Class<?> actualType = TypeUtil.extractComponentType(type);
        if (actualType == null) {
            return null;
        }
        return childResources.stream()
            .map(resource -> InstantiationUtil.getFilteredResource(resource, settingsHolder.prefix(), settingsHolder.postfix()))
            .filter(resource -> !resource.getValueMap().isEmpty())
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
     * @param settingsHolder {@code Children} annotation object containing the adaptation settings
     * @return {@code List} of initialized predicate functions
     */
    private static Predicate<Resource> getResourceFilter(Children settingsHolder) {
        if (ArrayUtils.isEmpty(settingsHolder.filters())) {
            return resource -> true;
        }
        return Arrays.stream(settingsHolder.filters())
            .filter(cls -> ClassUtils.isAssignable(cls, Predicate.class))
            .map(InstantiationUtil::getObjectInstance)
            .filter(Objects::nonNull)
            .map(filter -> (Predicate<Resource>) filter)
            .reduce(Predicate::and)
            .orElse(DEFAULT_FILTER);
    }

    /**
     * Converts the provided parametrized list of objects into the array of the same parameter type
     * @param values {@code List} instance
     * @param type   {@code Class} reference that specifies the type of list entries
     * @param <T>    Type of entry
     * @return Array of objects
     */
    @SuppressWarnings("unchecked")
    private static <T> T[] toArray(List<T> values, Class<?> type) {
        T[] result = (T[]) Array.newInstance(type.getComponentType(), values.size());
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
