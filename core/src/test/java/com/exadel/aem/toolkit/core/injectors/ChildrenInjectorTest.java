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

import java.util.List;

import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.exadel.aem.toolkit.core.injectors.models.TestModelChildren;
import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

public class ChildrenInjectorTest {

    private static TestModelChildren testModel;

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelChildren.class);
        context.registerInjectActivateService(new ChildrenInjector());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/childInjector.json", "/content");
        ResourceResolver resolver = context.resourceResolver();
        context.request().setResource(resolver.getResource("/content/jcr:content"));
        testModel = context.request().adaptTo(TestModelChildren.class);
    }

    @Test
    public void testInjectorChildrenResourceItems() {
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getList();
        assertNotNull(resourceList);

        assertEquals(5L, resourceList.size());
    }

    @Test
    public void testInjectorChildrenResourceList() {
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getResourceList();
        assertNotNull(resourceList);

        assertEquals(5L, resourceList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels2() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels2();
        assertNotNull(modelList);

        assertEquals(4L, modelList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels3() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels3();
        assertNotNull(modelList);

        assertEquals(4L, modelList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels4() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels4();
        assertNotNull(modelList);

        assertEquals(4L, modelList.size());
    }

    @Test
    public void testInjectorChildrenListItemModels5() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModels5();
        assertNotNull(modelList);

        assertEquals(2L, modelList.size());
    }

    @Test
    public void testInjectorChildrenNotExistedResources() {
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getNotExistedResources();
        assertNull(resourceList);
    }

    @Test
    public void testInjectorChildrenNotExistedModel() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedModel();
        assertNull(modelList);
    }

    @Test
    public void testInjectorChildrenNotExistedPrefix() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedPrefix();
        assertNull(modelList);
    }

    @Test
    public void testInjectorChildrenNotExistedPostfix() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedPostfix();
        assertNull(modelList);
    }

    @Test
    public void testInjectorChildrenNotExistedFilter() {
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNotExistedFilter();
        assertNull(modelList);
    }
}
