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
package com.exadel.aem.toolkit.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Utility class to create {@code Resource} objects with a fluent API
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own code</p>
 */
public class ResourceFactory {

    private static final String KEY_FIELD_COUNT = ResourceFactory.class.getName() + ".count";

    /**
     * Default (instantiation-restricting) constructor
     */
    private ResourceFactory() {
    }

    /**
     * Creates a new {@link Resource} instance based on the provided parameters
     * @param resolver The {@code ResourceResolver} to be used for resource creation
     * @return A {@link Builder} instance for fluent resource creation
     */
    public static Builder<?> newResource(ResourceResolver resolver) {
        return new Builder<>(resolver, Builder.class);
    }

    /**
     * Creates a new {@link Resource} instance representing a Granite UI field based on the provided parameters
     * @param request The {@code SlingHttpServletRequest} that serves as the context for field creation
     * @return A {@link FieldBuilder} instance for fluent field creation
     */
    public static FieldBuilder newGraniteField(SlingHttpServletRequest request) {
        return new FieldBuilder(request.getResourceResolver())
            .path(request.getResource().getPath() + "/field" + getAndIncrementFieldCount(request));
    }

    /**
     * Retrieves and increments the field count stored in the request attribute
     * @param request The {@code SlingHttpServletRequest} that serves as the context for field creation
     * @return Int value
     */
    private static int getAndIncrementFieldCount(SlingHttpServletRequest request) {
        int fieldCount = request.getAttribute(KEY_FIELD_COUNT) != null
            ? (int) request.getAttribute(KEY_FIELD_COUNT)
            : 0;
        request.setAttribute(KEY_FIELD_COUNT, fieldCount + 1);
        return fieldCount;
    }

    /**
     * Builder class for creating {@link Resource} instances with a fluent API
     * @param <T> The type of the builder
     */
    public static class Builder<T extends Builder<?>> {

        private final ResourceResolver resolver;
        private final Class<T> type;

        private List<Resource> children;
        private String path;
        private Map<String, Object> properties;
        private String resourceType = JcrConstants.NT_UNSTRUCTURED;

        /**
         * Constructs a new Builder instance
         * @param resolver The {@link ResourceResolver} to be used for resource creation
         * @param type     The class type of the builder
         */
        Builder(ResourceResolver resolver, Class<T> type) {
            this.resolver = resolver;
            this.type = type;
        }

        // Builder methods

        /**
         * Assigns child resources to the resource being built
         * @param value The collection of child resources
         * @return The builder instance
         */
        public T children(Collection<Resource> value) {
            if (CollectionUtils.isEmpty(value)) {
                return type.cast(this);
            }
            value.forEach(this::child);
            return type.cast(this);
        }

        /**
         * Assigns a single child resource to the resource being built
         * @param value The child resource
         * @return The builder instance
         */
        public T child(Resource value) {
            if (value == null) {
                return type.cast(this);
            }
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(value);
            return type.cast(this);
        }

        /**
         * Assigns a path to the resource being built
         * @param value The path segments
         * @return The builder instance
         */
        public T path(String... value) {
            if (ArrayUtils.isEmpty(value)) {
                return type.cast(this);
            }
            String effectivePath = Stream.of(value)
                .map(v -> StringUtils.strip(v, CoreConstants.SEPARATOR_SLASH))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(CoreConstants.SEPARATOR_SLASH));
            if (StringUtils.isNotEmpty(effectivePath)) {
                path = effectivePath;
            }
            return type.cast(this);
        }

        /**
         * Assigns multiple properties to the resource being built
         * @param value The map of properties
         * @return The builder instance
         */
        public T properties(Map<String, Object> value) {
            if (MapUtils.isEmpty(value)) {
                return type.cast(this);
            }
            value.forEach(this::property);
            return type.cast(this);
        }

