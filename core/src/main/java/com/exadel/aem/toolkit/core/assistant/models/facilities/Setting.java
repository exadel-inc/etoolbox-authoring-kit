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
package com.exadel.aem.toolkit.core.assistant.models.facilities;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a {@link Facility} setting
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Setting {

    private String id;
    private String title;
    private SettingType type;
    private SettingPersistence persistence;
    private List<Option> options;
    private String minValue;
    private String maxValue;
    private String defaultValue;

    /**
     * Default (instantiation-preventing) constructor
     */
    private Setting() {
    }

    /**
     * Retrieves the identifier of the current setting
     * @return String value; a non-null string is expected
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the title is specified for the current setting
     * @return Optional string value
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the type of the setting
     * @return {@link SettingType} value
     */
    public SettingType getType() {
        return type;
    }

    /**
     * Gets the persistence flag specified for the current setting. Apart from {@code default} (persistent) settings,
     * there are: <br>
     * - {@link SettingPersistence#TRANSIENT} settings that users are prompted to specify upon every request; <br>
     * - {@link SettingPersistence#REQUIRED} settings that users should specify if not previously stored in a
     * configuration field
     * @return {@link SettingPersistence} value
     */
    public SettingPersistence getPersistence() {
        return persistence;
    }

    /**
     * Retrieves a list of options if specified for this setting
     * @return A nullable list of {@link Option}s
     */
    public List<Option> getOptions() {
        return options;
    }

    /**
     * Retrieves the minimal possible numeric value if specified for the setting
     * @return Optional string value
     */
    public String getMinValue() {
        return minValue;
    }

    /**
     * Retrieves the maximal possible numeric value if one specified for the setting
     * @return Optional string value
     */
    public String getMaxValue() {
        return maxValue;
    }

    /**
     * Retrieves the default value if one is specified for the setting
     * @return Optional string value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Retrieves an instance of setting {@link Builder}
     * @return {@code Builder} object; non-null
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Represents a builder for the facility {@link Setting}. Allows to specify only required values of the complete
     * scope
     */
    public static class Builder {
        private String id;
        private String title;
        private SettingType type = SettingType.STRING;
        private SettingPersistence persistence = SettingPersistence.DEFAULT;
        private List<Option> options;
        private String minValue;
        private String maxValue;
        private String defaultValue;

        public Builder id(String value) {
            id = value;
            return this;
        }

        public Builder title(String value) {
            title = value;
            return this;
        }

        public Builder type(SettingType value) {
            type = value;
            return this;
        }

        public Builder persistence(SettingPersistence value) {
            persistence = value;
            return this;
        }

        public Builder option(String id) {
            return option(id, StringUtils.capitalize(id));
        }

        public Builder option(String id, String title) {
            Option option = new Option(id, title);
            if (options == null) {
                options = new ArrayList<>();
            }
            options.add(option);
            return this;
        }

        public Builder minValue(Object value) {
            minValue = String.valueOf(value);
            return this;
        }

        public Builder maxValue(Object value) {
            maxValue = String.valueOf(value);
            return this;
        }

        public Builder defaultValue(Object value) {
            defaultValue = String.valueOf(value);
            return this;
        }

        public Setting build() {
            Setting result = new Setting();
            result.id = id;
            result.title = title;
            result.type = type;
            result.persistence = persistence;
            result.options = options;
            result.minValue = minValue;
            result.maxValue = maxValue;
            result.defaultValue = defaultValue;
            return result;
        }
    }
}
