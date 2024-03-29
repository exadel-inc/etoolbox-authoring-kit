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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import com.exadel.aem.toolkit.core.optionprovider.OptionProviderConstants;

/**
 * Represents a particular text-value pair to be extracted from a {@code Resource} and then transformed into a
 * datasource option
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
    private List<String> attributeMembers;
    private List<Pair<String, String>> attributes;

    private StringTransformation textTransform;
    private StringTransformation valueTransform;

    private boolean uniqueByName;

    /**
     * Default (instantiation-restricting) constructor
     */
    private Option() {
    }

    /**
     * Generates a Granite-compliant {@link ValueMapResource} representing a particular option with the specified label
     * and value
     * @return A {@code ValueMapResource} item that stands for the datasource option, or null in case this
     * {@code DataSourceEntry} is invalid
     */
    ValueMapResource toDataSourceEntry() {
        if (!isValid()) {
            return null;
        }
        ResourceResolver effectiveResourceResolver = resourceResolver != null ? resourceResolver : resource.getResourceResolver();
        ValueMap valueMap = new ValueMapDecorator(new HashMap<>());
        valueMap.put(CoreConstants.PN_TEXT, getText());
        valueMap.put(CoreConstants.PN_VALUE, getValue());
        if (selected) {
            valueMap.put(CoreConstants.PN_SELECTED, true);
            valueMap.put(CoreConstants.PN_CHECKED, true);
        }
        return new OptionResource(effectiveResourceResolver, valueMap, getCustomAttributes());
    }

    /**
     * Gets whether this entry has enough values to be transformed into a datasource option
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
     * Gets the name part of the option entry
     * @return String value
     */
    String getName() {
        return getCustomAttribute(OptionProviderConstants.PARAMETER_NAME, StringTransformation.NONE);
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
        if (CollectionUtils.isNotEmpty(attributeMembers)) {
            attributeMembers
                .stream()
                .filter(StringUtils::isNotBlank)
                .forEach(attributeMember -> {
                    String attributeValue = getCustomAttribute(attributeMember, StringTransformation.NONE);
                    if (StringUtils.isNotBlank(attributeValue)) {
                        result.put(attributeMember.replace(CoreConstants.SEPARATOR_COLON, CoreConstants.SEPARATOR_HYPHEN), attributeValue);
                    }
                });
        }
        if (CollectionUtils.isNotEmpty(attributes)) {
            attributes
                .stream()
                .filter(pair -> StringUtils.isNotBlank(pair.getKey()))
                .forEach(pair -> result.put(pair.getKey(), pair.getValue()));
        }
        return result;
    }

    /**
     * Used to retrieve this option's text or value, or a custom attribute of an underlying JCR resource
     * @param attributeMember    Reference to either {@code textMember}, {@code valueMember}, or {@code attributeMember}
     *                           value
     * @param attributeTransform Reference to either {@code textTransform} or {@code valueTransform} value
     * @return String value, or an empty string
     */
    private String getCustomAttribute(String attributeMember, StringTransformation attributeTransform) {
        if (!isValid(resource)) {
            return StringUtils.EMPTY;
        } else if (OptionProviderConstants.PARAMETER_ID.equals(attributeMember)) {
            return getResourceId();
        } else if (OptionProviderConstants.PARAMETER_NAME.equals(attributeMember)) {
            return attributeTransform != null ? attributeTransform.apply(resource.getName()) : resource.getName();
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
     * Returns resource ID as requested by the user setting. For an ordinary resource node (such as an ACS List-like
     * option), this resolves to merely node name, but for a Tag, the namespace-qualified tag identifier is returned
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
     * Implements builder pattern for the {@link Option}. Ensures that the {@code DataSourceEntry} fields are
     * initialized with proper defaults
     */
    @SuppressWarnings("MissingJavadocMethod")
    static class Builder {
        private final Option option = new Option();

        private Builder() {
        }

        Builder resource(Resource value) {
            option.resource = value;
            return this;
        }

        Builder resourceResolver(ResourceResolver value) {
            option.resourceResolver = value;
            return this;
        }

        Builder text(String value) {
            option.text = value;
            return this;
        }

        Builder textMember(String value) {
            option.textMember = value;
            return this;
        }

        Builder value(String value) {
            option.value = value;
            return this;
        }

        Builder valueMember(String value) {
            option.valueMember = value;
            return this;
        }

        Builder attributeMembers(List<String> value) {
            option.attributeMembers = value;
            return this;
        }

        Builder attributes(List<Pair<String, String>> value) {
            option.attributes = value;
            return this;
        }

        Builder textTransform(StringTransformation value) {
            option.textTransform = value;
            return this;
        }

        Builder valueTransform(StringTransformation value) {
            option.valueTransform = value;
            return this;
        }

        Builder uniqueByName(boolean value) {
            option.uniqueByName = value;
            return this;
        }

        Option build() {
            if (StringUtils.isBlank(option.textMember) && StringUtils.isEmpty(option.text)) {
                option.textMember = JcrConstants.JCR_TITLE;
            }
            if (StringUtils.isBlank(option.valueMember) && StringUtils.isEmpty(option.value)) {
                option.valueMember = CoreConstants.PN_VALUE;
            }
            if (option.textTransform == null) {
                option.textTransform = StringTransformation.NONE;
            }
            if (option.valueTransform == null) {
                option.valueTransform = StringTransformation.NONE;
            }
            return option;
        }
    }

    /**
     * Implements {@link Object#equals(Object)} to make sure two options are equal when they have the same values. Note:
     * this behavior is overridden if the {@code uniqueByName} set to {@code true}. In the latter case, two options are
     * only considered equal if they have equal names
     * @param obj Object to compare to
     * @return True or false
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Option
            && (!uniqueByName || (StringUtils.equals(getName(), ((Option) obj).getName())))
            && StringUtils.equals(getValue(), ((Option) obj).getValue());
    }

    /**
     * Implements {@link Object#hashCode()} to accompany the current object's {@code equals()} override
     * @return Hash code as generated for this instance's {@code value}
     */
    @Override
    public int hashCode() {
        return uniqueByName ? Objects.hash(getName(), getValue()) : Objects.hashCode(getValue());
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
