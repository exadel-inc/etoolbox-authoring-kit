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
package com.exadel.aem.toolkit.core.injectors.models.i18n;

import java.util.Locale;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

public class TestLocaleDetector implements Function<Object, Locale> {

    @Override
    public Locale apply(Object o) {
        if (o instanceof SlingHttpServletRequest) {
            return getLocaleFromResource(((SlingHttpServletRequest) o).getResource());
        } else if (o instanceof Resource) {
            return getLocaleFromResource((Resource) o);
        }
        return null;
    }

    private static Locale getLocaleFromResource(Resource resource) {
        String localeString = resource.getValueMap().get("locale", StringUtils.EMPTY);
        return StringUtils.isNotBlank(localeString) ? new Locale(localeString, localeString) : null;
    }
}
