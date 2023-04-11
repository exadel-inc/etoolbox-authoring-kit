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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Contains user-specified parameters for a particular {@code datasource} path
 * @see OptionSourceParameters
 * @see OptionProviderService
 */
public class PathParameters {
    private String path;
    private String fallbackPath;

    private String textMember;
    private String valueMember;
    private String[] attributes;
    private String[] attributeMembers;

    private StringTransformation textTransform;
    private StringTransformation valueTransform;

    /**
     * Default (instantiation-restricting) constructor
     */
    private PathParameters() {
    }

    /**
     * Gets the user-specified {@code path} setting
     * @return String value
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the user-specified {@code fallbackPath} setting
     * @return String value
     */
    public String getFallbackPath() {
        return fallbackPath;
    }

    /**
     * Gets the user-specified {@code textMember} setting
     * @return String value
     */
    public String getTextMember() {
        return textMember;
    }

    /**
     * Gets the user-specified {@code valueMember} setting
     * @return String value
     */
    public String getValueMember() {
        return valueMember;
    }

    /**
     * Gets the user-specified {@code attributes} setting parsed into a string array
     * @return Array of strings
     */
    public String[] getAttributes() {
        return attributes;
    }

    /**
     * Gets the user-specified {@code attributeMembers} setting parsed into a string array
     * @return Array of strings
     */
    public String[] getAttributeMembers() {
        return attributeMembers;
    }

    /**
     * Gets the user-specified {@code textTransform} setting
     * @return String value
     */
    public StringTransformation getTextTransform() {
        return textTransform;
    }

    /**
     * Gets the user-specified {@code valueTransform} setting
     * @return String value
     */
    public StringTransformation getValueTransform() {
        return valueTransform;
    }

    public PathParameters modify(String textMember, String valueMember) {
        return builder()
            .path(this.path)
            .fallbackPath(this.fallbackPath, null)
            .textMember(textMember, null)
            .valueMember(valueMember, null)
            .attributes(this.attributes, null)
            .attributeMembers(attributeMembers, null)
            .textTransform(textTransform)
            .valueTransform(valueTransform)
            .build();
    }

    /**
     * Gets a builder for a new {@link PathParameters} instance
     * @return {@code DataSourcePathParameter} object
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Implements the builder pattern for the {@link PathParameters}
     */
    @SuppressWarnings("MissingJavadocMethod")
    public static class Builder {
        private final PathParameters optionSourcePathParameters = new PathParameters();

        private Builder() {
        }

        public Builder path(String value) {
            optionSourcePathParameters.path = value;
            return this;
        }

        public Builder fallbackPath(String value, String defaultValue) {
            optionSourcePathParameters.fallbackPath = StringUtils.defaultIfBlank(value, defaultValue);
            return this;
        }

        public Builder textMember(String value, String defaultValue) {
            optionSourcePathParameters.textMember = StringUtils.defaultIfBlank(value, defaultValue);
            return this;
        }

        public Builder valueMember(String value, String defaultValue) {
            optionSourcePathParameters.valueMember = StringUtils.defaultIfBlank(value, defaultValue);
            return this;
        }

        public Builder attributeMembers(String[] value, String[] defaultValue) {
            optionSourcePathParameters.attributeMembers = ArrayUtils.isNotEmpty(value) ? value : defaultValue;
            return this;
        }

        public Builder attributes(String[] value, String[] defaultValue) {
            optionSourcePathParameters.attributes = ArrayUtils.isNotEmpty(value) ? value : defaultValue;
            return this;
        }

        public Builder textTransform(StringTransformation value) {
            optionSourcePathParameters.textTransform = value;
            return this;
        }

        public Builder valueTransform(StringTransformation value) {
            optionSourcePathParameters.valueTransform = value;
            return this;
        }

        public PathParameters build() {
            return optionSourcePathParameters;
        }
    }
}
