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

import com.exadel.aem.toolkit.api.annotations.injectors.I18N;
import com.exadel.aem.toolkit.core.injectors.i18n.NativeLocaleDetector;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Strings {

    private static final String LABEL_HELLO = "Hello world";
    private static final String LOCALE_IT_UNDERSCORE = "it_it";

    @I18N
    private String value;

    private final String constructorValue;

    @I18N(value = LABEL_HELLO, locale = LOCALE_IT_UNDERSCORE)
    private String valueByLocale;

    @I18N(localeDetector = LocaleDetector.class)
    @Named(LABEL_HELLO)
    private String valueByDetector;

    @I18N(value = LABEL_HELLO, localeDetector = NativeLocaleDetector.class)
    private String valueByNativeDetector;

    @Inject
    public Strings(@I18N @Named String value) {
        this.constructorValue = value;
    }


    @Nullable
    public String getValue() {
        return value;
    }

    @Nullable
    public String getConstructorValue() {
        return constructorValue;
    }

    @Nullable
    public String getValueByLocale() {
        return valueByLocale;
    }

    @Nullable
    public String getValueByDetector() {
        return valueByDetector;
    }

    @Nullable
    public String getValueByNativeDetector() {
        return valueByNativeDetector;
    }
}
