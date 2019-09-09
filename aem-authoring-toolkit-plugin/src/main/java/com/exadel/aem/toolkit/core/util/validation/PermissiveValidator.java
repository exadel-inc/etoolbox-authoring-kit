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
package com.exadel.aem.toolkit.core.util.validation;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;

/**
 *  Default (fully permissive) {@link Validator} implementation
 */
public final class PermissiveValidator implements Validator {
    /**
     * Default (fully permissive) test
     */
    @Override
    public boolean test(Object obj) {
        return true;
    }

    /**
     * Default (fully permissive) predicate
     */
    @Override
    public boolean isApplicableTo(Object obj) {
        return true;
    }

    /**
     * Default (permissive) filter
     */
    @Override
    public Object getFilteredValue(Object obj) {
        return obj;
    }

    @Override
    public String getWarningMessage() {
        return null;
    }
}
