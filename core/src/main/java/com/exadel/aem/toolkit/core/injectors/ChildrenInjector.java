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
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
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

        List<Object> children = getFilteredInjectables(currentResource, type, annotation);
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
     * @param current    Current {@code Resource}
     * @param type       The {@code Type} to adapt to
     * @param annotation {@code Children} annotation object holding the settings
     * @return {@code List<Object>} list of filtered and adapted objects, or an empty list
     */
    private static List<Object> getFilteredInjectables(Resource current, Type type, Children annotation) {
        Predicate<Resource> resourceFilter = getResourceFilter(annotation);
        List<Resource> filteredChildren = StreamSupport.stream(current.getChildren().spliterator(), false)
            .filter(resourceFilter)
            .collect(Collectors.toList());
        return getAdaptedObjects(filteredChildren, annotation.prefix(), annotation.postfix(), type);
    }

    /**
     * Retrieves combined resource predicate that originates from the {@link Children} annotation {@code filter}
     * parameter
     * @param annotation {@code Children} annotation object holding the settings
     * @return {@code List} of initialized predicate functions
     */
    private static Predicate<Resource> getResourceFilter(Children annotation) {
        if (ArrayUtils.isEmpty(annotation.filters())) {
            return resource -> true;
        }

        return Arrays.stream(annotation.filters())
            .filter(cls -> ClassUtils.isAssignable(cls, Predicate.class))
            .map(InstantiationUtil::getObjectInstance)
            .filter(Objects::nonNull)
            .map(filter -> (Predicate<Resource>) filter)
            .reduce(Predicate::and)
            .orElse(resource -> true);
    }

    /**
     * Retrieves the list of objects adapted from the given resources. The adaptation honors the optional properties
     * filter defined by the {@code prefix} and {@code postfix}
     * @param resources Collection of resources
     * @param prefix    An optional leading string chunk to filter resources' properties with
     * @param postfix   An optional trailing string chunk to filter resources' properties with
     * @param type      Type (parameter type) of receiving Java collection or array
     * @return List of adapted objects
     */
    private static List<Object> getAdaptedObjects(List<Resource> resources, String prefix, String postfix, Type type) {
        final Class<?> actualType = TypeUtil.extractComponentType(type);
        return resources.stream()
            .map(resource -> InstantiationUtil.getFilteredResource(resource, prefix, postfix))
            .filter(resource -> !resource.getValueMap().isEmpty())
            .map(resource -> getAdaptedObject(resource, actualType))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the adaptation of the given {@code Resource} to the provided type
     * @param resource {@code Resource} to be adapted
     * @param type     Type of the adaptation
     * @return The object that represents an adapted resource, or null if the adaptation failed
     */
    private static Object getAdaptedObject(Resource resource, Class<?> type) {
        if (Resource.class.equals(type) || Object.class.equals(type)) {
            return resource;
        }
        return resource.adaptTo(type);
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
