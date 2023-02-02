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

import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Option {

    private final String id;
    private final String title;
    private final boolean isDefault;

    public Option(String id) {
        this(id, StringUtils.capitalize(id));
    }

    public Option(String id, String title) {
        this(id, title, false);
    }

    public Option(String id, boolean isDefault) {
        this(id, StringUtils.capitalize(id), false);
    }

    public Option(String id, String title, boolean isDefault) {
        this.id = id;
        this.title = title;
        this.isDefault = isDefault;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @JsonProperty("default")
    public boolean isDefault() {
        return isDefault;
    }
}
