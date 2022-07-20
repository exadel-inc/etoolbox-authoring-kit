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

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;

/**
 * Used in {@link com.exadel.aem.toolkit.core.injectors.I18nInjector} to provide detection of locale based on the
 * current resource page
 */
public class PageLocaleDetector implements Function<Object, Locale> {

    /**
     * Retrieves a locale value for the provided adaptable object
     * @param adaptable An adaptable; usually a {@code SlingHttpServletRequest} or a {@code Resource}
     * @return Locale instance
     */
    @Override
    public Locale apply(Object adaptable) {
        ResourceResolver resourceResolver = AdaptationUtil.getResourceResolver(adaptable);
        Resource resource = AdaptationUtil.getResource(adaptable);
        if (resourceResolver == null || resource == null) {
            return null;
        }
        Page page = Optional.ofNullable(resourceResolver.adaptTo(PageManager.class))
            .map(pageManager -> pageManager.getContainingPage(resource))
            .orElse(null);
        if (page == null) {
            return null;
        }
        return page.getLanguage();
    }
}

