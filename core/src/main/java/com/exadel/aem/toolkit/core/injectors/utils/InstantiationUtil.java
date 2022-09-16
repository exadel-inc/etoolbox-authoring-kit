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
package com.exadel.aem.toolkit.core.injectors.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.DeepReadValueMapDecorator;
import org.apache.sling.api.wrappers.ModifiableValueMapDecorator;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for creating instances ob objects
 * <p><u>Note</u>: This class is not a part of the public API</p>
 */
public class InstantiationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(InstantiationUtil.class);

    /**
     * Default (instantiation-restricting) constructor
     */
    private InstantiationUtil() {
    }

    /**
     * Creates a new instance of the specified {@code Class}
     * @param type The class to instantiate
     * @param <T>  Instance type
     * @return New object instance, or null if the creation or initialization failed
     */
    public static <T> T getObjectInstance(Class<? extends T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException ex) {
            LOG.error("Could not initialize object {}", type.getName(), ex);
        }
        return null;
    }

    /**
     * Gets an existing resource or else creates a new {@code Resource} that contains properties from the given current
     * resource filtered with a predicate
     * @param current {@code Resource} object contains properties to be filtered
     * @param prefix  {@code String} representing an optional prefix the properties are checked against when filtering
     * @param postfix {@code String} representing an optional postfix the properties are checked against when filtering
     * @return {@code Resource} instance, or null if retrieval failed
     */
    public static Resource getFilteredResource(Resource current, String prefix, String postfix) {
        if (StringUtils.isEmpty(prefix) && StringUtils.isEmpty(postfix)) {
            return current;
        }
        Map<String, Object> values = current
            .getValueMap()
            .entrySet()
            .stream()
            .filter(entry -> isMatchByPrefixOrPostfix(entry.getKey(), prefix, postfix))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                //entry -> clearPrefixOrPostfix(entry.getKey(), prefix, postfix),
                Map.Entry::getValue));
        List<Resource> children = StreamSupport.stream(current.getChildren().spliterator(), false)
            .filter(child -> isMatchByPrefixOrPostfix(child.getName(), prefix, postfix))
            .map(child -> new ValueMapResource(
                current.getResourceResolver(),
                clearPrefixOrPostfixFromPath(child.getPath(), prefix, postfix),
                child.getResourceType(),
                child.getValueMap()))
            .collect(Collectors.toList());
        return new ValueMapResource(
            current.getResourceResolver(),
            current.getPath(),
            values.getOrDefault(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, current.getResourceType()).toString(),
            new SubNameDeepReadValueMapDecorator(current, new ModifiableValueMapDecorator(values), prefix, postfix),
            children);
    }

    /**
     * Returns whether the given property name is matched by the provided prefix or postfix
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @param postfix  String value representing an optional postfix
     * @return True or false
     */
    private static boolean isMatchByPrefixOrPostfix(String property, String prefix, String postfix) {
        if (StringUtils.isNotEmpty(prefix) && StringUtils.isNotEmpty(postfix)) {
            return StringUtils.startsWith(property, prefix) && StringUtils.endsWith(property, postfix);
        }
        if (StringUtils.isNotEmpty(prefix) && StringUtils.isEmpty(postfix)) {
            return StringUtils.startsWith(property, prefix);
        }
        if (StringUtils.isEmpty(prefix) && StringUtils.isNotEmpty(postfix)) {
            return StringUtils.endsWith(property, postfix);
        }
        return true;
    }

    /**
     * Removes the given prefix and/or postfix from the provided string if they are present
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @param postfix  String value representing an optional postfix
     * @return String value
     */
    private static String clearPrefixOrPostfix(String property, String prefix, String postfix) {
        String result = property;
        if (StringUtils.isNotEmpty(prefix)) {
            result = StringUtils.removeStart(result, prefix);
        }
        if (StringUtils.isNotEmpty(postfix)) {
            result = StringUtils.removeEnd(result, postfix);
        }
        return result;
    }

    /**
     * Removes the given prefix and/or postfix from the provided resource path
     * @param path String value representing the path
     * @param prefix   String value representing an optional prefix
     * @param postfix  String value representing an optional postfix
     * @return String value
     */
    private static String clearPrefixOrPostfixFromPath(String path, String prefix, String postfix) {
        if (!StringUtils.contains(path, CoreConstants.SEPARATOR_SLASH)) {
            return path;
        }
        String parentPath = StringUtils.substringBeforeLast(path, CoreConstants.SEPARATOR_SLASH);
        String name = StringUtils.substringAfterLast(path, CoreConstants.SEPARATOR_SLASH);
        if (StringUtils.isNotEmpty(prefix)) {
            name = StringUtils.removeStart(name, prefix);
        }
        if (StringUtils.isNotEmpty(postfix)) {
            name = StringUtils.removeEnd(name, postfix);
        }
        return parentPath + CoreConstants.SEPARATOR_SLASH + name;
    }
}

class SubNameDeepReadValueMapDecorator extends DeepReadValueMapDecorator {
    private final String prefix;

    private final String postfix;

    private final ValueMap base;

    private final ResourceResolver resolver;

    private final String pathPrefix;

    public SubNameDeepReadValueMapDecorator(Resource resource, ValueMap base, String prefix, String postfix) {
        super(resource, base);
        this.resolver = resource.getResourceResolver();
        this.pathPrefix = resource.getPath() + CoreConstants.SEPARATOR_SLASH;
        this.base = base;
        this.prefix = prefix;
        this.postfix = postfix;
    }

    private ValueMap getValueMap(final String name) {
        final int position = name.lastIndexOf(CoreConstants.SEPARATOR_SLASH);
        if ( position == -1 ) {
            return this.base;
        }
        final Resource resource = this.resolver.getResource(this.pathPrefix + this.getNameWithPrefixAndPostfix(name.substring(0, position)));
        if ( resource != null ) {
            final ValueMap valueMap = resource.adaptTo(ValueMap.class);
            if ( valueMap != null ) {
                return valueMap;
            }
        }
        return ValueMap.EMPTY;
    }

    @Override
    public <T> T get(String name, Class<T> type) {
        return this.getValueMap(name).get(this.getPropertyName(name), type);
    }

    private String getNameWithPrefixAndPostfix(String name) {
        if (StringUtils.isNotEmpty(prefix)) {
            name = prefix + name;
        }
        if (StringUtils.isNotEmpty(postfix)) {
            name = name + postfix;
        }
        return name;
    }

    private String getPropertyName(final String name) {
        final int pos = name.lastIndexOf(CoreConstants.SEPARATOR_SLASH);
        if ( pos == -1 ) {
            return name;
        }
        return name.substring(pos + 1);
    }
}
