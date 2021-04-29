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
package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Represents a particular text-value pair to be extracted from a {@code Resource} and then transformed into
 * a datasource option
 */
class Option {
    static final Comparator<Option> COMPARATOR = new OptionComparator();

    private Resource resource;
    private ResourceResolver resourceResolver;

    private String text;
    private String value;

    private boolean selected;

    private String textMember;
    private String valueMember;
    private String[] attributeMembers;
    private String[] attributes;

    private StringTransformation textTransform;
    private StringTransformation valueTransform;

    /**
     * Default (instantiation-restricting) constructor
     */
    private Option() {
    }

    /**
     * Generates a Granite-compliant {@link ValueMapResource} representing a particular option with specified label and value
     * @return {@code ValueMapResource} item that stands for the datasource option, or null in case this
     * {@code DataSourceEntry} is invalid
     */
    ValueMapResource toValueMapEntry() {
        if (!isValid()) {
            return null;
        }
        ResourceResolver effectiveResourceResolver = resourceResolver != null ? resourceResolver : resource.getResourceResolver();
        ValueMap valueMap = new ValueMapDecorator(new HashMap<>());
        valueMap.put(CoreConstants.PN_TEXT, getText());
        valueMap.put(CoreConstants.PN_VALUE, getValue());
        if (selected) {
            valueMap.put(CoreConstants.PN_SELECTED, true);
        }
        return new OptionResource(effectiveResourceResolver, valueMap, getCustomAttributes());
    }

    /**
     * Gets whether this entry has enough values to be transformed to a datasource option
     * @return true or false
     */
    boolean isValid() {
        return StringUtils.isNotBlank(getText())
            && (isValid(resource) || resourceResolver != null);
    }

    /**
     * Gets whether this resource is an actual JCR resource
     * @param resource {@code Resource} object to test
     * @return True or false
     */
    private static boolean isValid(Resource resource) {
        return resource != null && !(resource instanceof NonExistingResource);
    }

    /**
     * Gets the text part of the option entry
     * @return String value
     */
    String getText() {
        if (StringUtils.isNotEmpty(text)) {
            return text;
        }
        text = getCustomAttribute(textMember, textTransform);
        return text;
    }

