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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

public abstract class Solution {
    private static final Logger LOG = LoggerFactory.getLogger(Solution.class);

    private static final String PN_ARGS = "args";
    private static final String ERROR_MESSAGE = "Could not serialize the solution";
    private static final String ERROR_MESSAGE_JSON = "{\"messages\": \"Serialization exception: %s\"}";

    private final Map<String, Object> args;

    Solution(Map<String, Object> args) {
        this.args = args;
    }

    public int getStatusCode() {
        return HttpStatus.SC_OK;
    }

    public abstract String asJson();

    String asJson(String key, List<String> entries) {
        Map<String, Object> values = new HashMap<>();
        values.put(PN_ARGS, args);
        if (CollectionUtils.isNotEmpty(entries)) {
            values.put(key, entries);
        }
        return asJson(values);
    }

    String asJson(Map<String, Object> values) {
        return ObjectConversionUtil.toJson(
            values,
            e -> {
                LOG.error(ERROR_MESSAGE, e);
                return String.format(ERROR_MESSAGE_JSON, e.getMessage());
            });
    }

    String asJson(String key, String nestedJson) {
        ObjectNode tree = (ObjectNode) ObjectConversionUtil.toNodeTree(Collections.singletonMap("args", args));
        if (StringUtils.isBlank(nestedJson)) {
            return tree.toString();
        }
        try {
            JsonNode parsedNestedJson = ObjectConversionUtil.toNodeTree(nestedJson);
            if (StringUtils.isEmpty(key) && parsedNestedJson instanceof ObjectNode) {
                tree.setAll((ObjectNode) parsedNestedJson);
            } else if (StringUtils.isNotEmpty(key)) {
                tree.set(key, parsedNestedJson);
            }
            return tree.toString();
        } catch (IOException e) {
            LOG.error(ERROR_MESSAGE, e);
            return String.format(ERROR_MESSAGE_JSON, e.getMessage());
        }
    }

    public static Solution empty() {
        return new Builder(null).empty();
    }

    public static Solution from(String message) {
        return new Builder(null).withMessage(message);
    }

    public static Builder from(Map<String, Object> args) {
        return new Builder(args);
    }

    public static class Builder {

        private final Map<String, Object> args;

        private Builder(Map<String, Object> args) {
            this.args = args;
        }

        public Solution empty() {
            return new JsonStringSolution(args);
        }

        public Solution withJsonContent(String content) {
            return new JsonStringSolution(args, null, content);
        }

        public Solution withJsonContent(String key, String content) {
            return new JsonStringSolution(args, key, content);
        }

        public Solution withOptions(List<String> options) {
            return new OptionSolution(args, options);
        }

        public Solution withValueMap(Map<String, Object> values) {
            return new JsonValueMapSolution(args, values);
        }

        public Solution withMessage(String message) {
            return withMessage(HttpStatus.SC_OK, message);
        }

        public Solution withMessage(int statusCode, String message) {
            return new NotificationSolution(args, statusCode, message);
        }
    }
}
