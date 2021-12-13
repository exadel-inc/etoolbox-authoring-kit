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
import static org.junit.Assert.assertNull;

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

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelChild.class);
        context.registerInjectActivateService(new ChildInjector());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/childInjector.json", "/content");
        ResourceResolver resolver = context.resourceResolver();
        context.request().setResource(resolver.getResource("/content/jcr:content"));
    }

    @Test
    public void testInjectChild() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        Resource actualResource = testModel.getList();
        assertNotNull(actualResource);
        assertEquals(EXPECTED_RESOURCE_TYPE1, actualResource.getResourceType());
    }

    @Test
    public void testInjectChildName() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        Resource actualResource = testModel.getListItemResource();
        assertNotNull(actualResource);
        assertEquals(EXPECTED_RESOURCE_TYPE1, actualResource.getResourceType());
    }

    @Test
    public void testInjectChildName2() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        Resource actualResource = testModel.getListItemResource2();
        assertNotNull(actualResource);
        assertEquals(EXPECTED_RESOURCE_TYPE1, actualResource.getResourceType());
    }

    @Test
    public void testInjectChildNameNested() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        Resource actualResource = testModel.getNestedResource();
        assertNotNull(actualResource);
        assertEquals(EXPECTED_RESOURCE_TYPE2, actualResource.getResourceType());
    }

    @Test
    public void testInjectChildAdapted() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        ListItemModel actualListItem = testModel.getListItemModel();
        assertNotNull(actualListItem);

        Map<String, Object> expected = new HashMap<>();
        expected.put("jcr:title", "key2");
        expected.put("value", "value2");
        Map<String, Object> actual = actualListItem.getProperties();
        assertEquals(expected, actual);
    }

    @Test
    public void testInjectChildPrefix() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        ListItemModel actualListItem = testModel.getListItemModel2();
        assertNotNull(actualListItem);

        Map<String, Object> expected = new HashMap<>();
        expected.put("jcr:title", "key1");
        expected.put("value", "value1");
        Map<String, Object> actual = actualListItem.getProperties();
        assertEquals(expected, actual);
    }

    @Test
    public void testInjectorPostfix() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        ListItemModel actualListItem = testModel.getListItemModel3();
        assertNotNull(actualListItem);

        Map<String, Object> expected = new HashMap<>();
        expected.put("jcr:title", "key11");
        expected.put("value", "nestedList2");
        Map<String, Object> actual = actualListItem.getProperties();
        assertEquals(expected, actual);
    }

    @Test
    public void testInjectorNotExistedResource() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        Resource resource = testModel.getNotExistedResource();
        assertNull(resource);
    }

    @Test
    public void testInjectorNotExistedModel() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        ListItemModel model = testModel.getNotExistedModel();
        assertNull(model);
    }

    @Test
    public void testInjectorNotExistedPrefix() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        ListItemModel model = testModel.getNotExistedPrefix();
        assertNull(model);
    }

    @Test
    public void testInjectorNotExistedPostfix() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        ListItemModel model = testModel.getNotExistedPostfix();
        assertNull(model);
    }
}
