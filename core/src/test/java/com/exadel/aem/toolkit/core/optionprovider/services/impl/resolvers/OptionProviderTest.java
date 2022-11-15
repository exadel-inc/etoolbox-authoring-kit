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
package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderServiceImpl;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionSourceParameters;

public class OptionProviderTest {

    private static final String MOCK_DATA_ROOT = "/com/exadel/aem/toolkit/core/optionprovider/";

    private OptionProviderService optionProvider;

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void setUp() {
        context.load().json(MOCK_DATA_ROOT + "content.json", "/content");
        context.request().setResource(context.resourceResolver().getResource("/content"));
        optionProvider = context.registerInjectActivateService(new OptionProviderServiceImpl());
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setResourcePath("/apps/" + ResourceTypes.OPTION_PROVIDER);
    }

    @Test
    public void shouldParseParameters() {
        String path1 = "/path/to/first/resource";
        String attributeMember1 = "sling:resourceType";
        String fallbackPath2 = "/path/to/second/fallbackResource";
        String textMember2 = "@id";
        String valueMember2 = "cq:template";

        String queryString = "path1=" + path1
            + "&path2=/path/to/second/resource"
            + "&attributeMembers=" + attributeMember1
            + "&fallbackPath2=" + fallbackPath2
            + "&textMember2=" + textMember2
            + "&valueMember2=" + valueMember2
            + "&sorted=true"
            + "&textTransform=uppercase"
            + "&valueTransform1=lowercase"
            + "&prepend=None:none"
            + "&append=More:prefix\\\\:value"
            + "&exclude=some*,*ing"
            + "&attributes=a:value,b:value";

        context.request().setQueryString(queryString);  // This way we merge parameters from the query string to those from
                                                        // the underlying resource
        OptionSourceParameters parameters = OptionSourceParameters.forRequest(context.request());
        // Checking paths
        assertEquals("/content/options", parameters.getPathParameters().get(0).getPath());
        assertEquals(path1, parameters.getPathParameters().get(1).getPath());
        assertEquals(fallbackPath2, parameters.getPathParameters().get(2).getFallbackPath());
        // Checking attributes and 'attributeMembers' param
        assertEquals(2, parameters.getPathParameters().get(0).getAttributes().length);
        assertEquals(attributeMember1, parameters.getPathParameters().get(1).getAttributeMembers()[0]);
        // Checking 'textMember' and 'valueMember' params
        assertEquals(textMember2, parameters.getPathParameters().get(2).getTextMember());
        assertEquals(valueMember2, parameters.getPathParameters().get(2).getValueMember());
        // Checking 'append' and 'prepend' params
        assertEquals("none", StringUtils.substringAfter(parameters.getPrependedOptions()[0], ":"));
        assertEquals("prefix\\\\:value", StringUtils.substringAfter(parameters.getAppendedOptions()[0], ":"));
        // Checking 'exclude' params
        assertEquals("some*", parameters.getExcludedOptions()[0]);
        assertEquals("*ing",  parameters.getExcludedOptions()[1]);
        // Checking transform params
        assertEquals(StringTransformation.UPPERCASE, parameters.getPathParameters().get(0).getTextTransform());
        assertEquals(StringTransformation.LOWERCASE, parameters.getPathParameters().get(1).getValueTransform());

        // Checking 'sorted' param
        assertTrue(parameters.isSorted());
    }

    @Test
    public void shouldCreateJcrBasedDataSource() {
        String queryString = "path=/content/options&exclude=*more";
        context.request().setQueryString(queryString);
        List<Resource> options = optionProvider.getOptions(context.request());
        assertArrayEquals(
            new String[] {"none", "value0", "value1", "value2"},
            options.stream().map(resource -> resource.getValueMap().get("value")).toArray());
    }

    @Test
    public void shouldCreateHttpBasedDataSource() throws IOException {
        String queryString = "path=https://acme.com/sample.json&textMember=label&exclude=*more";
        context.request().setQueryString(queryString);

        HttpClient mockHttpClient = getMockHttpClient("httpResponse1.json");
        context.request().setAttribute(OptionSourceResolver.class.getName(), new HttpOptionSourceResolver(mockHttpClient));

        assertArrayEquals(
            new String[] {"none", "1", "2", "3"},
            optionProvider.getOptions(context.request()).stream().map(resource -> resource.getValueMap().get("value")).toArray());
    }

    @Test
    public void shouldCreateHttpBasedDataSourceViaPath() throws IOException {
        String queryString = "path=https://acme.com/sample.json/base/data&textMember=label&exclude=*more";
        context.request().setQueryString(queryString);

        HttpClient mockHttpClient = getMockHttpClient("httpResponse2.json");
        context.request().setAttribute(OptionSourceResolver.class.getName(), new HttpOptionSourceResolver(mockHttpClient));

        assertArrayEquals(
            new String[] {"none", "4", "5", "6"},
            optionProvider.getOptions(context.request()).stream().map(resource -> resource.getValueMap().get("value")).toArray());
    }

    @Test
    public void shouldCreateHttpBasedDataSourceSingleton() throws IOException {
        String queryString = "path=https://acme.com/sample.json/base&textMember=dummy&exclude=*more";
        context.request().setQueryString(queryString);

        HttpClient mockHttpClient = getMockHttpClient("httpResponse2.json");
        context.request().setAttribute(OptionSourceResolver.class.getName(), new HttpOptionSourceResolver(mockHttpClient));

        assertArrayEquals(
            new String[] {"none", StringUtils.EMPTY},
            optionProvider.getOptions(context.request()).stream().map(resource -> resource.getValueMap().get("value")).toArray());
    }

    @Test
    public void shouldMergePaths() {
        String queryString = "path2=/content/optionsPathHolder@moreOptionsPath"  // this way we merge options from more than one source
                + "&textMember2=text&exclude=Excluded*,*6";                      // and implement path-by-reference facility
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());

        assertEquals(7, options.size());
        assertEquals("value4", options.get(5).getValueMap().get("value"));
        assertEquals("prefix:more", options.get(options.size() - 1).getValueMap().get(CoreConstants.PN_VALUE));
    }

    private HttpClient getMockHttpClient(String contentFile) throws IOException {
        String expectedJson = IOUtils.toString(
            Objects.requireNonNull(OptionProviderTest.class.getResourceAsStream(MOCK_DATA_ROOT + contentFile)),
            StandardCharsets.UTF_8);
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse mockHttpResponse = new BasicHttpResponse(new HttpVersion(1, 0), HttpStatus.SC_OK, StringUtils.EMPTY);
        mockHttpResponse.setEntity(new StringEntity(expectedJson, ContentType.APPLICATION_JSON));
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(mockHttpResponse);
        return mockHttpClient;
    }
}
