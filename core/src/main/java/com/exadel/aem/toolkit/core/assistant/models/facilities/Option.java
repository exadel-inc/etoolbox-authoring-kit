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

/**
 * Represents an option available for a {@link Setting}
 */
public class Option {

    private final String id;
    private final String title;

    /**
     * Constructs a new option
     * @param id    Identifier of the option
     * @param title Title of the option
     */
    Option(String id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * Retrieves the identifier of the option
     * @return String value; a non-null string is expected
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the title of the option
     * @return String value; a non-null string is expected
     */
    public String getTitle() {
        return title;
    }
}
