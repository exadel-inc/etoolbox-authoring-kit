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

import java.util.Collections;
import java.util.Locale;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.impl.injectors.ValueMapInjector;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.adobe.granite.ui.components.ExpressionResolver;

import com.exadel.aem.toolkit.api.annotations.injectors.models.TestModelExpression;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionInjectorIntegrationTest {

    @Rule
    public final AemContext context = new AemContext();

    @Mock
    private ExpressionResolver expressionResolver;

    @InjectMocks
    private ExpressionInjector injector;

    private TestModelExpression testModel;

    private ValueMap map;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelExpression.class);
        context.load().json("/com/exadel/aem/toolkit/api/annotations/injectors/page.json", "/content");
        context.currentResource("/content/test");
        context.registerService(ExpressionResolver.class, expressionResolver);
        context.registerInjectActivateService(new ValueMapInjector(),
            Collections.singletonMap("component.name", "org.apache.sling.models.impl.injectors.ValueMapInjector"));
        context.registerInjectActivateService(injector);
        when(expressionResolver.resolve(anyString(), any(), eq(String.class),
            any(MockSlingHttpServletRequest.class))).thenReturn("testFoo");
        when(expressionResolver.resolve(anyString(), any(), eq(Integer.class),
            any(MockSlingHttpServletRequest.class))).thenReturn(1);
        testModel = context.request().adaptTo(TestModelExpression.class);
        map = context.request().getResource().adaptTo(ValueMap.class);
    }

    @Test
    public void getFoo_shouldInjectFooFromTheCurrentResourceValueMap_whenAnnotationNameIsEmpty() {
        assertEquals(map.get("foo"), testModel.getFoo());
    }

    @Test
    public void getValueFromBar_shouldRetrievesBarFromTheCurrentResourceValueMapAndInjectIntoField() {
        assertEquals(map.get("bar"), testModel.getValueFromBar());
    }

    @Test
    public void getFooWithOr_shouldReturnValueOfBarFromTheCurrentResourcesValueMap_whenValueMapOfFooIsEmpty() {
        assertEquals(map.get("bar"), testModel.getFooWithOr());
    }

    @Test
    public void getFooWithOrAndDefaultValue_shouldReturnDefaultValue_whenValueMapOfFooIsEmptyAndBarIsNull() {
        assertEquals("Default Value", testModel.getFooWithOrAndDefaultValue());
    }

    @Test
    public void getFooTernary_shouldComputeValueMapValueOfFooAndProceedWithTernary_whenContextIsPopulatedCorrectly() {
        assertEquals("testFoo", testModel.getFooTernary());
        verify(expressionResolver).resolve(eq("${'testFoo' != bar ? 'testFoo' : 'not true'}"),
            eq(Locale.US), eq(String.class), any(SlingHttpServletRequest.class));
    }

    @Test
    public void getFooWithPrefix_shouldInjectFooFromTheCurrentResourceValueMapWithPrefixFromAnnotationName() {
        assertEquals("My: " + map.get("foo"), testModel.getFooWithPrefix());
    }

    @Test
    public void getFooTernaryIntValue_shouldComputeValueMapValueOfFooIntValueAndProceedWithTernary() {
        assertEquals(1, testModel.getFooTernaryIntValue());
    }
}
