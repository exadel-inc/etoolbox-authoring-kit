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

import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;

/**
 * Contains user-specified parameters for a particular {@code datasource} path
 * @see OptionSourceParameters
 * @see OptionProviderService
 */
class PathParameters {
    private String path;
    private String fallbackPath;

    private String textMember;
    private String valueMember;
    private String[] attributes;
    private String[] attributeMembers;

    private StringTransform textTransform;
    private StringTransform valueTransform;

    /**
     * Default (private) constructor
     */
    private PathParameters() {
    }

    /**
     * Gets the user-specified {@code path} setting
     * @return String value
     */
    String getPath() {
        return path;
    }

    /**
     * Gets the user-specified {@code fallbackPath} setting
     * @return String value
     */
    String getFallbackPath() {
        return fallbackPath;
    }

    /**
     * Gets the user-specified {@code textMember} setting
     * @return String value
     */
    String getTextMember() {
        return textMember;
    }

    /**
     * Gets the user-specified {@code valueMember} setting
     * @return String value
     */
    String getValueMember() {
        return valueMember;
    }

    /**
     * Gets the user-specified {@code attributes} setting
     * @return Array of strings
     */
    String[] getAttributes() {
        return attributes;
    }

    /**
     * Gets the user-specified {@code attributeMembers} setting
     * @return Array of strings
     */
    String[] getAttributeMembers() {
        return attributeMembers;
    }

    /**
     * Gets the user-specified {@code textTransform} setting parsed to a string array
     * @return String value
     */
    StringTransform getTextTransform() {
        return textTransform;
    }

    /**
     * Gets the user-specified {@code valueTransform} setting parsed to a string array
     * @return String value
     */
    StringTransform getValueTransform() {
        return valueTransform;
    }

    /**
     * Gets a builder for a new {@link PathParameters} instance
     * @return {@code DataSourcePathParameter} object
     */
    static Builder builder() {
        return new Builder();
    }


    /**
     * Implements builder pattern for the {@link PathParameters}
     */
    static class Builder {
        private final PathParameters optionSourcePathParameters = new PathParameters();

        private Builder() {
        }

        Builder path(String value) {
            optionSourcePathParameters.path = value;
            return this;
        }

        Builder fallbackPath(String value, String defaultValue) {
            optionSourcePathParameters.fallbackPath = StringUtils.defaultIfBlank(value, defaultValue);
            return this;
        }

        Builder textMember(String value, String defaultValue) {
            optionSourcePathParameters.textMember = StringUtils.defaultIfBlank(value, defaultValue);
            return this;
        }

        Builder valueMember(String value, String defaultValue) {
            optionSourcePathParameters.valueMember = StringUtils.defaultIfBlank(value, defaultValue);
            return this;
        }

        Builder attributeMembers(String[] value, String[] defaultValue) {
            optionSourcePathParameters.attributeMembers = ArrayUtils.isNotEmpty(value) ? value : defaultValue;
            return this;
        }

        Builder attributes(String[] value, String[] defaultValue) {
            optionSourcePathParameters.attributes = ArrayUtils.isNotEmpty(value) ? value : defaultValue;
            return this;
        }

        Builder textTransform(StringTransform value) {
            optionSourcePathParameters.textTransform = value;
            return this;
        }

        Builder valueTransform(StringTransform value) {
            optionSourcePathParameters.valueTransform = value;
            return this;
        }

        PathParameters build() {
            return optionSourcePathParameters;
        }
    }
}
