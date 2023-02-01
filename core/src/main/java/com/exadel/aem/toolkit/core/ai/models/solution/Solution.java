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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class Solution {
    private static final Logger LOG = LoggerFactory.getLogger(OptionSolution.class);

    public static Solution EMPTY = new RawJsonSolution(null);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Solution() {
    }

    public abstract String asJson();

    String asJson(String entryKey, List<String> entries) {
        try {
            return OBJECT_MAPPER.writeValueAsString(Collections.singletonMap(entryKey, entries));
        } catch (JsonProcessingException e) {
            LOG.error("Could not serialize notification(-s)", e);
            return "{\"messages\": \"Serialization exception: " + e.getMessage() + "\"}";
        }
    }

    public static Solution fromJson(String content) {
        return new RawJsonSolution(content);
    }

    public static Solution fromOptions(List<String> options) {
        return new OptionSolution(options);
    }

    public static Solution fromMessage(String message) {
        return new NotificationSolution(message);
    }
}
