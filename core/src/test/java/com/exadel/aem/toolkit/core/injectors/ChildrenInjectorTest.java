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

import com.exadel.aem.toolkit.core.injectors.models.TestModelChildren;

import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChildrenInjectorTest {

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelChildren.class);
        context.registerInjectActivateService(new ChildrenInjector());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/childInjector.json", "/content");
        ResourceResolver resolver = context.resourceResolver();
        context.request().setResource(resolver.getResource("/content/jcr:content"));
    }

    @Test
    public void testInjectorChildrenResourceItems() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getResourceItems();
        assertNotNull(resourceList);

        assertEquals(3, resourceList.size());
    }

    @Test
    public void testInjectorChildrenResourceList() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getResourceList();
        assertNotNull(resourceList);

        assertEquals(5, resourceList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels2() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels2();
        assertNotNull(modelList);

        assertEquals(2, modelList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels3() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels3();
        assertNotNull(modelList);

        assertEquals(2, modelList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels4() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels4();
        assertNotNull(modelList);

        assertEquals(1, modelList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels5() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels5();
        assertNotNull(modelList);

        assertEquals(1, modelList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels6() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels6();
        assertNotNull(modelList);

        assertEquals(2, modelList.size());
    }

    @Test
    public void testInjectorChildrenNotExistedResources() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getNotExistedResources();
        assertNull(resourceList);
    }

    @Test
    public void testInjectorChildrenNotExistedModel() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedModel();
        assertNull(modelList);
    }

    @Test
    public void testInjectorChildrenNotExistedPrefix() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedPrefix();
        assertNull(modelList);
    }

    @Test
    public void testInjectorChildrenNotExistedPostfix() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedPostfix();
        assertNull(modelList);
    }

    @Test
    public void testInjectorChildrenNotExistedFilter() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedFilter();
        assertNull(modelList);
    }
}
