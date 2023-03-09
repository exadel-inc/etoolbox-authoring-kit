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

import java.util.Map;

class JsonStringSolution extends Solution {

    private final String key;
    private final String content;

    JsonStringSolution(Map<String, Object> args) {
        this(args, null, null);
    }

    JsonStringSolution(Map<String, Object> args, String key, String content) {
        super(args);
        this.key = key;
        this.content = content;
    }

    @Override
    public String asJson() {
        return asJson(key, content);
    }
}
