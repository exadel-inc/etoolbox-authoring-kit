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
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderServiceImpl;

public class OptionProviderHttpTest {

    @Rule
    public final AemContext context = new AemContext();

    private OptionProviderService optionProvider;

    @Before
    public void setUp() {
        context.load().json(OptionProviderTest.MOCK_DATA, TestConstants.ROOT_RESOURCE);
        context.request().setResource(context.resourceResolver().getResource(TestConstants.ROOT_RESOURCE));
        optionProvider = context.registerInjectActivateService(new OptionProviderServiceImpl());
        ((MockRequestPathInfo) context
            .request()
            .getRequestPathInfo())
            .setResourcePath(OptionProviderTest.RESOURCE_TYPE_PREFIX + ResourceTypes.OPTION_PROVIDER);
    }

    @Test
    public void shouldCreateHttpBasedDataSource() throws IOException {
        // (Here and below) we merge options defined in both the request string and the underlying resource.
        // In particular, the "none" value came from the underlying resource
        String queryString = "path=https://acme.com/sample.json&textMember=label&exclude=*more";
        context.request().setQueryString(queryString);

        HttpClient mockHttpClient = getMockHttpClient("httpResponse1.json");
        context.request().setAttribute(OptionSourceResolver.class.getName(), new HttpOptionSourceResolver(mockHttpClient));

        assertArrayEquals(
            new String[] {OptionProviderTest.VALUE_NONE, "1", "2", "3"},
            optionProvider.getOptions(context.request())
                .stream().map(resource -> resource.getValueMap().get(CoreConstants.PN_VALUE))
                .toArray());
    }

    @Test
    public void shouldCreateHttpBasedDataSourceWithInternalPath() throws IOException {
        String queryString = "path=https://acme.com/sample.json/base/data&textMember=label&exclude=*more";
        context.request().setQueryString(queryString);

        HttpClient mockHttpClient = getMockHttpClient("httpResponse2.json");
        context.request().setAttribute(OptionSourceResolver.class.getName(), new HttpOptionSourceResolver(mockHttpClient));

        assertArrayEquals(
            new String[] {OptionProviderTest.VALUE_NONE, "4", "5", "6"},
            optionProvider.getOptions(context.request())
                .stream()
                .map(resource -> resource.getValueMap().get(CoreConstants.PN_VALUE))
                .toArray());
    }

    @Test
    public void shouldCreateHttpBasedDataSourceWithMissingPath() throws IOException {
        String queryString = "path=https://acme.com/sample.json/base&textMember=dummy&exclude=*more";
        context.request().setQueryString(queryString);

        HttpClient mockHttpClient = getMockHttpClient("httpResponse2.json");
        context.request().setAttribute(OptionSourceResolver.class.getName(), new HttpOptionSourceResolver(mockHttpClient));

        assertArrayEquals(
            new String[] {OptionProviderTest.VALUE_NONE},
            optionProvider.getOptions(context.request())
                .stream()
                .map(resource -> resource.getValueMap().get(CoreConstants.PN_VALUE))
                .toArray());
    }

    /* ---------------
       Service methods
       --------------- */

    private static HttpClient getMockHttpClient(String contentFile) throws IOException {
        String mockDataPath = StringUtils.substringBeforeLast(OptionProviderTest.MOCK_DATA, CoreConstants.SEPARATOR_SLASH)
            + CoreConstants.SEPARATOR_SLASH
            + contentFile;
        String expectedJson = IOUtils.toString(
            Objects.requireNonNull(OptionProviderHttpTest.class.getResourceAsStream(mockDataPath)),
            StandardCharsets.UTF_8);
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        HttpResponse mockHttpResponse = new BasicHttpResponse(new HttpVersion(1, 0), HttpStatus.SC_OK, StringUtils.EMPTY);
        mockHttpResponse.setEntity(new StringEntity(expectedJson, ContentType.APPLICATION_JSON));
        Mockito.when(mockHttpClient.execute(Mockito.any())).thenReturn(mockHttpResponse);
        return mockHttpClient;
    }
}
