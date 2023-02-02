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
package com.exadel.aem.toolkit.core.ai.models.facility;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class SimpleFacility implements Facility {

    protected SimpleFacility() {
    }

    public abstract String getVendor();

    @JsonIgnore
    @Override
    public List<Facility> getVariants() {
        return Collections.singletonList(this);
    }

    public List<Setting> getSettings() {
        return null;
    }
}
