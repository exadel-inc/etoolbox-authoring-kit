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
package com.exadel.aem.toolkit.core.lists.servlets;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.adobe.granite.ui.components.ds.DataSource;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

public class ItemComponentsServletTest {

    private static final String GENERIC_ITEM_PATH = "/apps/etoolbox-authoring-kit/lists/components/content/genericItem";
    private static final String SIMPLE_ITEM_PATH = "/etc/etoolbox-authoring-kit/lists/components/content/simpleItem";

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

    @Before
    public void setUp() {
        context.load().json("/com/exadel/aem/toolkit/core/lists/datasource/genericItem.json", GENERIC_ITEM_PATH);
        context.load().json("/com/exadel/aem/toolkit/core/lists/datasource/simpleItem.json", SIMPLE_ITEM_PATH);
    }

    @Test
    public void shouldReturnDataSourceFromApps() {
        ItemComponentsServlet servlet = new ItemComponentsServlet();
        servlet.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());

        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
        assertEquals(GENERIC_ITEM_PATH, dataSource.iterator().next().getValueMap().get("value"));
        assertEquals("Generic List Item", dataSource.iterator().next().getValueMap().get("text"));
    }
}
