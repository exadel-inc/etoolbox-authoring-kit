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
package com.exadel.aem.toolkit.api.annotations.widgets.common;

/**
 * Contains possible values defining the size of a Granite component
 */
public enum Size {
    EXTRA_SMALL("XS"),
    SMALL("S"),
    MEDIUM("M"),
    LARGE("L");

    private final String value;

    /**
     * Initializes a new enum value
     * @param value Character representation of the value
     */
    Size(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