    /**
     * Gets the value part of the option entry
     * @return String value
     */
    String getValue() {
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }
        value = getCustomAttribute(valueMember, valueTransform);
        return value;
    }

    /**
     * Turns on {@code selected} flag for this option
     */
    void select() {
        selected = true;
    }

    /**
     * Gets custom attributes of the option entry
     * @return {@code Map<String, String>} object, or an empty map
     */
    private Map<String, Object> getCustomAttributes() {
        Map<String, Object> result = new HashMap<>();
        if (!isValid(resource)) {
            return result;
        }
        if (ArrayUtils.isNotEmpty(attributeMembers)) {
            Arrays.stream(attributeMembers)
                .filter(StringUtils::isNotBlank)
                .forEach(attributeMember -> {
                    String attributeValue = getCustomAttribute(attributeMember, StringTransformation.NONE);
                    if (StringUtils.isNotBlank(attributeValue)) {
                        result.put(attributeMember.replace(CoreConstants.SEPARATOR_COLON, CoreConstants.SEPARATOR_HYPHEN), attributeValue);
                    }
                });
        }
        if (ArrayUtils.isNotEmpty(attributes)) {
            Arrays.stream(attributes)
                .map(attr -> attr.split(OptionSourceParameters.KEV_VALUE_SEPARATOR_PATTERN, 2))
                .filter(parts -> ArrayUtils.getLength(parts) == 2 && StringUtils.isNotBlank(parts[0]))
                .forEach(parts -> result.put(
                    parts[0].replaceAll(OptionSourceParameters.INLINE_COLON_PATTERN, CoreConstants.SEPARATOR_HYPHEN).trim(),
                    parts[1].trim()));
        }
        return result;
    }

    /**
     * Used to retrieve this option's text or value, or a custom attribute of an underlying JCR resource
     * @param attributeMember    Reference to either {@code textMember}, {@code valueMember}, or {@code attributeMember} value
     * @param attributeTransform Reference to either {@code textTransform} or {@code valueTransform} value
     * @return String value, or an empty string
     */
    private String getCustomAttribute(String attributeMember, StringTransformation attributeTransform) {
        if (!isValid(resource)) {
            return StringUtils.EMPTY;
        } else if (CoreConstants.PARAMETER_ID.equals(attributeMember)) {
            return getResourceId();
        } else if (CoreConstants.PARAMETER_NAME.equals(attributeMember)) {
            return resource.getName();
        }
        // Tf [textMember]-valued or [valueMember]-valued attribute not found within this Resource, there's still
        // a chance that it may be found under jcr:content subnode (relevant for the case when current option is an
        // "ordinary" page or a similar resource)
        Resource effectiveResource = resource;
        if (!effectiveResource.getValueMap().containsKey(attributeMember) && effectiveResource.getChild(JcrConstants.JCR_CONTENT) != null) {
            effectiveResource = effectiveResource.getChild(JcrConstants.JCR_CONTENT);
        }
        String result = Objects.requireNonNull(effectiveResource).getValueMap().get(attributeMember, StringUtils.EMPTY);
        if (StringUtils.isBlank(result) || attributeTransform == null) {
            return result;
        }
        return attributeTransform.apply(result);
    }

    /**
     * Returns resource ID as requested by the user setting. For an ordinary resource node (such as an ACS List -like
     * option) this resolves to merely node name, but for a Tag, the namespace-qualified tag identifier is returned
     * @return String value
     */
    private String getResourceId() {
        try {
            Node resourceNode = Objects.requireNonNull(resource.adaptTo(Node.class));
            if (resourceNode.isNodeType(TagConstants.NT_TAG)) {
                Tag tag = Objects.requireNonNull(resource.adaptTo(Tag.class));
                return tag.getTagID();
            }
        } catch (RepositoryException | NullPointerException e) {
            return resource.getName();
        }
        return resource.getName();
    }


    /**
     * Gets a builder for a new {@link Option} instance
     * @return {@code DataSourceEntryBuilder} object
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Implements builder pattern for the {@link Option}. Ensures that the {@code DataSourceEntry} fields
     * are initialized with proper defaults
     */
    static class Builder {
        private final Option dataSourceOption = new Option();

        private Builder() {
        }

        Builder resource(Resource value) {
            dataSourceOption.resource = value;
            return this;
        }

        Builder resourceResolver(ResourceResolver value) {
            dataSourceOption.resourceResolver = value;
            return this;
        }

        Builder text(String value) {
            dataSourceOption.text = value;
            return this;
        }

        Builder textMember(String value) {
            dataSourceOption.textMember = value;
            return this;
        }

        Builder value(String value) {
            dataSourceOption.value = value;
            return this;
        }

        Builder valueMember(String value) {
            dataSourceOption.valueMember = value;
            return this;
        }

        Builder attributeMembers(String[] value) {
            dataSourceOption.attributeMembers = value;
            return this;
        }

        Builder attributes(String[] value) {
            dataSourceOption.attributes = value;
            return this;
        }

        Builder textTransform(StringTransformation value) {
            dataSourceOption.textTransform = value;
            return this;
        }

        Builder valueTransform(StringTransformation value) {
            dataSourceOption.valueTransform = value;
            return this;
        }

        Option build() {
            if (StringUtils.isBlank(dataSourceOption.textMember) && StringUtils.isEmpty(dataSourceOption.text)) {
                dataSourceOption.textMember = JcrConstants.JCR_TITLE;
            }
            if (StringUtils.isBlank(dataSourceOption.valueMember) && StringUtils.isEmpty(dataSourceOption.value)) {
                dataSourceOption.valueMember = CoreConstants.PN_VALUE;
            }
            if (dataSourceOption.textTransform == null) {
                dataSourceOption.textTransform = StringTransformation.NONE;
            }
            if (dataSourceOption.valueTransform == null) {
                dataSourceOption.valueTransform = StringTransformation.NONE;
            }
            return dataSourceOption;
        }
    }

    /**
     * Implements {@link Object#equals(Object)} to make sure two options are equal when have same values
     * @param obj Object to compare to
     * @return True or false
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Option && StringUtils.equals(getValue(), ((Option) obj).getValue());
    }

    /**
     * Implements {@link Object#hashCode()} to accompany the current object's {@code equals()} override
     * @return Hash code as generated for this instance's {@code value}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }

    /**
     * Implements {@code Comparator<DataSourceOption>} to make possible sorting {@link Option}s by their labels
     */
    private static class OptionComparator implements Comparator<Option> {
        @Override
        public int compare(Option o1, Option o2) {
            if (o1 == null) {
                return o2 != null ? 1 : 0;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.getText().compareTo(o2.getText());
        }
    }
}
