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
package com.exadel.aem.toolkit.core.injectors.models;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import com.day.cq.i18n.I18n;

import com.exadel.aem.toolkit.api.annotations.injectors.I18N;
import com.exadel.aem.toolkit.core.injectors.i18n.NativeLocaleDetector;
import com.exadel.aem.toolkit.core.injectors.models.i18n.TestLocaleDetector;

@Model(adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@SuppressWarnings("unused")
public class TestModelI18n {

    @I18N
    private I18n i18n;

    private final I18n i18nConstructor;

    @I18N(locale = "it-it")
    private I18n i18nLocale;

    @I18N(localeDetector = TestLocaleDetector.class)
    private I18n i18nDetector;

    @I18N(localeDetector = NativeLocaleDetector.class)
    private I18n i18nNative;

    @I18N
    private String helloWorld;

    @I18N(value = "Hello world", locale = "it_it")
    private String helloWorldLocale;

    @I18N(localeDetector = TestLocaleDetector.class)
    @Named("Hello world")
    private String helloWorldDetector;

    @I18N(value = "Hello world", localeDetector = NativeLocaleDetector.class)
    private String helloWorldNative;

    @Inject
    public TestModelI18n(
        @I18N @Named I18n i18n) {
        this.i18nConstructor = i18n;
    }

    public I18n getI18n() {
        return i18n;
    }

    public I18n getI18nLocale() {
        return i18nLocale;
    }

    public I18n getI18nDetector() {
        return i18nDetector;
    }

    public I18n getI18nNative() {
        return i18nNative;
    }

    public I18n getI18nConstructor() {
        return i18nConstructor;
    }

    public String getHelloWorld() {
        return helloWorld;
    }

    public String getHelloWorldLocale() {
        return helloWorldLocale;
    }

    public String getHelloWorldDetector() {
        return helloWorldDetector;
    }

    public String getHelloWorldNative() {
        return helloWorldNative;
    }
}
