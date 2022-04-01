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
package com.exadel.aem.toolkit.api.annotations.injectors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.function.Function;

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.core.injectors.I18nInjector;
import com.exadel.aem.toolkit.core.injectors.i18n.NativeLocaleDetector;
import com.exadel.aem.toolkit.core.injectors.i18n.PageLocaleDetector;

/**
 * Used on either a field, a method, or a method parameter of a Sling model to inject an {@code  I18n} object or a
 * particular internationalized string. Internationalization depends on the detection of locale.
 * <p>By default, the locale is detected through the property of the current resource page. To override this, one can
 * either provide a particular locale value, a locale detector (a routine that guesses on the locale  by, e.g., the
 * request path), or fall back to the standard mechanism which guesses on a locale by the preferences of the current
 * user.
 * <p>The preferred adaptable object for this injector in an AEM environment is {@code SlingHttpServletRequest}. If
 * adaptation is done from a {@code Resource}, the result depends on whether a proper Sling resource bundle exists for
 * the current installation
 * <p>The type of the underlying Java member must be {@link com.day.cq.i18n.I18n} or else {@code String} or {@code
 * Object}. Into object-typed members, a string is injected
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(I18nInjector.NAME)
public @interface I18nValue {

    /**
     * Used to specify the string value to internationalize. If not set, defaults to the name of the underlying Java
     * class member. This parameter is ignored if the type of the underlying member is not {@code String} or {@code
     * Object}
     * @return Optional string
     */
    String value() default "";

    /**
     * Used to specify the locale in one of the formats like {@code en}, {@code en/us}, {@code en-us}, or {@code en_US}.
     * One can provide either a two-symbol language token or a language-country pair (not: language must come first).
     * <p>If this parameter is specified it overrides the {@link I18nValue#localeDetector()} property. If neither
     * {@code locale} nor {@code localeDetector} are set, the locale is guessed based on the current resource page
     * @return Optional non-blank string
     */
    String locale() default "";

    /**
     * Used to specify a routine that will be used for detecting the current locale unless a particular value is set up
     * via {@link I18nValue#locale()}. A locale detector must implement the {@code Function<Object, Locale>} interface.
     * It is invoked with the call to the interface's {@link Function#apply(Object)} method.
     * <p>By default, the {@link PageLocaleDetector} is turned on. It detects the locale based on the current resource
     * page property. To bring back the AEM's default behavior (detection based on the authenticated user's properties),
     * specify {@link NativeLocaleDetector}
     * @return Optional {@code Class} reference
     */
    Class<? extends Function<Object, Locale>> localeDetector() default PageLocaleDetector.class;
}
