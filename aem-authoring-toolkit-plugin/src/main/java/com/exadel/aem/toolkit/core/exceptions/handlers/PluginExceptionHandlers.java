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

package com.exadel.aem.toolkit.core.exceptions.handlers;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;

/**
 * The factory-like class for getting an instance of {@link ExceptionHandler} that matches the value specified in
 * the {@code terminateOn} AEM Authoring Toolkit plugin setting
 */
public class PluginExceptionHandlers {
    private static final String ALL_EXCEPTIONS = "all";
    private static final String NONE_EXCEPTIONS = "none";

    private PluginExceptionHandlers() {
    }

    /**
     * Gets the instance of {@link ExceptionHandler} that matches the value specified in
     * {@code terminateOn} AEM Authoring Toolkit plugin setting
     * @param value Setting value
     * @return {@code ExceptionHandler} instance
     */
    public static ExceptionHandler getHandler(String value) {
        if (StringUtils.isBlank(value) || NONE_EXCEPTIONS.equalsIgnoreCase(value)) {
            return new PermissiveExceptionHandler();
        }
        if (ALL_EXCEPTIONS.equalsIgnoreCase(value)) {
            return new StrictExceptionHandler();
        }
        return new SelectiveExceptionHandler(Arrays.stream(StringUtils.split(value, ','))
                .map(String::trim).collect(Collectors.toList()));
    }
}
