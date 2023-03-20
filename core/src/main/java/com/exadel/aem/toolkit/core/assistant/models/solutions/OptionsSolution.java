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
package com.exadel.aem.toolkit.core.assistant.models.solutions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

class OptionsSolution extends Solution {

    private static final String PN_HAS_MORE = "hasMore";

    private final List<String> options;
    private final boolean continuous;

    OptionsSolution(Map<String, Object> args, List<String> options) {
        this(args, options, true);
    }

    OptionsSolution(Map<String, Object> args, List<String> options, boolean continuous) {
        super(args);
        this.options = options;
        this.continuous = continuous;
    }

    public List<String> getOptions() {
        return options;
    }

    @Override
    public String asText() {
        return options != null ? String.join("\n", options).trim() : StringUtils.EMPTY;
    }

    @Override
    public String asJson() {
        Map<String, Object> details = new HashMap<>();
        details.put(CoreConstants.PN_OPTIONS, options);
        details.put(PN_HAS_MORE, continuous);
        return asJson(details);
    }
}
