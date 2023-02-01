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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.ai.models.solution.Solution;

class CombinedFacility implements Facility {

    private final String id;
    private final List<Facility> variants;

    CombinedFacility(Facility other) {
        this.id = StringUtils.substringBefore(other.getId(), CoreConstants.SEPARATOR_DOT);
        this.variants = new ArrayList<>(other.getVariants());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getVendor() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getTitle() {
        return variants
            .stream()
            .map(Facility::getTitle)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(getId());
    }

    @Override
    public String getIcon() {
        return variants
            .stream()
            .map(Facility::getIcon)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(StringUtils.EMPTY);
    }

    @Override
    public List<Facility> getVariants() {
        return variants;
    }

    @Override
    public Solution execute(Map<String, Object> arguments) {
        return Solution.EMPTY;
    }
}