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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.core.injectors.models.ITestModelSelectors;
import com.exadel.aem.toolkit.core.injectors.models.TestModelSelectors;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RequestSelectorsInjectorTest {
    private static final String SELECTOR_1 = "selector1";
    private static final String SELECTOR_2 = "selector2";
    private static final String SELECTORS_1_2 = SELECTOR_1 + "." + SELECTOR_2;

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelSelectors.class);
        context.registerInjectActivateService(new RequestSelectorsInjector());
    }

    @Test
    public void shouldInjectSelector() {
        context.requestPathInfo().setSelectorString(SELECTOR_1);
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals("selector1", testModel.getSelectorsString());
    }

    @Test
    public void shouldInjectSelectorObject() {
        context.requestPathInfo().setSelectorString(SELECTOR_2);
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals(SELECTOR_2, testModel.getSelectorsObject());
    }

    @Test
    public void shouldInjectSelectorCollection() {
        context.requestPathInfo().setSelectorString(SELECTORS_1_2);
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals(Arrays.asList(SELECTOR_1, SELECTOR_2), testModel.getSelectorsCollection());
    }

    @Test
    public void shouldInjectSelectorList() {
        context.requestPathInfo().setSelectorString(SELECTORS_1_2);
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals(Arrays.asList(SELECTOR_1, SELECTOR_2), testModel.getSelectorsList());
    }

    @Test
    public void shouldInjectSelectorArray() {
        context.requestPathInfo().setSelectorString(SELECTORS_1_2);
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertArrayEquals(new String[]{SELECTOR_1, SELECTOR_2}, testModel.getSelectorsArray());
    }

    @Test
    public void shouldInjectIntoMethodParameter() {
        context.requestPathInfo().setSelectorString(SELECTOR_1);
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertEquals(SELECTOR_1, testModel.getSelectorsFromParameter());
    }

    @Test
    public void shouldInjectIntoMethod() {
        context.requestPathInfo().setSuffix(SELECTOR_2);
        ITestModelSelectors testModel = context.request().adaptTo(ITestModelSelectors.class);
        assertNotNull(testModel);

        String expectedSelectors = context.requestPathInfo().getSelectorString();
        String actualSelectors = testModel.getSelectorsFromMethod();
        assertEquals(expectedSelectors, actualSelectors);
    }

    @Test
    public void shouldNotInjectIfSelectorsMissing() {
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertNull(testModel.getSelectorsString());
    }

    @Test
    public void shouldNotInjectIfWrongType() {
        context.requestPathInfo().setSelectorString(SELECTOR_1);
        TestModelSelectors testModel = context.request().adaptTo(TestModelSelectors.class);

        assertNotNull(testModel);
        assertNull(testModel.getSelectorsArrayInt());
        assertNull(testModel.getSelectorsListInt());
        assertNull(testModel.getSelectorsSet());
        assertEquals(0, testModel.getSelectorsInt());
    }
}
