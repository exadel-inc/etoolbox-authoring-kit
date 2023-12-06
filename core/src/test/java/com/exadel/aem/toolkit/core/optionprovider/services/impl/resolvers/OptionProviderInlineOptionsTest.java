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

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderServiceImpl;

public class OptionProviderInlineOptionsTest {

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    private OptionProviderService optionProvider;

    @Before
    public void setUp() throws ClassNotFoundException {
        context.load().json(OptionProviderTest.MOCK_DATA, TestConstants.ROOT_RESOURCE);
        context.request().setResource(context.resourceResolver().getResource(TestConstants.ROOT_RESOURCE));
        optionProvider = context.registerInjectActivateService(new OptionProviderServiceImpl());
        ((MockRequestPathInfo) context
            .request()
            .getRequestPathInfo())
            .setResourcePath(OptionProviderTest.RESOURCE_TYPE_PREFIX + ResourceTypes.OPTION_PROVIDER);
    }

    @Test
    public void shouldCreateInlineOptionsBasedDataSource() {
        String queryString = "path=[{\"jcr:title\":\"Option 1\", \"value\":1},{\"jcr:title\":\"Option 2\", \"value\":2}]";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);
        assertArrayEquals(
            new String[]{"None", "Option 1", "Option 2", "More"},
            options.stream().map(Resource::getValueMap).map(vm -> vm.get(CoreConstants.PN_TEXT)).toArray());
    }

    @Test
    public void shouldMergeInlineOptionsWithJcrOptions1() {
        String queryString = "path1=[{\"name\":\"Leading 1\",\"val\":1},{\"name\":\"Trailing 2\", \"val\":2}]"
            + "&textMember1=name&valueMember1=val&sorted=true";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);
        assertArrayEquals(
            new String[] {"None", "Leading 1", "Option 0", "Option 1", "Option 2", "Trailing 2", "More"},
            options.stream().map(Resource::getValueMap).map(vm -> vm.get(CoreConstants.PN_TEXT)).toArray());
    }

    @Test
    public void shouldMergeInlineOptionsWithJcrOptions2() {
        context.request().setResource(context.resourceResolver().getResource(TestConstants.ROOT_RESOURCE + "/otherDatasource"));

        String queryString = "path1=[{\"jcr:title\":\"Leading 1\",\"value\":1},{\"jcr:title\":\"Trailing 2\", \"value\":2}]"
            + "&textMember1=jcr:title&valueMember1=value&sorted=true";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);
        assertArrayEquals(
            new String[] {"Leading 1", "Other option 1", "Other option 2", "Trailing 2"},
            options.stream().map(Resource::getValueMap).map(vm -> vm.get(CoreConstants.PN_TEXT)).toArray());
        assertArrayEquals(
            new String[] {null, "one", "two", null},
            options
                .stream()
                .map(resource -> resource.getChild(CoreConstants.NN_GRANITE_DATA))
                .map(resource -> resource != null ? resource.getValueMap().get("attr", String.class) : null)
                .toArray());
    }

    @Test
    public void shouldAddInlineOptionsAsFallback() {
        String queryString = "path=/content/nonexistent"
            + "&path1=[{\"jcr:title\":\"Option 1\", \"value\":1},{\"jcr:title\":\"Option 2\", \"value\":2}]"
            + "&fallback1=true";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);
        assertArrayEquals(
            new String[]{"None", "Option 1", "Option 2", "More"},
            options.stream().map(Resource::getValueMap).map(vm -> vm.get(CoreConstants.PN_TEXT)).toArray());
    }
}
