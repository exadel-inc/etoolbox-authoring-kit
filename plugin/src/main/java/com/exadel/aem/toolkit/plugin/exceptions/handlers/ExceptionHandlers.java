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

package com.exadel.aem.toolkit.plugin.exceptions.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * The factory-like class for getting an instance of {@link ExceptionHandler} that matches the value specified in
 * the {@code terminateOn} AEM Authoring Toolkit plugin setting
 */
public class ExceptionHandlers {
    private static final String EXCEPTION_TOKEN_NONE = "none";

    private ExceptionHandlers() {
    }

    /**
     * Gets the instance of {@link ExceptionHandler} that matches the value specified in
     * the {@code terminateOn} AEM Authoring Toolkit plugin setting
     * @param value Setting value
     * @return {@code ExceptionHandler} instance
     */
    public static ExceptionHandler forSetting(String value) {
        if (StringUtils.isBlank(value) || EXCEPTION_TOKEN_NONE.equalsIgnoreCase(value)) {
            return new PermissiveExceptionHandler();
        }
        List<String> exceptionTokens = Arrays.stream(StringUtils.split(value, CoreConstants.SEPARATOR_COMMA))
                .map(String::trim)
                .collect(Collectors.toList());
        return new SelectiveExceptionHandler(exceptionTokens);
    }
}
