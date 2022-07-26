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
package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.i18n.ResourceBundleProvider;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.i18n.I18n;

import com.exadel.aem.toolkit.api.annotations.injectors.I18N;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.InstantiationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;


/**
 * Injects into a Sling model an {@link com.day.cq.i18n.I18n} object that corresponds to the current locale, or else an
 * internationalized string value
 * @see I18N
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class I18nInjector extends BaseInjectorTemplateMethod<I18N> {

    private static final Logger LOG = LoggerFactory.getLogger(I18nInjector.class);
    private static final Pattern LOCALE_PARTS_SPLITTER = Pattern.compile("[/_-]");

    public static final String NAME = "eak-etoolbox-i18n-injector";

    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    private List<ResourceBundleProvider> resourceBundleProviders;

    /**
     * Retrieves the name of the current instance
     * @return String value
     * @see Injector
     */
    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public I18N getAnnotation(AnnotatedElement element) {
        return element.getDeclaredAnnotation(I18N.class);
    }
    @Override
    public Object getValue(Object adaptable, String name, Type type, I18N annotation) {
        String value = StringUtils.defaultIfEmpty(annotation.value(), name);

        Function<Object, Locale> localeDetector = InstantiationUtil.getObjectInstance(annotation.localeDetector());
        Locale locale = StringUtils.isNotBlank(annotation.locale())
            ? getLocale(annotation.locale())
            : getLocale(adaptable, localeDetector);

        I18n i18n = getI18n(adaptable, locale);

        if (isI18nType(type)) {
            return i18n;
        } else if (TypeUtil.isValidObjectType(type, String.class)) {
            return i18n.get(value);
        }
        return null;
    }
    @Override
    public void logError(Type type) {
        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
    }

    /**
     * Creates a new {@link Locale} object from the provided string token
     * @param value String value parsed to create a {@code Locale}
     * @return {@code Locale} instance
     */
    private Locale getLocale(String value) {
        String[] parts = LOCALE_PARTS_SPLITTER.split(value, 2);
        if (parts.length == 2) {
            return new Locale(parts[0].toLowerCase(), parts[1].toLowerCase());
        }
        return new Locale(parts[0].toLowerCase());
    }

    /**
     * Creates a new {@link Locale} object from the given adaptable object using the provided locale detector
     * @param adaptable A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param detector  A routine used to guess the proper locale from the current request or resource
     * @return {@code Locale} instance; might be null
     */
    private Locale getLocale(
        Object adaptable,
        Function<Object, Locale> detector) {

        return Optional.ofNullable(detector).map(d -> detector.apply(adaptable)).orElse(null);
    }

    /**
     * Retrieves an {@link I18n} object for the given adaptable and locale
     * @param adaptable A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param locale    A nullable {@link Locale} object
     * @return {@code I18n} instance
     */
    private I18n getI18n(Object adaptable, Locale locale) {
        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);
        if (request != null && locale != null) {
            return new I18n(request.getResourceBundle(locale));
        } else if (request != null) {
            return new I18n(request);
        }
        ResourceBundle resourceBundle = CollectionUtils.emptyIfNull(resourceBundleProviders)
            .stream()
            .map(provider -> provider.getResourceBundle(locale != null ? locale : Locale.getDefault()))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        return new I18n(resourceBundle);
    }

    /**
     * Gets whether the provided member {@code Type} is assignable to {@link I18n}
     * @param value {@code Type} reference
     * @return True or false
     */
    private static boolean isI18nType(Type value) {
        return value instanceof Class<?> && ClassUtils.isAssignable((Class<?>) value, I18n.class);
    }

}
