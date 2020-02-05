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
package com.exadel.aem.toolkit.core.util.validation;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.core.util.ConversionUtility;

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
     * @param obj Annotation instance
     * @return True or false
     */
    @Override
    public boolean test(Object obj) {
        if (super.test(obj)) {
            return true;
        }
        if (!isApplicableTo(obj)) {
            return false;
        }
        Characters characters = (Characters) obj;
        return characters.rangeStart() > 0 && characters.rangeEnd() > characters.rangeStart();
    }

    /**
     * Returns whether this object is of {@code Characters} type
     * @param obj Tested value
     * @return True or false
     */
    @Override
    public boolean isApplicableTo(Object obj) {
        return obj instanceof Characters;
    }

    /**
     * Gets a {@code Characters} annotation instance with redundant data filtered out based on the result
     * of {@link AllNotBlankValidator#test(Object)} and {@link CharactersObjectValidator#test(Object)}
     * @param source Source {@code Characters} annotation
     * @return Filtered {@code Characters} value
     */
    public Characters getFilteredInstance(Characters source) {
        if (super.test(source)) {
            return ConversionUtility.getFilteredAnnotation(source, Characters.class, new String[] {METHOD_NAME, METHOD_ENTITY});
        }
        return ConversionUtility.getFilteredAnnotation(source, Characters.class, new String[] {METHOD_RANGE_START, METHOD_RANGE_END});
    }

    @Override
    public String getWarningMessage() {
        return MSG_VALID_PARAMS_EXPECTED;
    }
}
