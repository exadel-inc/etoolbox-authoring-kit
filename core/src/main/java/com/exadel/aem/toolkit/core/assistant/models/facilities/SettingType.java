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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the type of setting managed by a {@link Facility}
 * @see Setting
 */
public enum SettingType {
    STRING, INTEGER, DOUBLE;

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
