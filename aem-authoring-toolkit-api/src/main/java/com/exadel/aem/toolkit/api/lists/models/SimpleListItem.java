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

package com.exadel.aem.toolkit.api.lists.models;

/**
 * Represents basic AEMBox List item which consists of "jct:title" and "value" fields
 */
public interface SimpleListItem {

    /**
     * Gets the {@code title} part of this item
     * @return String value (non-null)
     */
    public String getTitle();

    /**
     * Gets the {@code value} part of this item
     * @return String value (non-null)
     */
    public String getValue();
}
