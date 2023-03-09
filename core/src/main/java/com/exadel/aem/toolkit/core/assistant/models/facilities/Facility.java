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

public interface Facility {

    String getId();

    default String getTitle() {
        return getId();
    }

    default String getIcon() {
        return StringUtils.EMPTY;
    }

    default int getRanking() { return 0; }

    default boolean isAllowed(SlingHttpServletRequest request) {
        return true;
    }

    List<Facility> getVariants();

    Solution execute(SlingHttpServletRequest request);
}
