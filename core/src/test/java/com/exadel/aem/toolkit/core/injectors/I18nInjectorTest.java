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

import java.util.Locale;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.i18n.ResourceBundleProvider;
import org.apache.sling.testing.mock.sling.MockResourceBundle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.i18n.I18nInterface;
import com.exadel.aem.toolkit.core.injectors.models.i18n.Objects;
import com.exadel.aem.toolkit.core.injectors.models.i18n.Strings;

public class I18nInjectorTest {

    private static final String MODELS_PACKAGE_NAME = CoreConstants.ROOT_PACKAGE + ".core.injectors.models.i18n";

    private static final String SOURCE_STRING = "Hello world";
    private static final String TRANSLATED_STRING = "Ciao mondo";

    private static final String SOURCE_FIELD_NAME = "value";
    private static final String TRANSLATED_FIELD_NAME = "valore";

    private static final Locale LOCALE_IT = new Locale("it", "it");

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        context.registerInjectActivateService(new I18nInjector());
        context.addModelsForPackage(MODELS_PACKAGE_NAME);

        ResourceBundleProvider resourceBundleProvider = context.getService(ResourceBundleProvider.class);
        assert resourceBundleProvider != null;
        MockResourceBundle resourceBundle = (MockResourceBundle) resourceBundleProvider.getResourceBundle(LOCALE_IT);
        resourceBundle.put(SOURCE_STRING, TRANSLATED_STRING);
        resourceBundle.put(SOURCE_FIELD_NAME, TRANSLATED_FIELD_NAME);

        context.load().json("/com/exadel/aem/toolkit/core/injectors/i18nInjector.json", "/content/site/it-it");
        context.request().setResource(context.resourceResolver().getResource("/content/site/it-it/page/jcr:content/resource"));
    }

    /* -----
       Tests
       ----- */

    @Test
    public void shouldInjectI18nObject() {
        shouldInjectI18nObject(context.request());
        shouldInjectI18nObject(context.request().getResource());
    }

    private void shouldInjectI18nObject(Adaptable adaptable) {
        Objects model = adaptable.adaptTo(Objects.class);
        assertNotNull(model);

        // Matched by the Page#getLanguage() value
        assertNotNull(model.getI18n());
        assertEquals(TRANSLATED_STRING, model.getI18n().get(SOURCE_STRING));

        // Matched by the Page#getLanguage() value; injected via constructor
        assertNotNull(model.getConstructorI18n());
        assertEquals(TRANSLATED_STRING, model.getConstructorI18n().get(SOURCE_STRING));

        // Matched by the explicitly set locale
        assertNotNull(model.getI18nLocale());
        assertEquals(TRANSLATED_STRING, model.getI18nLocale().get(SOURCE_STRING));

        // Matched by the custom locale detector
        assertNotNull(model.getI18nDetector());
        assertEquals(TRANSLATED_STRING, model.getI18nDetector().get(SOURCE_STRING));

        // Matched by the native routine
        assertNotNull(model.getI18nNative());
        assertEquals(SOURCE_STRING, model.getI18nNative().get(SOURCE_STRING));
    }

    @Test
    public void shouldInjectI18nString() {
        shouldInjectI18nString(context.request());
        shouldInjectI18nString(context.request().getResource());
    }

    private void shouldInjectI18nString(Adaptable adaptable) {
        Strings model = adaptable.adaptTo(Strings.class);
        assertNotNull(model);

        // Matched by the Page#getLanguage() value
        assertEquals(TRANSLATED_FIELD_NAME, model.getValue());
        assertEquals(TRANSLATED_FIELD_NAME, model.getObjectValue());

        // Matched by the explicitly set locale
        assertEquals(TRANSLATED_STRING, model.getValueByLocale());

        // Matched by the custom locale detector
        assertNotNull(model.getValueByDetector());
        assertEquals(TRANSLATED_STRING, model.getValueByDetector());

        // Matched by the native routine
        assertEquals(SOURCE_STRING, model.getValueByNativeDetector());
    }

    @Test
    public void shouldInjectViaInterfaceMethod() {
        shouldInjectViaInterfaceMethod(context.request());
        shouldInjectViaInterfaceMethod(context.request().getResource());
    }

    private void shouldInjectViaInterfaceMethod(Adaptable adaptable) {
        I18nInterface model = adaptable.adaptTo(I18nInterface.class);
        assertNotNull(model);

        assertNotNull(model.getI18n());
        assertEquals(TRANSLATED_STRING, model.getI18n().get(SOURCE_STRING));
        assertEquals(TRANSLATED_FIELD_NAME, model.getValue());
        assertEquals(TRANSLATED_STRING, model.getNamedValue());
    }
}
