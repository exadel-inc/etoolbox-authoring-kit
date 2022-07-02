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

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.MockAdapterManagerImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.ITestModelChild;
import com.exadel.aem.toolkit.core.injectors.models.TestModelChild;
import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ChildInjectorTest {

    private static final String EXPECTED_RESOURCE_TYPE1 = "wcm/foundation/components/responsivegrid";
    private static final String EXPECTED_RESOURCE_TYPE2 = "etoolbox-authoring-kit/lists/components/content/listItem";

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelChild.class, ITestModelChild.class);
        context.registerInjectActivateService(new ChildInjector());
        context.registerInjectActivateService(new MockAdapterManagerImpl());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/childInjector.json", "/content");
        context.request().setResource(context.resourceResolver().getResource("/content/jcr:content"));
    }

    @Test
    public void shouldInjectByFieldName() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        assertNotNull(testModel.getDefaultChild());
        assertEquals("Hello", testModel.getDefaultChild().getTitle());
    }

    @Test
    public void shouldInjectByArbitraryName() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        Resource resource1 = testModel.getListResource();
        assertNotNull(resource1);
        assertEquals(EXPECTED_RESOURCE_TYPE1, resource1.getResourceType());

        Object resource2 = testModel.getListItemResource();
        assertNotNull(resource2);
        assertEquals(EXPECTED_RESOURCE_TYPE2, ((Resource) resource2).getResourceType());
    }

    @Test
    public void shouldInjectByParentPath() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        assertNotNull(testModel.getParent());
        assertEquals("Sample Page", testModel.getParent().getValueMap().get(JcrConstants.JCR_TITLE));
    }

    @Test
    public void shouldInjectByPath() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        ListItemModel itemModelByAbsPath = testModel.getModelByAbsolutePath();
        assertNotNull(itemModelByAbsPath);
        assertEquals(10, itemModelByAbsPath.getProperties().size());
        assertEquals("nestedListItem2", itemModelByAbsPath.getProperties().get(CoreConstants.PN_VALUE));

        ListItemModel itemModelByRelPath = testModel.getModelByRelativePath();
        assertNotNull(itemModelByRelPath);
        assertEquals(10, itemModelByRelPath.getProperties().size());
    }

    @Test
    public void shouldInjectFilteredByPrefix() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        ListItemModel itemModel = testModel.getModelFilteredByPrefix();
        assertNotNull(itemModel);
        assertEquals(4, itemModel.getProperties().size());

        Map<String, Object> expected = new HashMap<>();
        expected.put("value", "Value with prefix");
        expected.put("value_2", "Value with prefix 2");
        expected.put("value_3", "Value with prefix 3");
        expected.put("value_postfix", "Value with prefix and postfix");
        assertEquals(expected, itemModel.getProperties());
    }

    @Test
    public void shouldInjectFilteredByPostfix() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        ListItemModel itemModel = testModel.getModelFilteredByPostfix();
        assertNotNull(itemModel);
        assertEquals(4, itemModel.getProperties().size());

        Map<String, Object> expected = new HashMap<>();
        expected.put("value_1", "Value with postfix");
        expected.put("value_2", "Value with postfix 2");
        expected.put("value_3", "Value with postfix 3");
        expected.put("prefix_value", "Value with prefix and postfix");
        assertEquals(expected, itemModel.getProperties());
    }

    @Test
    public void shouldInjectFilteredByPrefixAndPostfix() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        ListItemModel itemModel = testModel.getModelFilteredByPrefixAndPostfix();
        assertNotNull(itemModel);
        assertEquals(1, itemModel.getProperties().size());
        assertEquals(42L, itemModel.getProperties().get("property"));
    }

    @Test
    public void shouldInjectModelAdaptedFromRequest() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);
        assertNotNull(testModel.getModelAdaptedFromRequest());
        assertNotNull(testModel.getModelAdaptedFromRequest().getRequest());
        assertEquals("nestedValue", testModel.getModelAdaptedFromRequest().getNestedProperty());
    }

    @Test
    public void shouldInjectOwnResource() {
        context.request().setResource(context.resourceResolver().getResource("/content/jcr:content/list/nested-node/nested_list_item_2"));

        TestModelChild testModelSelf = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModelSelf);
        assertNotNull(testModelSelf.getSelfResourceFiltered());
        assertEquals(4, testModelSelf.getSelfResourceFiltered().getValueMap().size());
    }

    @Test
    public void shouldInjectConstructorArguments() {
        TestModelChild testModel = context.request().adaptTo(TestModelChild.class);
        assertNotNull(testModel);

        assertEquals(
            testModel.getConstructorArgument1().getValueMap().get(JcrConstants.JCR_TITLE),
            ((Resource) testModel.getListItemResource()).getValueMap().get(JcrConstants.JCR_TITLE));
        assertEquals(
            testModel.getConstructorArgument1().getValueMap().get(CoreConstants.PN_VALUE),
            ((Resource) testModel.getListItemResource()).getValueMap().get(CoreConstants.PN_VALUE));

        assertEquals(
            testModel.getConstructorArgument2().getProperties().get(JcrConstants.JCR_TITLE),
            testModel.getModelByRelativePath().getProperties().get(JcrConstants.JCR_TITLE));
        assertEquals(
            testModel.getConstructorArgument2().getProperties().get(CoreConstants.PN_VALUE),
            testModel.getModelByRelativePath().getProperties().get(CoreConstants.PN_VALUE));
    }

    @Test
    public void shouldInjectViaMethod() {
        ITestModelChild testModel = context.request().adaptTo(ITestModelChild.class);
        assertNotNull(testModel);

        assertEquals("key11", testModel.getInjectedViaMethod().getProperties().get(JcrConstants.JCR_TITLE));
        assertEquals("nestedListItem2", testModel.getInjectedViaMethod().getProperties().get(CoreConstants.PN_VALUE));
    }
}
