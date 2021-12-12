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
package com.exadel.aem.toolkit.plugin.adapters;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.placement.containers.PlacementHelper;

/**
 * Adapts a {@link Source} object to manage the associated placement data derived from source annotations
 */
@Adapts(Source.class)
public class PlaceSetting {

    private Place wrappedPlace;

    @SuppressWarnings("deprecation") // PlaceOnTab support is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    private PlaceOnTab wrappedPlaceOnTab;
    private Target matchingTarget;

    /**
     * Instance constructor per the {@link Adapts} contract
     * @param source {@code Source} object that will be used for extracting resource type
     */
    @SuppressWarnings("deprecation") // PlaceOnTab support is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    public PlaceSetting(Source source) {
        if (source == null) {
            return;
        }
        this.wrappedPlace = source.adaptTo(Place.class);
        this.wrappedPlaceOnTab = source.adaptTo(PlaceOnTab.class);
    }

    /**
     * Retrieves the effective placement reference (the value of {@code @Place} or a similar directive)
     * @return String value; defaults to the empty string
     */
    public String getValue() {
        if (wrappedPlace != null) {
            return wrappedPlace.value();
        } else if (wrappedPlaceOnTab != null) {
            return wrappedPlaceOnTab.value();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Retrieves the {@code Target} object associated with this instance. A target is set if the placed member is
     * supposed to be moved to another container as thw rendering flow proceeds. Then the value is used to move the
     * render outside the former container and into a new one
     * @return {@code Target} instance
     * @see PlacementHelper
     */
    public Target getMatchingTarget() {
        return matchingTarget;
    }

    /**
     * Assigns a {@code Target} to the current instance. The target is usually a node into which the adapted
     * {@code Source} is rendered. It then may be used for moving the render to another container
     * @param value {@code Target} object; non-null value expected
     */
    public void setMatchingTarget(Target value) {
        this.matchingTarget = value;
    }
}
