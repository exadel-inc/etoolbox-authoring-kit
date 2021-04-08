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

import java.util.Arrays;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;

/**
 *  {@link Validator} implementation for testing that provided character range is valid
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
     * Gets a {@code Characters} annotation instance with redundant data filtered out based on the result
     * of {@link AllNotBlankValidator#test(Object)} and {@link CharactersObjectValidator#test(Object)}
     * @param source Source {@code Characters} annotation
     * @return Filtered {@code Characters} value:
     * either with the two of "name" and "entity" String fields set, while the numeric fields are nilled,
     * or with the two "rangeStart" and "rangeEnd" numeric fields set, while String fields are voided
     */
    public Characters getFilteredInstance(Characters source) {
        if (super.test(source)) {
            return AnnotationUtil.filterInstance(source, Characters.class, Arrays.asList(METHOD_RANGE_START, METHOD_RANGE_END));
        }
        return AnnotationUtil.filterInstance(source, Characters.class, Arrays.asList(METHOD_NAME, METHOD_ENTITY));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWarningMessage() {
        return MSG_VALID_PARAMS_EXPECTED;
    }
}
