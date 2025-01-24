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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.google.gson.Gson;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;

public class ListsJsonServletTest {

    private static final String SIMPLE_LIST_PATH = "/content/etoolbox-authoring-kit/lists/simpleList";
    private static final String CUSTOM_LIST_PATH = "/content/etoolbox-authoring-kit/lists/etoolbox-lists/customList";

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    private ListsJsonServlet servlet;

    @Before
    public void setUp() {
        servlet = new ListsJsonServlet();
        context.load().json("/com/exadel/aem/toolkit/core/lists/utils/simpleList.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/core/lists/utils/customList.json", CUSTOM_LIST_PATH);
        context.create().page("/content/etoolbox-authoring-kit/lists/dummy", StringUtils.EMPTY);
    }

    @Test
    public void shouldRetrieveSimpleJson() throws IOException {
        context.request().setResource(context.resourceResolver().getResource(SIMPLE_LIST_PATH + "/jcr:content"));
        servlet.doGet(context.request(), context.response());

        assertEquals(HttpStatus.SC_OK, context.response().getStatus());
        String jsonResponse = context.response().getOutputAsString();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = new Gson().fromJson(jsonResponse, List.class);
        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(item -> item.containsKey("value")));
    }

    @Test
    public void shouldRetrieveCustomJson() throws IOException {
        context.request().setResource(context.resourceResolver().getResource(CUSTOM_LIST_PATH + "/jcr:content"));
        servlet.doGet(context.request(), context.response());

        assertEquals(HttpStatus.SC_OK, context.response().getStatus());
        String jsonResponse = context.response().getOutputAsString();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = new Gson().fromJson(jsonResponse, List.class);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> item.containsKey("textValue")));
        assertTrue(result.stream().allMatch(item -> item.containsKey("booleanValue")));
    }

    @Test
    public void shouldReturn404ForNonList() throws IOException {
        context.request().setResource(context.resourceResolver().getResource("/content/etoolbox-authoring-kit/lists"));
        servlet.doGet(context.request(), context.response());

        assertEquals(HttpStatus.SC_NOT_FOUND, context.response().getStatus());

        context.request().setResource(context.resourceResolver().getResource("/content/etoolbox-authoring-kit/lists/dummy"));
        servlet.doGet(context.request(), context.response());

        assertEquals(HttpStatus.SC_NOT_FOUND, context.response().getStatus());
    }
}