        /**
        * Assigns a single property to the resource being built
        * @param name  The property name
        * @param value The property value
        * @return The builder instance
        */
        public T property(String name, Object value) {
            if (StringUtils.isEmpty(name) || value == null) {
                return type.cast(this);
            }
            if (properties == null) {
                properties = new HashMap<>();
            }
            properties.put(name, value);
            return type.cast(this);
        }

        /**
         * Assigns a resource type to the resource being built
         * @param value The resource type
         * @return The builder instance
         */
        public T resourceType(String value) {
            if (StringUtils.isNotEmpty(value)) {
                this.resourceType = value;
            }
            return type.cast(this);
        }

        /**
         * Builds the {@link Resource} instance based on the provided parameters
         * @return The built resource instance
         */
        public Resource build() {
            String effectiveMainPath = StringUtils.defaultString(path);
            if (properties == null
                || properties.keySet().stream().noneMatch(k -> StringUtils.contains(k, CoreConstants.SEPARATOR_SLASH))) {
                return new ValueMapResource(
                    resolver,
                    effectiveMainPath,
                    resourceType,
                    new ValueMapDecorator(MapUtils.emptyIfNull(properties)),
                    children);
            }
            Map<String, Map<String, Object>> derivedValueMaps = extractDerivedValueMaps(properties);
            Map<String, Resource> derivedResources = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : derivedValueMaps.entrySet()) {
                String relativePath = entry.getKey();
                boolean isMainResource = relativePath.isEmpty();
                Map<String, Object> derivedValueMap = entry.getValue();
                List<Resource> contextualChildren = derivedResources.entrySet().stream()
                    .filter(e -> !StringUtils.equals(e.getKey(), relativePath))
                    .filter(e -> {
                        if (relativePath.isEmpty()) {
                            return !StringUtils.contains(e.getKey(), CoreConstants.SEPARATOR_SLASH);
                        }
                        return StringUtils.startsWith(e.getKey(), relativePath + CoreConstants.SEPARATOR_SLASH)
                            && !e.getKey().substring(relativePath.length() + 1).contains(CoreConstants.SEPARATOR_SLASH);
                    })
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toCollection(ArrayList::new));
                if (isMainResource && CollectionUtils.isNotEmpty(children)) {
                    contextualChildren.addAll(children);
                }
                Resource resource = new ValueMapResource(
                    resolver,
                    effectiveMainPath
                        + (isMainResource ? StringUtils.EMPTY : CoreConstants.SEPARATOR_SLASH)
                        + relativePath,
                    isMainResource ? resourceType : JcrConstants.NT_UNSTRUCTURED,
                    new ValueMapDecorator(derivedValueMap),
                    contextualChildren);
                derivedResources.put(relativePath, resource);
            }
            return derivedResources.get(StringUtils.EMPTY);
        }

        // Accessors

        /**
         * Retrieves the path assigned to the resource being built
         * @return String value
         */
        public String getPath() {
            return path;
        }

        /**
         * Retrieves the child resources assigned to the resource being built
         * @return List of resources
         */
        List<Resource> getChildren() {
            return children;
        }

        /**
         * Retrieves the properties assigned to the resource being built
         * @return Map of properties
         */
        Map<String, Object> getProperties() {
            return properties;
        }

        /**
         * Retrieves the resolver assigned to the resource being built
         * @return ResourceResolver instance
         */
        ResourceResolver getResolver() {
            return resolver;
        }

        /**
         * Retrieves the resource type assigned to the resource being built
         * @return String value
         */
        String getResourceType() {
            return resourceType;
        }

        /**
         * Extracts value maps representing either the current resource or its children from the provided properties map
         * based on the presence of path separators in the keys
         * @param properties Map of properties
         * @return Map of derived value maps
         */
        private static Map<String, Map<String, Object>> extractDerivedValueMaps(Map<String, Object> properties) {
            Map<String, Map<String, Object>> result = new TreeMap<>(Builder::compareByPathDepth);
            properties.forEach((key, value) -> {
                String parentPath = StringUtils.contains(key, CoreConstants.SEPARATOR_SLASH)
                    ? StringUtils.substringBeforeLast(key, CoreConstants.SEPARATOR_SLASH)
                    : StringUtils.EMPTY;
                String propertyName = parentPath.isEmpty()
                    ? key
                    : StringUtils.substringAfterLast(key, CoreConstants.SEPARATOR_SLASH);
                result.computeIfAbsent(parentPath, k -> new HashMap<>()).put(propertyName, value);
                while (parentPath.contains(CoreConstants.SEPARATOR_SLASH)) {
                    parentPath = StringUtils.substringBeforeLast(parentPath, CoreConstants.SEPARATOR_SLASH);
                    result.computeIfAbsent(parentPath, k -> new HashMap<>());
                }
            });
            return result;
        }

        /**
         * Comparator method to sort resource paths by their depth in descending order. Used to build a virtual resource
         * hierarchy starting from the deepest nodes
         * @param first  First resource path
         * @param second Second resource path
         * @return Comparison result
         */
        private static int compareByPathDepth(String first, String second) {
            int firstDepth = StringUtils.countMatches(first, CoreConstants.SEPARATOR_SLASH);
            int secondDepth = StringUtils.countMatches(second, CoreConstants.SEPARATOR_SLASH);
            if (firstDepth != secondDepth) {
                return Integer.compare(secondDepth, firstDepth);
            }
            if (StringUtils.isEmpty(first) && !StringUtils.isEmpty(second)) {
                return 1;
            } else if (StringUtils.isEmpty(second) && !StringUtils.isEmpty(first)) {
                return -1;
            }
            return StringUtils.compare(first, second);
        }
    }

    /**
     * Extends the generic {@link Builder} class for creating Granite UI field resources with a fluent API
     */
    public static class FieldBuilder extends Builder<FieldBuilder> {
        private boolean isMultiValue;

        /**
         * Constructs a new FieldBuilder instance
         * @param resolver The {@link ResourceResolver} to be used for field resource creation
         */
        FieldBuilder(ResourceResolver resolver) {
            super(resolver, FieldBuilder.class);
        }

        /**
         * Assigns a Granite UI data attribute to the field being built
         * @param key The key of the data attribute
         * @param value The value of the data attribute
         * @return The current FieldBuilder instance
         */
        public FieldBuilder graniteData(String key, Object value) {
            if (StringUtils.isEmpty(key) || value == null) {
                return this;
            }
            return property(CoreConstants.NN_GRANITE_DATA + CoreConstants.SEPARATOR_SLASH + key, value);
        }

        /**
         * Designates the field being built as a multifield
         * @param value True or false
         * @return The current FieldBuilder instance
         */
        public FieldBuilder multi(boolean value) {
            isMultiValue = value;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Resource build() {
            if (!isMultiValue) {
                return super.build();
            }
            Map<String, Object> wrapperValueMap = new HashMap<>();
            wrapperValueMap.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.MULTIFIELD);
            wrapperValueMap.put(CoreConstants.PN_FIELD_LABEL, getProperties().get(CoreConstants.PN_FIELD_LABEL));
            if (ResourceTypes.CONTAINER.equals(getResourceType())) {
                wrapperValueMap.put("composite", true);
            }
            Map<String, Object> nestedValueMap = new HashMap<>(getProperties());
            nestedValueMap.remove(CoreConstants.PN_FIELD_LABEL);
            Resource nestedField = ResourceFactory.newResource(getResolver())
                .path(getPath(), "field")
                .resourceType(getResourceType())
                .properties(nestedValueMap)
                .children(getChildren())
                .build();
            return ResourceFactory.newResource(getResolver())
                .path(getPath())
                .resourceType(ResourceTypes.MULTIFIELD)
                .properties(wrapperValueMap)
                .child(nestedField)
                .build();
        }
    }
}
