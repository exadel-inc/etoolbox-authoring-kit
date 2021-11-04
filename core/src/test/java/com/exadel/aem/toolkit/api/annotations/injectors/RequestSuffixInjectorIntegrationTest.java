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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.api.annotations.injectors.models.TestModelSuffix;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RequestSuffixInjectorIntegrationTest {

    @Rule
    public final AemContext context = new AemContext();

    private TestModelSuffix testModel;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelSuffix.class);
        context.load().json("/com/exadel/aem/toolkit/api/annotations/injectors/page.json", "/content");
        context.registerInjectActivateService(new RequestSuffixInjector());
        context.currentResource("/content/test");
    }

    @Test
    public void shouldReturnSuffix() {
        context.requestPathInfo().setSuffix("/qwerty");
        testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertEquals("/qwerty", testModel.getSuffix());
    }

    @Test
    public void shouldReturnResourceFromSuffix() {
        context.requestPathInfo().setSuffix("/content/foo");
        testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNotNull(testModel.getSuffixResource());
        assertNotNull(testModel.getAbstractResource());
    }

    @Test
    public void shouldReturnNullIfSuffixIsNull() {
        testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNull(testModel.getSuffix());
    }

    @Test
    public void shouldReturnNullIfResourceNotExists() {
        context.requestPathInfo().setSuffix("/not-exists");
        testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNull(testModel.getSuffixResource());
    }

    @Test
    public void shouldReturnNullIfTypeIsNotSupported() {
        context.requestPathInfo().setSuffix("/type");
        testModel = context.request().adaptTo(TestModelSuffix.class);

        assertNotNull(testModel);
        assertNull(testModel.getSuffixTestModel());
        assertNull(testModel.getSuffixArray());
        assertNull(testModel.getSuffixList());
        assertEquals(0, testModel.getSuffixInt());
        assertEquals(0.0, testModel.getSuffixDouble(), 0.01);
    }
}
