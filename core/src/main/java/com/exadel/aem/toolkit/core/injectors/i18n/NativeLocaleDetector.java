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
package com.exadel.aem.toolkit.core.injectors.i18n;

import java.util.Locale;
import java.util.function.Function;

/**
 * Used in {@link com.exadel.aem.toolkit.core.injectors.I18nInjector} to provide the fallback locale detection
 * functionality. Technically, it always returns a {@code null} locale so that the injector could internally refer to
 * the value stored in user preferences or else to the global default
 */
public class NativeLocaleDetector implements Function<Object, Locale> {

    /**
     * Retrieves a locale value for the provided adaptable object
     * @param adaptable An adaptable; usually a {@code SlingHttpServletRequest} or a {@code Resource}
     * @return Locale instance
     */
    @Override
    public Locale apply(Object adaptable) {
        return null;
    }
}
