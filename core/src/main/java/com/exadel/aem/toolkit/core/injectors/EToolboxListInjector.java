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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import com.exadel.aem.toolkit.api.annotations.injectors.EToolboxList;
import com.exadel.aem.toolkit.core.lists.utils.ListHelper;

/**
 * Injects EToolbox Lists into Sling models obtained via {@code ResourceResolver} instance
 * @see ListHelper
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class EToolboxListInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(EToolboxListInjector.class);

    public static final String NAME = "eak-etoolbox-list-injector";

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
     * Attempts to inject list entries into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve
     *                         annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return The value to inject, or null in case injection is not possible
     * @see Injector
     * @see ListHelper
     */
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        EToolboxList annotation = element.getDeclaredAnnotation(EToolboxList.class);
        if (annotation == null) {
            return null;
        }

        ResourceResolver resourceResolver = InjectorUtils.getResourceResolver(adaptable);
        if (resourceResolver == null) {
            return null;
        }

        if (InjectorUtils.isValidRawType(type, Collection.class)) {

            return getTypeArgument(type, 0).equals(Object.class)
                ? getObjectList(resourceResolver, annotation.value())
                : ListHelper.getList(resourceResolver, annotation.value(), getTypeArgument(type, 0));

        } else if (InjectorUtils.isValidRawType(type, Map.class)) {

            if (!annotation.keyProperty().isEmpty()) {
                return ListHelper.getMap(resourceResolver, annotation.value(), annotation.keyProperty(), getTypeArgument(type, 1));

            } else if (getTypeArgument(type, 1).equals(String.class) || getTypeArgument(type, 1).equals(Object.class)) {
                return ListHelper.getMap(resourceResolver, annotation.value());
            }

            return ListHelper.getMap(resourceResolver, annotation.value(), JcrConstants.JCR_TITLE, getTypeArgument(type, 1));

        } else if (!(type instanceof ParameterizedType) && ((Class<?>) type).isArray()) {
            List<?> modelList = ListHelper.getList(resourceResolver, annotation.value(), ((Class<?>) type).getComponentType());
            List<Object> objectList = getObjectList(resourceResolver, annotation.value());

            return ((Class<?>) type).getComponentType().equals(Object.class)
                ? toArray(objectList, ((Class<?>) type).getComponentType())
                : toArray(modelList, ((Class<?>) type).getComponentType());
        }

        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
        return null;
    }

    /**
     * Retrieves a {@code List<Object>} collected from list entries under the provided path
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             The path to a list of items
     * @return List of {@code Object}
     */
    private List<Object> getObjectList(ResourceResolver resourceResolver, String path) {

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
            Objects.requireNonNull(Optional.of(resourceResolver)
                    .map(resolver -> resolver.adaptTo(PageManager.class))
                    .map(pageManager -> pageManager.getPage(path))
                    .map(Page::getContentResource)
                    .map(contentRes -> contentRes.getChild("list"))
                    .orElse(null))
                .listChildren(), Spliterator.ORDERED), false).collect(Collectors.toList());
    }

    /**
     * Retrieves type arguments from parameterized collection type
     * @param type  Type of receiving Java class member
     * @param index The index of the element in the array of type objects
     * @return Class of type object
     */
    private Class<?> getTypeArgument(Type type, int index) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[index];
    }

    /**
     * Converts the List of unknown to an array and casts it to the specified type
     * @param list A generic {@code List} that contains list entries
     * @param type Type of receiving Java class member
     * @param <T>  The class of the objects in the array
     * @return An array containing all the elements from provided List
     */
    @SuppressWarnings("unchecked")
    private <T> T[] toArray(List<?> list, Class<?> type) {
        T[] array = (T[]) Array.newInstance(type, list.size());
        IntStream.range(0, list.size()).forEach(i -> array[i] = (T) list.get(i));
        return array;
    }
}
