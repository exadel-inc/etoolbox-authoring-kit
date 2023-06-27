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

import java.lang.annotation.Annotation;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.plugin.annotations.Metadata;

/**
 * {@link Validator} implementation for testing that all String-typed annotation properties are not blank
 */
public class AllNotBlankValidator implements Validator {
    private static final String MSG_STRINGS_NOT_BLANK = "string properties must not be blank";

    /**
     * Tests that all String-typed properties of the provided annotation are not blank
     * @param value Annotation instance
     * @return True or false
     */
    @Override
    public boolean test(Object value) {
        if (!isApplicableTo(value)) {
            return false;
        }
        return Metadata.from((Annotation) value).stream(false, false)
            .filter(prop -> String.class.equals(prop.getType()))
            .allMatch(prop -> prop.getValue() != null && StringUtils.isNotBlank(prop.getValue().toString()));
    }

    /**
     * Returns whether this object is of {@code Annotation} type
     * @param value Tested value
     * @return True or false
     */
    @Override
    public boolean isApplicableTo(Object value) {
        return value instanceof Annotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWarningMessage() {
        return MSG_STRINGS_NOT_BLANK;
    }
}
