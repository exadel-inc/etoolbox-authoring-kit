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

import java.util.Collections;
import java.util.List;
import java.util.Map;

class NotificationSolution extends Solution {
    private static final String PN_MESSAGES = "messages";

    private final int statusCode;
    private final List<String> messages;

    NotificationSolution(Map<String, Object> args, int statusCode, String message) {
        super(args);
        this.statusCode = statusCode;
        this.messages = Collections.singletonList(message);
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String asJson() {
        return asJson(Collections.singletonMap(PN_MESSAGES, messages));
    }
}
