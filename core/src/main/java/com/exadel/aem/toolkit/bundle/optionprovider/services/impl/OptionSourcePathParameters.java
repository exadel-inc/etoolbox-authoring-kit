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

package com.exadel.aem.toolkit.bundle.optionprovider.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.bundle.optionprovider.services.OptionProviderService;

/**
 * Contains user-specified parameters for a particular {@code datasource} path
 * @see OptionSourceParameters
 * @see OptionProviderService
 */
class OptionSourcePathParameters {
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
    private OptionSourcePathParameters() {
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
     * Gets a builder for a new {@link OptionSourcePathParameters} instance
     * @return {@code DataSourcePathParameter} object
     */
    static OptionSourcePathParameterBuilder builder() {
        return new OptionSourcePathParameterBuilder();
    }


    /**
     * Implements builder pattern for the {@link OptionSourcePathParameters}
     */
    static class OptionSourcePathParameterBuilder {
        private final OptionSourcePathParameters optionSourcePathParameters = new OptionSourcePathParameters();

        private OptionSourcePathParameterBuilder() {
        }

        OptionSourcePathParameterBuilder path(String value) {
            optionSourcePathParameters.path = value;
            return this;
        }

        OptionSourcePathParameterBuilder fallbackPath(String value, String defaultValue) {
            optionSourcePathParameters.fallbackPath = StringUtils.isNotBlank(value) ? value : defaultValue;
            return this;
        }

        OptionSourcePathParameterBuilder textMember(String value, String defaultValue) {
            optionSourcePathParameters.textMember = StringUtils.isNotBlank(value) ? value : defaultValue;
            return this;
        }

        OptionSourcePathParameterBuilder valueMember(String value, String defaultValue) {
            optionSourcePathParameters.valueMember = StringUtils.isNotBlank(value) ? value : defaultValue;
            return this;
        }

        OptionSourcePathParameterBuilder attributeMembers(String[] value, String[] defaultValue) {
            optionSourcePathParameters.attributeMembers = ArrayUtils.isNotEmpty(value) ? value : defaultValue;
            return this;
        }

        OptionSourcePathParameterBuilder attributes(String[] value, String[] defaultValue) {
            optionSourcePathParameters.attributes = ArrayUtils.isNotEmpty(value) ? value : defaultValue;
            return this;
        }

        OptionSourcePathParameterBuilder textTransform(StringTransform value) {
            optionSourcePathParameters.textTransform = value;
            return this;
        }

        OptionSourcePathParameterBuilder valueTransform(StringTransform value) {
            optionSourcePathParameters.valueTransform = value;
            return this;
        }

        OptionSourcePathParameters build() {
            return optionSourcePathParameters;
        }
    }
}
