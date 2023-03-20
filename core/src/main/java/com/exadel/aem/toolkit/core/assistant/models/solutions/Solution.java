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

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

public abstract class Solution {
    static final Logger LOG = LoggerFactory.getLogger(Solution.class);

    static final String PN_ARGS = "args";
    static final String PN_MESSAGES = "messages";
    private static final String PN_STATUS = "status";

    private static final String VALUE_STATUS_OK = "ok";

    static final String EXCEPTION_COULD_NOT_SERIALIZE = "Could not serialize the solution";

    private final Map<String, Object> args;

    Solution(Map<String, Object> args) {
        this.args = args;
    }

    public int getStatusCode() {
        return HttpStatus.SC_OK;
    }

    Map<String, Object> getArgs() {
        return args;
    }

    /* -------------
       Serialization
       ------------- */

    public abstract String asJson();

    String asJson(Map<String, Object> values) {
        Map<String, Object> effectiveValues = new HashMap<>(values);
        effectiveValues.putIfAbsent(PN_ARGS, args);
        return ObjectConversionUtil.toJson(
            effectiveValues,
            e -> {
                LOG.error(ERROR_MESSAGE, e);
                return String.format(ERROR_MESSAGE_JSON, e.getMessage());
            });
    }

    /* ---------------
       Factory methods
       --------------- */

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
            return new OptionsSolution(args, options);
        }

        public Solution withOptions(List<String> options, boolean continuous) {
            return new OptionsSolution(args, options, continuous);
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
