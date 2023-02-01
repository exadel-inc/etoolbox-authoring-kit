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
package com.exadel.aem.toolkit.core.ai.models.solution;

import java.util.Collections;
import java.util.List;

class NotificationSolution extends Solution {
    private final List<String> messages;

    NotificationSolution(String message) {
        this.messages = Collections.singletonList(message);
    }

    @Override
    public String asJson() {
        return asJson("messages", messages);
    }
}