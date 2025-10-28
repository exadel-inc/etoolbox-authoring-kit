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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;
import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigAttribute;
import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigDefinition;

/**
 * Provides utility methods to create dialog fields for configuration attributes
 */
class FieldUtil {

    private static final String NN_DATA_CHILD = "./data/";
    private static final String PN_CLASS = "granite:class";

    private static final String RESTYPE_SEPARATOR = "etoolbox-authoring-kit/configurator/components/content/separator";

    private static final String KEY_FIELD_COUNT = FieldUtil.class.getName() + ".count";

    /**
     * Default (instantiation-restricting) constructor
     */
    private FieldUtil() {
    }

    /* ------------------
       Request processing
       ------------------ */

    /**
     * Processes the request to build a data source out of configuration attributes
     * @param request The {@link SlingHttpServletRequest} instance
     * @param config  The {@link ConfigDefinition} instance that contains configuration attributes to be processed
     */
    public static void processRequest(SlingHttpServletRequest request, ConfigDefinition config) {
        List<Resource> fieldCollection = new ArrayList<>();

        // Form heading
        String heading = config.getName();
        if (config.isFactoryInstance()) {
            heading = "Instance of " + heading;
        }
        fieldCollection.add(newHeading(request, heading));
        if (StringUtils.isNotBlank(config.getDescription())) {
            fieldCollection.add(newText(request, config.getDescription(), "config-description"));
        }

        // Node resource type setters
        fieldCollection.add(newHidden(
            request,
            CoreConstants.RELATIVE_PATH_PREFIX + JcrConstants.JCR_PRIMARYTYPE,
            JcrConstants.NT_UNSTRUCTURED));

        fieldCollection.add(newHidden(
            request,
            NN_DATA_CHILD + JcrConstants.JCR_PRIMARYTYPE,
            JcrConstants.NT_UNSTRUCTURED));

        fieldCollection.add(newHidden(
            request,
            CoreConstants.RELATIVE_PATH_PREFIX + JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
            "/bin/etoolbox/authoring-kit/config"));

        // Attribute fields
        for (ConfigAttribute attribute : CollectionUtils.emptyIfNull(config.getAttributes())) {
            boolean isSkipped = attribute.getDefinition().getID().endsWith(ConfiguratorConstants.SUFFIX_BACKUP)
                || StringUtils.equalsAny(attribute.getDefinition().getID(), ConfiguratorConstants.ATTR_NAME_HINT)
                || attribute.getDefinition().getID().startsWith(ConfiguratorConstants.ATTR_CONFIGURATOR);
            if (isSkipped) {
                continue;
            }
            addFieldsForAttribute(request, fieldCollection, attribute);
        }

        DataSource dataSource = new SimpleDataSource(fieldCollection.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
    }

    /**
     * Adds dialog fields for the specified configuration attribute to the specified collection
     * @param request The {@link SlingHttpServletRequest} that serves as the context for field creation
     * @param collection A list of {@code Resource} instances representing dialog fields
     * @param attribute  The {@link ConfigAttribute} instance to be processed
     */
    private static void addFieldsForAttribute(
        SlingHttpServletRequest request,
        List<Resource> collection,
        ConfigAttribute attribute) {

        if (!collection.isEmpty()) {
            collection.add(new Builder(request).resourceType(RESTYPE_SEPARATOR).build());
        }

        Builder builder = new Builder(request);
        String resourceType = attribute.getResourceType();
        Resource childResource = null;

        // Generic properties
        builder
            .resourceType(resourceType)
            .property(CoreConstants.PN_NAME, NN_DATA_CHILD + attribute.getDefinition().getID());

        // Labels
        if (ResourceTypes.CHECKBOX.equals(resourceType)) {
            builder.property(CoreConstants.PN_TEXT, attribute.getDefinition().getName());
        } else {
            builder.property(CoreConstants.PN_FIELD_LABEL, attribute.getDefinition().getName());
        }

        // Value members
        if (ResourceTypes.CHECKBOX.equals(resourceType)) {
            builder.property(CoreConstants.PN_VALUE, Boolean.TRUE.toString());
            builder.property("uncheckedValue", Boolean.FALSE.toString());
        } else if (ResourceTypes.SELECT.equals(resourceType) && ArrayUtils.isNotEmpty(attribute.getDefinition().getOptionValues())) {
            List<Resource> options = new ArrayList<>();
            for (int i = 0; i < attribute.getDefinition().getOptionValues().length; i++) {
                String value = attribute.getDefinition().getOptionValues()[i];
                String text = ArrayUtils.getLength(attribute.getDefinition().getOptionLabels()) > i
                    ? attribute.getDefinition().getOptionLabels()[i]
                    : value;
                Map<String, Object> optionProperties = new HashMap<>();
                optionProperties.put(CoreConstants.PN_TEXT, text);
                optionProperties.put(CoreConstants.PN_VALUE, value);
                Resource option = new ValueMapResource(
                    request.getResourceResolver(),
                    builder.getPath() + "/items/" + value.toLowerCase(),
                    JcrConstants.NT_UNSTRUCTURED,
                    new ValueMapDecorator(optionProperties));
                options.add(option);
            }
            childResource = new ValueMapResource(
                request.getResourceResolver(),
                builder.getPath() + "/items",
                JcrConstants.NT_UNSTRUCTURED,
                null,
                options);
        }

        // Input field
        Resource newInput = builder.child(childResource).multi(attribute.isMultiValue()).build();
        collection.add(newInput);

        // Type and "IgnoreBlanks" hints for multifields
        if (attribute.isMultiValue()) {
            collection.add(newHidden(
                request,
                NN_DATA_CHILD + attribute.getDefinition().getID() + "@TypeHint",
                attribute.getJcrType()));
            collection.add(newHidden(
                request,
                NN_DATA_CHILD + attribute.getDefinition().getID() + "@IgnoreBlanks",
                Boolean.TRUE.toString()));
        }

        // Description
        if (StringUtils.isNotBlank(attribute.getDefinition().getDescription())) {
            Resource description = newText(request, attribute.getDefinition().getDescription(), "field-description");
            collection.add(description);
        }
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates a heading field
     * @param request The {@link SlingHttpServletRequest} that serves as the context for field creation
     * @param text    The heading text
     * @return The {@code Resource} instance representing the field
     */
    private static Resource newHeading(SlingHttpServletRequest request, String text) {
        return new Builder(request)
            .resourceType(ResourceTypes.HEADING)
            .property(CoreConstants.PN_TEXT, text)
            .property("level", 2)
            .build();
    }

    /**
     * Creates a hidden field
     * @param request The {@link SlingHttpServletRequest} that serves as the context for field creation
     * @param nameOrId The field name (if starts with {@code ./}) or the {@code id} attribute (otherwise)
     * @param value    The field value
     * @return The {@code Resource} instance representing the field
     */
    private static Resource newHidden(SlingHttpServletRequest request, String nameOrId, String value) {
        Builder builder = new Builder(request)
            .resourceType(ResourceTypes.HIDDEN)
            .property(CoreConstants.PN_VALUE, value);
        if (StringUtils.startsWith(nameOrId, CoreConstants.RELATIVE_PATH_PREFIX)) {
            builder.property(CoreConstants.PN_NAME, nameOrId);
        } else {
            builder.property("granite:id", nameOrId);
        }
        return builder.build();
    }

    /**
     * Creates a text field
     * @param request The {@link SlingHttpServletRequest} that serves as the context for field creation
     * @param text      The text content
     * @param className The CSS class name to be assigned to the field
     * @return The {@code Resource} instance representing the field
     */
    private static Resource newText(SlingHttpServletRequest request, String text, String className) {
        Builder builder = new Builder(request)
            .resourceType(ResourceTypes.TEXT)
            .property(CoreConstants.PN_TEXT, text);
        if (StringUtils.isNotBlank(className)) {
            builder.property(PN_CLASS, className);
        }
        return builder.build();
    }

    /**
     * Implements the builder pattern to create dialog field resources
     */
    private static class Builder {
        private final Map<String, Object> properties = new HashMap<>();
        private final String path;
        private final ResourceResolver resolver;

        private Resource child;
        private boolean isMultiValue;
        private String resourceType;

        /**
         * Initializes the instance of {@code Builder} with the context of the specified request
         * @param request The {@link SlingHttpServletRequest} that serves as the context for field creation
         */
        Builder(SlingHttpServletRequest request) {
            this.resolver = request.getResourceResolver();
            this.path = request.getResource().getPath() + "/field" + getAndIncrementFieldCount(request);
        }

        /**
         * Retrieves the path associated with the field being built
         * @return String value
         */
        String getPath() {
            return path;
        }

        /**
         * Assigns the child resource to the field being built
         * @param value The {@code Resource} object
         * @return This instance
         */
        Builder child(Resource value) {
            this.child = value;
            return this;
        }

        /**
         * Sets whether the field being built is multi-valued
         * @param value True or false
         * @return This instance
         */
        Builder multi(boolean value) {
            this.isMultiValue = value;
            return this;
        }

        /**
         * Adds a property to the field being built
         * @param name  The property name
         * @param value The property value
         * @return This instance
         */
        Builder property(String name, Object value) {
            this.properties.put(name, value);
            return this;
        }

        /**
         * Sets the resource type of the field being built
         * @param value The resource type
         * @return This instance
         */
        Builder resourceType(String value) {
            this.resourceType = value;
            return this;
        }

        /**
         * Builds the field resource
         * @return The {@code Resource} instance representing the field
         */
        Resource build() {
            String effectiveResourceType = StringUtils.defaultIfEmpty(resourceType, JcrConstants.NT_UNSTRUCTURED);
            ValueMap valueMap = new ValueMapDecorator(escapeTextProperties(properties));
            if (isMultiValue) {
                ValueMap wrapperValueMap = new ValueMapDecorator(new HashMap<>());
                wrapperValueMap.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.MULTIFIELD);
                wrapperValueMap.put(CoreConstants.PN_FIELD_LABEL, valueMap.get(CoreConstants.PN_FIELD_LABEL, StringUtils.EMPTY));
                valueMap.remove(CoreConstants.PN_FIELD_LABEL);
                Resource nestedField = new ValueMapResource(
                    resolver,
                    path + "/field",
                    effectiveResourceType,
                    valueMap,
                    child != null ? Collections.singletonList(child) : null);
                return new ValueMapResource(
                    resolver,
                    path,
                    ResourceTypes.MULTIFIELD,
                    wrapperValueMap,
                    Collections.singletonList(nestedField));
            }
            return new ValueMapResource(
                resolver,
                path,
                effectiveResourceType,
                valueMap,
                child != null ? Collections.singletonList(child) : null);
        }

        /**
         * Escapes text properties to prevent them from being interpreted as Sling expressions
         * @param properties The map of properties to be processed
         * @return The processed map
         */
        private static Map<String, Object> escapeTextProperties(Map<String, Object> properties) {
            return properties
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue() instanceof String
                        ? e.getValue().toString().replace("${", "\\${")
                        : e.getValue()));
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
    }
}
