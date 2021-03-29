/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.api.annotations.meta;

/**
 * Represents abstraction of routine used to validate annotations values before rendering them as XML attributes
 */
public interface Validator {
    /**
     * When implemented in a validator class, tests provided {@code Annotation}, primitive, or primitive wrapper against a criteria
     * @param obj Value being tested
     * @return True or false
     */
    boolean test(Object obj);
    /**
     * When implemented in a validator class, used to define if this validation is relevant for a value
     * @param obj Value being tested
     * @return True or false
     */
    boolean isApplicableTo(Object obj);
    /**
     * In a validator class, returns an explanation why a tested value is invalid
     * @return Non-blank string
     */
    String getWarningMessage();
}
