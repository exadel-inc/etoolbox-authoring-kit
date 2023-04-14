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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

/**
 * Represents a function delivered by an Assistant service
 */
public interface Facility {

    /**
     * Retrieves the ID of the facility
     * @return A non-blank string value
     */
    String getId();

    /**
     * Retrieves the title of the facility. Defaults to the ID value
     * @return A non-blank string value
     */
    default String getTitle() {
        return getId();
    }

    /**
     * Retrieves the image associated with the facility (usually a Base64-encoded string for an inline image, or else a
     * CSS identifier of a standard picture. Defaults to an empty string
     * @return String value
     */
    default String getIcon() {
        return StringUtils.EMPTY;
    }

    /**
     * Retrieves the number that determines the position of the facility in a list
     * @return Int value
     */
    default int getRanking() {
        return 0;
    }

    /**
     * Retrieves whether the facility is allowed to be used in the current request
     * @param request {@link SlingHttpServletRequest} instance
     * @return True or false
     */
    default boolean isAllowed(SlingHttpServletRequest request) {
        return true;
    }

    /**
     * Retrieves the list of secondary facilities that are associated with the current instance. They can, e.g., be
     * variants of the same function supplied by different service vendors
     * @return List of {@link Facility} instances
     */
    List<Facility> getVariants();

    /**
     * Executes the facility and returns a {@link Solution}
     * @param request {@link SlingHttpServletRequest} instance containing parameters (arguments) of execution
     * @return {@code Solution} instance
     */
    Solution execute(SlingHttpServletRequest request);
}
