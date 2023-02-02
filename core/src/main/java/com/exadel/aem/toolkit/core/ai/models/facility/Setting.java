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

import java.util.List;

public class Setting {

    private final String id;
    private final String title;
    private final List<Option> options;

    public Setting(String id, String title) {
        this(id, title, null);
    }

    public Setting(String id, String title, List<Option> options) {
        this.id = id;
        this.title = title;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Option> getOptions() {
        return options;
    }
}
