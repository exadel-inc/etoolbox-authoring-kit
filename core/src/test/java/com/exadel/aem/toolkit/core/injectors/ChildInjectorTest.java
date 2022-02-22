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

import com.exadel.aem.toolkit.core.injectors.models.TestModelChild;

import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ChildInjectorTest {

    private static final String EXPECTED_RESOURCE_TYPE1 = "wcm/foundation/components/responsivegrid";
    private static final String EXPECTED_RESOURCE_TYPE2 = "etoolbox-authoring-kit/lists/components/content/listItem";
    private static TestModelChild testModel;

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelChild.class);
        context.registerInjectActivateService(new ChildInjector());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/childInjector.json", "/content");
        ResourceResolver resolver = context.resourceResolver();
        context.request().setResource(resolver.getResource("/content/jcr:content"));
        testModel = context.request().adaptTo(TestModelChild.class);
    }

    @Test
    public void shouldInjectChild() {
        assertNotNull(testModel);
        Resource actualResource = testModel.getList();

        assertNotNull(actualResource);
        assertEquals(EXPECTED_RESOURCE_TYPE2, actualResource.getResourceType());
    }

    @Test
    public void shouldInjectChild2() {
        assertNotNull(testModel);
        Resource actualResource = testModel.getListItemResource();

        assertNotNull(actualResource);
        assertEquals(EXPECTED_RESOURCE_TYPE1, actualResource.getResourceType());
    }

    @Test
    public void shouldInjectChildName() {
        assertNotNull(testModel);
        ListItemModel itemModel = testModel.getListItemModel();

        assertNotNull(itemModel);
        assertEquals(8L, itemModel.getProperties().size());
    }

    @Test
    public void shouldInjectChildPrefix() {
        assertNotNull(testModel);
        ListItemModel itemModel = testModel.getListItemModel1();

        assertNotNull(itemModel);
        assertEquals(3L, itemModel.getProperties().size());

        Map<String, Object> expected = new HashMap<>();
        expected.put("prefix_value", "pref_value");
        expected.put("prefix_value_2", "pref_value_2");
        expected.put("prefix_value_3", "pref_value_3");
        assertEquals(expected, itemModel.getProperties());
    }

    @Test
    public void shouldInjectChildPostfix() {
        assertNotNull(testModel);
        ListItemModel itemModel = testModel.getListItemModel2();

        assertNotNull(itemModel);
        assertEquals(3L, itemModel.getProperties().size());

        Map<String, Object> expected = new HashMap<>();
        expected.put("value_1_postfix", "value_1_postfix");
        expected.put("value_2_postfix", "value_2_postfix");
        expected.put("value_3_postfix", "value_3_postfix");
        assertEquals(expected, itemModel.getProperties());
    }

    @Test
    public void shouldInjectChildPrefixPostfix() {
        assertNotNull(testModel);
        ListItemModel itemModel = testModel.getListItemModel3();

        assertNotNull(itemModel);
        assertEquals(6L, itemModel.getProperties().size());

        Map<String, Object> expected = new HashMap<>();
        expected.put("prefix_value", "pref_value");
        expected.put("prefix_value_2", "pref_value_2");
        expected.put("prefix_value_3", "pref_value_3");
        expected.put("value_1_postfix", "value_1_postfix");
        expected.put("value_2_postfix", "value_2_postfix");
        expected.put("value_3_postfix", "value_3_postfix");
        assertEquals(expected, itemModel.getProperties());
    }
}
