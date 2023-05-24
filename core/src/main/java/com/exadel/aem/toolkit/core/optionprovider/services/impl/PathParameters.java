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

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Contains user-specified parameters for a particular {@code datasource} path
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 * @see OptionSourceParameters
 * @see OptionProviderService
 */
public class PathParameters {
    private String path;

    private String textMember;
    private String valueMember;
    private List<Pair<String, String>> attributes;
    private List<String> attributeMembers;

    private StringTransformation textTransform;
    private StringTransformation valueTransform;

    private boolean fallback;

    /**
     * Default (instantiation-restricting) constructor
     */
    private PathParameters() {
    }

    /**
     * Gets the user-specified {@code path} or {@code fallbackPath} setting depending on the {@code isFallback} state
     * @return String value
     */
    public String getPath() {
        return path;
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
     * @return Collection of name-value pairs
     */
    public List<Pair<String, String>> getAttributes() {
        return attributes;
    }

    /**
     * Gets the user-specified {@code attributeMembers} setting parsed into a string array
     * @return Collection of strings
     */
    public List<String> getAttributeMembers() {
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

    /**
     * Gets whether the current instance represents fallback content
     * @return True or false
     */
    public boolean isFallback() {
        return fallback;
    }

    /**
     * Gets a builder for a new {@link PathParameters} instance
     * @return {@code DataSourcePathParameter} object
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Implements the builder pattern for the {@link PathParameters} entity. The arguments assigned to the builder are
     * checked for nullity. Therefore, it is possible to call the method several times for different sources (like:
     * "assign a default value, then try to overwrite it with the preferred one if it is not missing")
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private final PathParameters pathParameters;

        /**
         * Default constructor
         */
        private Builder() {
            this.pathParameters = new PathParameters();
        }

        /**
         * Assigns the {@code path} value to the current builder. Can be used several times for setting a default value
         * and then a preferred one
         * @param value A nullable {@code path} value
         * @return This builder
         */
        public Builder path(String value) {
            if (StringUtils.isNotEmpty(value)) {
                pathParameters.path = value;
            }
            return this;
        }

        /**
         * Assigns the {@code textMember} value to the current builder. Can be used several times for setting a default
         * value and then a preferred one
         * @param value A nullable {@code textMember} value
         * @return This builder
         */
        public Builder textMember(String value) {
            if (StringUtils.isNotEmpty(value)) {
                pathParameters.textMember = value;
            }
            return this;
        }

        /**
         * Assigns the {@code valueMember} value to the current builder. Can be used several times for setting a default
         * value and then a preferred one
         * @param value A nullable {@code valueMember} value
         * @return This builder
         */
        public Builder valueMember(String value) {
            if (StringUtils.isNotEmpty(value)) {
                pathParameters.valueMember = value;
            }
            return this;
        }

        /**
         * Assigns the collection of attribute members to the current builder. Can be used several times for setting a
         * default value and then a preferred one
         * @param value A nullable list
         * @return This builder
         */
        public Builder attributeMembers(List<String> value) {
            if (CollectionUtils.isNotEmpty(value)) {
                pathParameters.attributeMembers = value;
            }
            return this;
        }

        /**
         * Assigns the collection of name-value pairs representing attribute values. Can be used several times for
         * setting a default value and then a preferred one
         * @param value A nullable list
         * @return This builder
         */
        public Builder attributes(List<Pair<String, String>> value) {
            if (CollectionUtils.isNotEmpty(value)) {
                pathParameters.attributes = value;
            }
            return this;
        }

        /**
         * Assigns the {@code textTransform} value to the current builder
         * @param value {@link StringTransformation} constant
         * @return This builder
         */
        public Builder textTransform(StringTransformation value) {
            pathParameters.textTransform = value;
            return this;
        }

        /**
         * Assigns the {@code valueTransform} value to the current builder
         * @param value {@link StringTransformation} constant
         * @return This builder
         */
        public Builder valueTransform(StringTransformation value) {
            pathParameters.valueTransform = value;
            return this;
        }

        /**
         * Assigns the {@code isFallback} value to the current builder
         * @param value A boolean flag
         * @return This builder
         */
        public Builder isFallback(boolean value) {
            pathParameters.fallback = value;
            return this;
        }

        /**
         * Completes the builder
         * @return {@link PathParameters} object filled with data
         */
        public PathParameters build() {
            return pathParameters;
        }
    }
}
