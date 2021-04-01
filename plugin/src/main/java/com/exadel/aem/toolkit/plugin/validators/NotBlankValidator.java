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

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;

/**
 *  {@link Validator} implementation for testing that provided string is not blank
 */
public class NotBlankValidator implements Validator {
    private static final String MSG_STRING_EXPECTED = "non-blank string expected";

    /**
     * Tests that the provided string is not blank
     * @param value Raw string value
     * @return True or false
     */
    @Override
    public boolean test(Object value) {
        return isApplicableTo(value) && StringUtils.isNotBlank(value.toString());
    }

    /**
     * {@inheritDoc}. In {@code NotBlankValidator}, defines the allow-all kind of predicate
     */
    @Override
    public boolean isApplicableTo(Object value) {
        return value != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWarningMessage() {
        return MSG_STRING_EXPECTED;
    }
}
