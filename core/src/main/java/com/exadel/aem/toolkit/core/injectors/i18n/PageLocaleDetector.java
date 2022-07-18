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
import java.util.Optional;
import java.util.function.Function;

import org.apache.sling.api.SlingHttpServletRequest;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Used in {@link com.exadel.aem.toolkit.core.injectors.I18nInjector} to provide detection of locale based on the
 * current resource page
 */
public class PageLocaleDetector implements Function<SlingHttpServletRequest, Locale> {

    /**
     * Retrieves a locale value for the provided request object
     * @param request An instance ofnSlingHttpServletRequest.class request;
     * @return Locale instance
     */
    @Override
    public Locale apply(SlingHttpServletRequest request) {
        Page page = Optional.ofNullable(request.getResourceResolver().adaptTo(PageManager.class))
            .map(pageManager -> pageManager.getContainingPage(request.getResource()))
            .orElse(null);
        if (page == null) {
            return null;
        }
        return page.getLanguage();
    }
}
