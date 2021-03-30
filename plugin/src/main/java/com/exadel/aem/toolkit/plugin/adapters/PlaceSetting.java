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
import com.exadel.aem.toolkit.api.handlers.Adaptable;
import com.exadel.aem.toolkit.api.handlers.Source;

@Adaptable(Source.class)
public class PlaceSetting {

    private Place wrappedPlace;

    @SuppressWarnings("deprecation") // PlaceOnTab support is retained for compatibility and will be removed
                                     // in a version after 2.0.1
    private PlaceOnTab wrappedPlaceOnTab;

    @SuppressWarnings("deprecation") // PlaceOnTab support is retained for compatibility and will be removed
                                     // in a version after 2.0.1
    public PlaceSetting(Source source) {
        if (source == null) {
            return;
        }
        this.wrappedPlace = source.adaptTo(Place.class);
        this.wrappedPlaceOnTab = source.adaptTo(PlaceOnTab.class);
    }

    public String getValue() {
        if (wrappedPlace != null) {
            return wrappedPlace.value();
        } else if (wrappedPlaceOnTab != null) {
            return wrappedPlaceOnTab.value();
        }
        return StringUtils.EMPTY;
    }
}
