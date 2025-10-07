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

/**
 * Provides utility methods to create dialog fields for configuration attributes
 */
class FieldUtil {

    private static final String NN_DATA_CHILD = "./data/";
    private static final String PN_CLASS = "granite:class";
    private static final String PN_FIELD = "field";

    private static final String RESTYPE_SEPARATOR = "etoolbox-authoring-kit/configurator/components/content/separator";

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
     * @param request The {@code SlingHttpServletRequest} instance
     * @param config  The {@link ConfigDefinition} instance that contains configuration attributes to be processed
     */
    public static void processRequest(SlingHttpServletRequest request, ConfigDefinition config) {
        List<Resource> fieldCollection = new ArrayList<>();

        // Form heading
        String heading = config.getName();
        if (config.isFactoryInstance()) {
            heading = "Instance of " + heading;
        }
        fieldCollection.add(newHeading(request.getResourceResolver(), heading));
        if (StringUtils.isNotBlank(config.getDescription())) {
            fieldCollection.add(newText(
                request.getResourceResolver(),
                config.getDescription(),
                "config-description"));
        }

        // Metadata fields
        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            "canCleanUp",
            Boolean.toString(!PermissionUtil.hasOverridingPermissions(request))));

        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            "canReplicate",
            Boolean.toString(PermissionUtil.hasReplicatePermission(request))));

        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            "changeCount",
            String.valueOf(config.getChangeCount())));

        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            "ownPath",
            request.getResource().getPath() + ".html/" + config.getId()));

        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            "modified",
            String.valueOf(config.isModified())));

        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            "published",
            String.valueOf(config.isPublished())));

        // Node resource type setters
        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            CoreConstants.RELATIVE_PATH_PREFIX + JcrConstants.JCR_PRIMARYTYPE,
            JcrConstants.NT_UNSTRUCTURED));

        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            NN_DATA_CHILD + JcrConstants.JCR_PRIMARYTYPE,
            JcrConstants.NT_UNSTRUCTURED));

        fieldCollection.add(newHidden(
            request.getResourceResolver(),
            CoreConstants.RELATIVE_PATH_PREFIX + JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
            "/bin/etoolbox/authoring-kit/config"));

        // Attribute fields
        for (ConfigAttribute attribute : config.getAttributes()) {
            boolean isSkipped = attribute.getDefinition().getID().endsWith(ConfiguratorConstants.SUFFIX_BACKUP)
                || StringUtils.equalsAny(attribute.getDefinition().getID(), ConfiguratorConstants.ATTR_NAME_HINT)
                || attribute.getDefinition().getID().startsWith(ConfiguratorConstants.ATTR_CONFIGURATOR);
            if (isSkipped) {
                continue;
            }
            addFieldsForAttribute(fieldCollection, attribute, request.getResourceResolver());
        }

        DataSource dataSource = new SimpleDataSource(fieldCollection.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
    }

    /**
     * Adds dialog fields for the specified configuration attribute to the specified collection
     * @param collection A list of {@code Resource} instances representing dialog fields
     * @param attribute  The {@link ConfigAttribute} instance to be processed
     * @param resolver   The {@code ResourceResolver} instance used to create {@code Resource} instances
     */
    private static void addFieldsForAttribute(
        List<Resource> collection,
        ConfigAttribute attribute,
        ResourceResolver resolver) {

        if (!collection.isEmpty()) {
            collection.add(newField(
                resolver,
                StringUtils.EMPTY,
                Collections.singletonMap(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, RESTYPE_SEPARATOR)));
        }

        String resourceType = attribute.getResourceType();
        Resource childResource = null;
        Map<String, Object> fieldProperties = new HashMap<>();

        // Generic properties
        fieldProperties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, resourceType);
        fieldProperties.put(
            CoreConstants.PN_NAME,
            NN_DATA_CHILD + attribute.getDefinition().getID());

        // Labels
        if (ResourceTypes.CHECKBOX.equals(resourceType)) {
            fieldProperties.put(CoreConstants.PN_TEXT, attribute.getDefinition().getName());
        } else {
            fieldProperties.put(CoreConstants.PN_FIELD_LABEL, attribute.getDefinition().getName());
        }

        // Value members
        if (ResourceTypes.CHECKBOX.equals(resourceType)) {
            fieldProperties.put(CoreConstants.PN_VALUE, Boolean.TRUE.toString());
            fieldProperties.put("uncheckedValue", Boolean.FALSE.toString());
        } else if (ResourceTypes.SELECT.equals(resourceType)) {
            if (ArrayUtils.isNotEmpty(attribute.getDefinition().getOptionValues())) {
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
                        resolver,
                        PN_FIELD + collection.size() + "/items/" + value.toLowerCase(),
                        JcrConstants.NT_UNSTRUCTURED,
                        new ValueMapDecorator(optionProperties));
                    options.add(option);
                }
                childResource = new ValueMapResource(
                    resolver,
                    PN_FIELD + collection.size() + "/items",
                    JcrConstants.NT_UNSTRUCTURED,
                    null,
                    options);
            }
        }

        // Input field
        Resource inputField = newField(
            resolver,
            PN_FIELD + collection.size(),
            fieldProperties,
            childResource,
            attribute.isMultiValue());
        collection.add(inputField);

        // Type hint
        if (attribute.isMultiValue()) {
            collection.add(newHidden(
                resolver,
                CoreConstants.RELATIVE_PATH_PREFIX + attribute.getDefinition().getID() + "@TypeHint",
                attribute.getJcrType()));
        }

        // Description
        if (StringUtils.isNotBlank(attribute.getDefinition().getDescription())) {
            Resource description = newText(
                resolver,
                attribute.getDefinition().getDescription(),
                "field-description");
            collection.add(description);
        }
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates an alert field
     * @param resolver The {@code ResourceResolver} instance used to create the field
     * @param text     The alert text
     * @param variant  The alert variant, e.g. {@code info}, {@code warning}, {@code error}
     * @return The {@code Resource} instance representing the field
     */
    static Resource newAlert(ResourceResolver resolver, String text, String variant) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.ALERT);
        properties.put(CoreConstants.PN_TEXT, text);
        properties.put("variant", variant);
        properties.put(PN_CLASS, "centered");
        return newField(resolver, "alert", properties);
    }

    /**
     * Creates a heading field
     * @param resolver The {@code ResourceResolver} instance used to create the field
     * @param text     The heading text
     * @return The {@code Resource} instance representing the field
     */
    private static Resource newHeading(ResourceResolver resolver, String text) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.HEADING);
        properties.put(CoreConstants.PN_TEXT, text);
        properties.put("level", 2);
        return newField(resolver, "heading", properties);
    }

    /**
     * Creates a hidden field
     * @param resolver The {@code ResourceResolver} instance used to create the field
     * @param nameOrId The field name (if starts with {@code ./}) or granite:id (otherwise)
     * @param value    The field value
     * @return The {@code Resource} instance representing the field
     */
    private static Resource newHidden(ResourceResolver resolver, String nameOrId, String value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.HIDDEN);
        if (StringUtils.startsWith(nameOrId, CoreConstants.RELATIVE_PATH_PREFIX)) {
            properties.put(CoreConstants.PN_NAME, nameOrId);
        } else {
            properties.put("granite:id", nameOrId);
        }
        properties.put(CoreConstants.PN_VALUE, value);
        return newField(resolver, StringUtils.EMPTY, properties);
    }

    /**
     * Creates a text field
     * @param resolver  The {@code ResourceResolver} instance used to create the field
     * @param text      The text content
     * @param className The CSS class name to be assigned to the field
     * @return The {@code Resource} instance representing the field
     */
    private static Resource newText(ResourceResolver resolver, String text, String className) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.TEXT);
        properties.put(CoreConstants.PN_TEXT, text);
        if (StringUtils.isNotBlank(className)) {
            properties.put(PN_CLASS, className);
        }
        return newField(resolver, "span", properties);
    }

    /**
     * Creates a generic field resource
     * @param resolver   The {@code ResourceResolver} instance used to create the field
     * @param path       The path to the field in a virtual resource tree
     * @param properties The field properties
     * @return The {@code Resource} instance representing the field
     */
    private static Resource newField(
        ResourceResolver resolver,
        String path,
        Map<String, Object> properties) {
        return newField(resolver, path, properties, null, false);
    }

    /**
     * Creates a generic field resource
     * @param resolver     The {@code ResourceResolver} instance used to create the field
     * @param path         The path to the field in a virtual resource tree
     * @param properties   The field properties
     * @param child        An optional child resource, e.g., for select options
     * @param isMultiValue Whether this field should be a {@code Multifield}
     * @return The {@code Resource} instance representing the field
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    private static Resource newField(
        ResourceResolver resolver,
        String path,
        Map<String, Object> properties,
        Resource child,
        boolean isMultiValue) {

        String resourceType = StringUtils.defaultIfEmpty(
            properties.getOrDefault(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, StringUtils.EMPTY).toString(),
            JcrConstants.NT_UNSTRUCTURED);
        ValueMap valueMap = new ValueMapDecorator(escapeTextProperties(properties));
        if (isMultiValue) {
            ValueMap wrapperValueMap = new ValueMapDecorator(new HashMap<>());
            wrapperValueMap.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.MULTIFIELD);
            wrapperValueMap.put(CoreConstants.PN_FIELD_LABEL, valueMap.get(CoreConstants.PN_FIELD_LABEL, StringUtils.EMPTY));
            valueMap.remove(CoreConstants.PN_FIELD_LABEL);
            Resource nestedField = new ValueMapResource(
                resolver,
                path + "/field",
                resourceType,
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
            resourceType,
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
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() instanceof String && StringUtils.isNotEmpty((String) e.getValue())
                    ? e.getValue().toString().replace("${", "\\${")
                    : e.getValue()));
    }
}
