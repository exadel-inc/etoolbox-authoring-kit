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
package com.exadel.aem.toolkit.plugin.validators;

import java.util.regex.Pattern;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;

/**
 *  {@link Validator} implementation for testing that provided string is a valid JCR path
 */
@SuppressWarnings("unused") // called indirectly via reflection
public class JcrPathValidator extends NotBlankValidator {
    private static final Pattern JCR_PATH_PATTERN = Pattern.compile("^(/[\\w:.-]+)+/?$");
    private static final Pattern PROP_INJECT_PATTERN = Pattern.compile("^\\$\\{\\w+(?:\\.\\w+)*}$");
    private static final String MSG_JCR_PATH_EXPECTED = "complete JCR path expected";

    /**
     * Tests that the provided string is a valid JCR path
     * @param value Raw string
     * @return True or false
     */
    @Override
    public boolean test(Object value) {
        if (!super.test(value)) {
            return false;
        }
        return JCR_PATH_PATTERN.matcher(value.toString()).matches()
                || PROP_INJECT_PATTERN.matcher(value.toString()).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWarningMessage() {
        return MSG_JCR_PATH_EXPECTED;
    }
}
