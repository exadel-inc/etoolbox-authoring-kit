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
package com.exadel.aem.toolkit.core.optionprovider.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.injectors.models.enums.Colors;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderServiceImpl;

public class OptionProviderServletTest {

    private static final String MOCK_DATA = "/com/exadel/aem/toolkit/core/optionprovider/content.json";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    private OptionProviderServlet servlet;

    @Before
    public void setUp() throws ClassNotFoundException {
        context.load().json(MOCK_DATA, TestConstants.ROOT_RESOURCE);
        context.request().setResource(context.resourceResolver().getResource(TestConstants.ROOT_RESOURCE));
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setResourcePath(ResourceTypes.OPTION_PROVIDER);
        context.registerInjectActivateService(new OptionProviderServiceImpl());
        servlet = context.registerInjectActivateService(new OptionProviderServlet());

        Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.doReturn(Colors.class).when(bundle).loadClass(Colors.class.getName());
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        Mockito.when(bundleContext.getBundle()).thenReturn(bundle);
        context.request().setAttribute(BundleContext.class.getName(), bundleContext);
    }

    @Test
    public void shouldSetDataSourceAttribute() throws IOException, ServletException {
        String queryString = "path=" + Colors.class.getName() + "&exclude=none,*more";
        context.request().setQueryString(queryString);
        servlet.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);

        List<Resource> options = IteratorUtils.toList(dataSource.iterator());
        assertFalse(options.isEmpty());
        List<String> optionValues = options.stream()
            .map(Resource::getValueMap)
            .map(valueMap -> valueMap.get(CoreConstants.PN_VALUE, String.class))
            .collect(Collectors.toList());
        List<String> expectedValues = Arrays.stream(Colors.values())
            .map(Colors::toString)
            .collect(Collectors.toList());
        assertEquals(expectedValues, optionValues);
    }

    @Test
    public void shouldOutputJsonWhenRequested() throws IOException, ServletException {
        context.request().setQueryString("output=json&exclude=value1,value2&attributeMembers=foo");
        servlet.doGet(context.request(), context.response());

        MockSlingHttpServletResponse response = context.response();
        assertEquals(CoreConstants.CONTENT_TYPE_JSON, response.getContentType());
        assertEquals(
            "[{\"text\":\"None\",\"value\":\"none\"},{\"text\":\"Option 0\",\"value\":\"value0\",\"granite:data\":{\"foo\":\"bar\"}},{\"text\":\"More\",\"value\":\"prefix:more\"}]",
            response.getOutputAsString());
        assertNull(context.request().getAttribute(DataSource.class.getName()));
    }

    @Test
    public void shouldOutputJsonWhenExtensionIsJson() throws IOException, ServletException {
        context.request().setQueryString("path=" + Colors.class.getName() + "&exclude=*FF*");
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setExtension("json");
        servlet.doGet(context.request(), context.response());

        MockSlingHttpServletResponse response = context.response();
        assertEquals(CoreConstants.CONTENT_TYPE_JSON, response.getContentType());
        assertEquals(
            "[{\"text\":\"None\",\"value\":\"none\"},{\"text\":\"Indigo\",\"value\":\"#4B0082\"},{\"text\":\"More\",\"value\":\"prefix:more\"}]",
            response.getOutputAsString());
    }

    @Test
    public void shouldOutputEmptyJsonArrayWhenNoOptions() throws IOException, ServletException {
        context.request().setResource(context.resourceResolver().getResource(TestConstants.ROOT_RESOURCE + "/options"));
        context.request().setQueryString("path=nonexistent.class.Name&output=json");
        servlet.doGet(context.request(), context.response());

        MockSlingHttpServletResponse response = context.response();
        assertEquals(CoreConstants.CONTENT_TYPE_JSON, response.getContentType());
        assertEquals("[]", response.getOutputAsString());
    }
}
