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

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.plugin.annotations.Metadata;

/**
 * {@link Validator} implementation for testing that provided character range is valid
 */
public class CharactersObjectValidator extends AllNotBlankValidator {
    private static final String MSG_VALID_PARAMS_EXPECTED = "a character range (start < end) or entity definition must be set";
    private static final String METHOD_RANGE_START = "rangeStart";
    private static final String METHOD_RANGE_END = "rangeEnd";
    private static final String METHOD_NAME = "name";
    private static final String METHOD_ENTITY = "entity";

    /**
     * Tests that the provided character range is valid
     * @param value Annotation instance
     * @return True or false
     */
    @Override
    public boolean test(Object value) {
        if (super.test(value)) {
            return true;
        }
        if (!isApplicableTo(value)) {
            return false;
        }
        Characters characters = (Characters) value;
        return characters.rangeStart() > 0 && characters.rangeEnd() > characters.rangeStart();
    }

    /**
     * Returns whether this object is of {@code Characters} type
     * @param value Tested value
     * @return True or false
     */
    @Override
    public boolean isApplicableTo(Object value) {
        return value instanceof Characters;
    }

    /**
     * Filters out redundant data from a {@code Characters} annotation instance basing on the result of {@link
     * AllNotBlankValidator#test(Object)} and {@link CharactersObjectValidator#test(Object)}. Either the {@code name}
     * and {@code entity} fields are set, while the numeric fields are nullified, or the {@code rangeStart} and {@code
     * rangeEnd} fields are set, while String fields are voided
     * @param source Source {@code Characters} annotation
     */
    public void filter(Characters source) {
        Metadata metadata = Metadata.from(source);
        if (super.test(source)) {
            metadata.unsetValue(METHOD_RANGE_START);
            metadata.unsetValue(METHOD_RANGE_END);
        } else {
            metadata.unsetValue(METHOD_NAME);
            metadata.unsetValue(METHOD_ENTITY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWarningMessage() {
        return MSG_VALID_PARAMS_EXPECTED;
    }
}
