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
package com.exadel.aem.toolkit.core.lists.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMException;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;

@RunWith(MockitoJUnitRunner.class)
public class ListPageUtilTest {

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    @Test
    public void shouldNotCreatePageIfCannotGetPageManager() {
        // Needed to emulate a case when {@code PageManager} is null
        ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
        assertThrows(WCMException.class, () -> ListPageUtil.createPage(mockResourceResolver, "/test/path"));
    }

    @Test
    public void shouldThrowExceptionIfPathInvalid() {
        assertThrows(WCMException.class, () -> ListPageUtil.createPage(context.resourceResolver(), "/nocontent/test"));
        assertThrows(WCMException.class, () -> ListPageUtil.createPage(context.resourceResolver(), "nocontent/test"));
        assertThrows(WCMException.class, () -> ListPageUtil.createPage(context.resourceResolver(), "/content/../../test"));
    }

    @Test
    public void shouldCreateListPageUnderExisingPath() throws WCMException, PersistenceException {
        context.create().page(TestConstants.ROOT_RESOURCE);
        Page listPage = ListPageUtil.createPage(context.resourceResolver(), "/content/test");
        assertNotNull(listPage);

        ValueMap properties = listPage.getProperties();
        assertNotNull(properties);
        assertEquals(ListConstants.LIST_TEMPLATE_NAME, properties.get(NameConstants.NN_TEMPLATE, StringUtils.EMPTY));
        assertEquals(ListConstants.SIMPLE_LIST_ITEM_RESOURCE_TYPE, properties.get(CoreConstants.PN_ITEM_RESOURCE_TYPE, StringUtils.EMPTY));
    }


    @Test
    public void shouldCreateListPageUnderNonexistentPath() throws WCMException, PersistenceException {
        context.create().page("/content");

        Page listPage = ListPageUtil.createPage(context.resourceResolver(), "/content/nested/node/../node/test");

        Resource nested = context.resourceResolver().getResource("/content/nested");
        assertNotNull(nested);
        assertEquals(JcrConstants.NT_UNSTRUCTURED, nested.getResourceType());

        Resource nestedNode = context.resourceResolver().getResource("/content/nested/node");
        assertNotNull(nestedNode);
        assertEquals(JcrConstants.NT_UNSTRUCTURED, nestedNode.getResourceType());

        assertNotNull(listPage);
        ValueMap properties = listPage.getProperties();
        assertNotNull(properties);
        assertEquals(ListConstants.LIST_TEMPLATE_NAME, properties.get(NameConstants.NN_TEMPLATE, StringUtils.EMPTY));
        assertEquals(ListConstants.SIMPLE_LIST_ITEM_RESOURCE_TYPE, properties.get(CoreConstants.PN_ITEM_RESOURCE_TYPE, StringUtils.EMPTY));
    }
}
