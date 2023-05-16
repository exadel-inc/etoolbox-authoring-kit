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
package com.exadel.aem.toolkit.core.assistant.services;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;

/**
 * Represents a service that provides facilities for the {@code Content Assistant}. Usually, one service maps to a
 * particular corporate or 3rd-party solution
 */
public interface AssistantService {

    /**
     * Retrieves the string to identify the vendor of the service. If not provided, the name of the class will be used
     * instead
     * @return A nullable string value
     */
    default String getVendorName() {
        return null;
    }

    /**
     * Retrieves the logo to visually distinguish this service and its facilities from the others. One can provide
     * either an icon class from the <a
     * href="https://developer.adobe.com/experience-manager/reference-materials/6-5/coral-ui/coralui3/Coral.Icon.html">Adobe
     * Coral icons collection</a>, or an image itself in the {@code Base64} encoding
     * @return A nullable string value
     */
    default String getLogo() {
        return null;
    }

    /**
     * Gets whether the current service is enabled. Usually, this is defined by the service configuration
     * @return True or false
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Retrieves the list of {@link Facility} objects representing the functions that the current service provides
     * @return A list of {@code Facility} entities. A non0null value is expected
     */
    List<Facility> getFacilities();

    /**
     * Retrieves a particular {@link Facility} by its identifier
     * @param id String value; a non-blank string is expected
     * @return A {@link Facility} object; or null if not a match is found
     */
    default Facility getFacility(String id) {
        return getFacilities()
            .stream()
            .filter(facility -> StringUtils.equals(facility.getId(), id))
            .findFirst()
            .orElse(null);
    }
}
