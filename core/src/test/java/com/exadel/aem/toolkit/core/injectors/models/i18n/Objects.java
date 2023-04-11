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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import com.day.cq.i18n.I18n;

import com.exadel.aem.toolkit.api.annotations.injectors.I18N;
import com.exadel.aem.toolkit.core.injectors.i18n.NativeLocaleDetector;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Objects {

    private static final String LOCALE_IT_DASH = "it-it";

    @I18N
    private I18n i18n;

    private final I18n constructorI18n;

    @I18N(locale = LOCALE_IT_DASH)
    private I18n i18nLocale;

    @I18N(localeDetector = LocaleDetector.class)
    private I18n i18nDetector;

    @I18N(localeDetector = NativeLocaleDetector.class)
    private I18n i18nNative;

    @Inject
    public Objects(
        @I18N @Named I18n i18n) {
        this.constructorI18n = i18n;
    }

    @Nullable
    public I18n getI18n() {
        return i18n;
    }

    @Nullable
    public I18n getConstructorI18n() {
        return constructorI18n;
    }

    @Nullable
    public I18n getI18nLocale() {
        return i18nLocale;
    }

    @Nullable
    public I18n getI18nDetector() {
        return i18nDetector;
    }

    @Nullable
    public I18n getI18nNative() {
        return i18nNative;
    }
}
