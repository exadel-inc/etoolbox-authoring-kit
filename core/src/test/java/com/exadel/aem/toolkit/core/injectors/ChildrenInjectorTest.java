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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.injectors.models.ITestModelChildren;
import com.exadel.aem.toolkit.core.injectors.models.TestModelChildren;
import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

import io.wcm.testing.mock.aem.junit.AemContext;
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
        context.request().setResource(context.resourceResolver().getResource("/content/jcr:content"));
    }

    /* -----------
       Valid cases
       ----------- */

    @Test
    public void shouldInjectByFieldName() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getList();
        assertNotNull(resourceList);
        assertEquals(5, resourceList.size());
    }

    @Test
    public void shouldInjectByArbitraryName() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getResourceList();
        assertNotNull(resourceList);
        assertEquals(5, resourceList.size());
    }

    @Test
    public void shouldInjectModelsByRelativePath() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        Collection<ListItemModel> modelList = testModel.getListItemModels();
        assertNotNull(modelList);
        assertEquals(2, modelList.size());
    }

    @Test
    public void shouldInjectChildrenUsePrefix() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        ListItemModel[] modelList = testModel.getListItemModelsWithPrefix();
        assertNotNull(modelList);
        assertEquals(1, modelList.length);
    }

    @Test
    public void shouldInjectChildrenUsePostfix() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModelsWithPostfix();
        assertNotNull(modelList);
        assertEquals(1, modelList.size());
        assertEquals(3, modelList.get(0).getProperties().size());
    }

    @Test
    public void shouldInjectChildrenUseFilters() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getListItemModelsFiltered();
        assertNotNull(modelList);
        assertEquals(1, modelList.size());
    }

    @Test
    public void shouldInjectOwnChildren() {
        context.request().setResource(context.resourceResolver().getResource("/content/jcr:content/list"));
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);
        assertNotNull(testModel.getOwnList());
        assertEquals(5, testModel.getOwnList().size());
    }

    @Test
    public void shouldInjectConstructorArguments() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> models = new ArrayList<>(testModel.getListItemModels());

        assertEquals(testModel.getInjectedViaConstructor().length, models.size());
        for (int i = 0; i < testModel.getInjectedViaConstructor().length; i++) {
            assertEquals(
                testModel.getInjectedViaConstructor()[i].getProperties().get(JcrConstants.JCR_TITLE),
                models.get(i).getProperties().get(JcrConstants.JCR_TITLE));
        }
    }

    @Test
    public void shouldInjectViaMethod() {
        ITestModelChildren testModel = context.request().adaptTo(ITestModelChildren.class);
        assertNotNull(testModel);

        assertEquals(2, testModel.getInjectedViaMethod().size());
        assertEquals("key1", testModel.getInjectedViaMethod().get(0).getProperties().get(JcrConstants.JCR_TITLE));
    }


    /* -------------
       Invalid cases
       ------------- */

    @Test
    public void shouldNotInjectNonExistentResource() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<Resource> resourceList = testModel.getNonExistentResources();
        assertNull(resourceList);

        List<ListItemModel> modelList = testModel.getNotExistentModel();
        assertNull(modelList);
    }

    @Test
    public void shouldNotInjectIfNotMatchingPrefix() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getNonExistentPrefix();
        assertNull(modelList);
    }

    @Test
    public void shouldNotInjectIfNotMatchingFilters() {
        TestModelChildren testModel = context.request().adaptTo(TestModelChildren.class);
        assertNotNull(testModel);

        List<ListItemModel> modelList = testModel.getUnmatchedFilters();
        assertNull(modelList);
    }
}
