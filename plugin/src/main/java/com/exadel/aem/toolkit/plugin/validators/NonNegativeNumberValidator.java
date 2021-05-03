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
 *  {@link Validator} implementation for testing that provided number is non-negative
 */
public class NonNegativeNumberValidator extends NumberValidator {
    private static final String MSG_NON_NEGATIVE_EXPECTED = "number equal or greater than zero expected";

    /**
     * Tests that the provided number is non-negative
     * @param value Generic representation of a number
     * @return True or false
     */
    @Override
    public boolean test(Object value) {
        if (!super.test(value)) {
            return false;
        }
        if (StringUtils.EMPTY.equals(value)) {
            return true;
        }
        return Double.parseDouble(value.toString()) >= 0d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWarningMessage() {
        return MSG_NON_NEGATIVE_EXPECTED;
    }
}
