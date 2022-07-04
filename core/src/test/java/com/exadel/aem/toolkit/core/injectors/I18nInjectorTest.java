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

import org.apache.sling.i18n.ResourceBundleProvider;
import org.apache.sling.testing.mock.sling.MockResourceBundle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.core.injectors.models.ITestModelI18n;
import com.exadel.aem.toolkit.core.injectors.models.TestModelI18n;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class I18nInjectorTest {

    private static final String SOURCE_STRING = "Hello world";
    private static final String TRANSLATED_STRING = "Ciao mondo";

    private static final Locale LOCALE_IT = new Locale("it", "it");

    @Rule
    public final AemContext context = new AemContext();

    private TestModelI18n testModel;

    @Before
    public void beforeTest() {
        context.registerInjectActivateService(new I18nInjector());
        context.addModelsForClasses(TestModelI18n.class, ITestModelI18n.class);

        ResourceBundleProvider resourceBundleProvider = context.getService(ResourceBundleProvider.class);
        assert resourceBundleProvider != null;
        MockResourceBundle resourceBundle = (MockResourceBundle) resourceBundleProvider.getResourceBundle(LOCALE_IT);
        resourceBundle.put(SOURCE_STRING, TRANSLATED_STRING);
        resourceBundle.put("helloWorld", "ciaoMondo");

        context.load().json("/com/exadel/aem/toolkit/core/injectors/i18nInjector.json", "/content/site/it-it");
        context.request().setResource(context.resourceResolver().getResource("/content/site/it-it/page/jcr:content/resource"));

        testModel = context.request().adaptTo(TestModelI18n.class);
    }

    @Test
    public void shouldInjectI18nObject() {
        // Matched by the Page#getLanguage() value
        assertNotNull(testModel.getI18n());
        assertEquals(TRANSLATED_STRING, testModel.getI18n().get(SOURCE_STRING));

        // Matched by the explicitly set locale
        assertNotNull(testModel.getI18nLocale());
        assertEquals(TRANSLATED_STRING, testModel.getI18nLocale().get(SOURCE_STRING));

        // Matched by the custom locale detector
        assertNotNull(testModel.getI18nDetector());
        assertEquals(TRANSLATED_STRING, testModel.getI18nDetector().get(SOURCE_STRING));

        // Matched by the native routine
        assertNotNull(testModel.getI18nNative());
        assertEquals(SOURCE_STRING, testModel.getI18nNative().get(SOURCE_STRING));
    }

    @Test
    public void shouldInjectI18nStringByFieldName() {
        // Matched by the Page#getLanguage() value
        assertEquals("ciaoMondo", testModel.getHelloWorld());
    }

    @Test
    public void shouldInjectI18nStringByValue() {
        // Matched by the explicitly set locale
        assertEquals(TRANSLATED_STRING, testModel.getHelloWorldLocale());

        // Matched by the custom locale detector
        assertNotNull(testModel.getI18nDetector());
        assertEquals(TRANSLATED_STRING, testModel.getHelloWorldDetector());

        // Matched by the native routine
        assertEquals(SOURCE_STRING, testModel.getHelloWorldNative());
    }

    @Test
    public void shouldInjectConstructorArguments() {
        assertNotNull(testModel.getI18nConstructor());
        assertEquals(TRANSLATED_STRING, testModel.getI18nConstructor().get(SOURCE_STRING));
    }

    @Test
    public void shouldInjectViaMethod() {
        ITestModelI18n testModel = context.request().adaptTo(ITestModelI18n.class);
        assertNotNull(testModel);

        assertNotNull(testModel.getI18n());
        assertEquals(TRANSLATED_STRING, testModel.getI18n().get(SOURCE_STRING));

        assertEquals("ciaoMondo", testModel.getHelloWorld());

        assertEquals(TRANSLATED_STRING, testModel.getHelloWorldNamed());
    }

    @Test
    public void shouldInjectInResourceAdaptable() {
        TestModelI18n testModel = context.request().getResource().adaptTo(TestModelI18n.class);
        assertNotNull(testModel);
        assertNotNull(testModel.getI18n());
        assertEquals(TRANSLATED_STRING, testModel.getI18n().get(SOURCE_STRING));
    }
}
