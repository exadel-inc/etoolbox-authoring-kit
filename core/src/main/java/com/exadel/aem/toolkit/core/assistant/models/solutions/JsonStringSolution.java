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
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

class JsonStringSolution extends Solution {

    private final String key;
    private final String content;

    JsonStringSolution(Map<String, Object> args, String key, String content) {
        super(args);
        this.key = key;
        this.content = content;
    }

    @Override
    public String asText() {
        return StringUtils.EMPTY;
    }

    @Override
    public String asJson() {
        if (StringUtils.isBlank(content)) {
            return super.asJson(Collections.emptyMap());
        }

        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        if (MapUtils.isNotEmpty(getArgs())) {
            ObjectNode args = (ObjectNode) ObjectConversionUtil.toNodeTree(getArgs());
            result.set(PN_ARGS, args);
        }

        if (StringUtils.isNoneBlank(key, content) && !ObjectConversionUtil.isJson(content)) {
            result.set(key, new TextNode(content));
            return result.toString();
        }

        JsonNode parsedNestedJson;
        try {
            parsedNestedJson = ObjectConversionUtil.toNodeTree(content);
        } catch (IOException e) {
            LOG.error(EXCEPTION_COULD_NOT_SERIALIZE, e);
            return ObjectConversionUtil.toJson(PN_MESSAGES, e.getMessage());
        }
        if (StringUtils.isNotEmpty(key)) {
            result.set(key, parsedNestedJson);
        } else if (parsedNestedJson instanceof ObjectNode) {
            result.setAll((ObjectNode) parsedNestedJson);
        }
        return result.toString();
    }
}
