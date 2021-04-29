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
package com.exadel.aem.toolkit.api.annotations.assets.dependson;

/**
 * Presents the set of possible {@code dependsOnRef} values to be used with {@link DependsOnRef} annotation
 */
public enum DependsOnRefTypes {

    /**
     * Type will be chosen automatically based on an element's widget type
     */
    AUTO {
        @Override
        public String toString() {
            return "";
        }
    },
    /**
     * No type casting is performed
     */
    ANY,
    /**
     * Cast to boolean (according to JS cast rules)
     */
    BOOLEAN,
    /**
     * Cast as a string value to boolean (true if string cast equals "true")
     */
    BOOLSTRING,
    /**
     * Parse as JSON value
     */
    JSON,
    /**
     * Cast to number value
     */
    NUMBER,
    /**
     * Cast to string
     */
    STRING
}
