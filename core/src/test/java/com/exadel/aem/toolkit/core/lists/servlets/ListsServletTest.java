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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.ds.DataSource;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ListsServletTest {
    private static final String SIMPLE_LIST_PATH = "/content/etoolbox-authoring-kit/lists/simpleList";
    private static final String CUSTOM_LIST_PATH = "/content/etoolbox-authoring-kit/lists/etoolbox-lists/customList";
    private static final String DATASOURCE_PATH = "/datasource";

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

    @Mock
    private ExpressionResolver expressionResolver;

    @InjectMocks
    private ListsServlet servlet;

    @Before
    public void setUp() {
        context.load().json("/com/exadel/aem/toolkit/core/lists/utils/simpleList.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/core/lists/utils/customList.json", CUSTOM_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/core/lists/datasource/datasource.json", DATASOURCE_PATH);

        Mockito.when(expressionResolver.resolve("${requestPathInfo.suffix}", Locale.US, String.class, context.request())).thenReturn("/content/etoolbox-authoring-kit/lists");
        Mockito.when(expressionResolver.resolve("${requestPathInfo.selectors[0]}", Locale.US, Integer.class, context.request())).thenReturn(0);
        Mockito.when(expressionResolver.resolve("${empty requestPathInfo.selectors[1] ? &quot;41&quot; : requestPathInfo.selectors[1] + 1}", Locale.US, Integer.class, context.request())).thenReturn(100);
    }

    @Test
    public void shouldReturnDataSourceFromContent() {
        List<String> expected = Arrays.asList("simpleList", "etoolbox-lists");

        context.request().setResource(context.resourceResolver().getResource(DATASOURCE_PATH));
        servlet.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        List<String> actualList = new ArrayList<>();
        Iterator<Resource> it = dataSource.iterator();
        while (it.hasNext()) {
            actualList.add(it.next().getName());
        }
        assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
        assertEquals(expected, actualList);
    }
}
