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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderServiceImpl;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionSourceParameters;

public class OptionProviderTest {

    static final String MOCK_DATA = "/com/exadel/aem/toolkit/core/optionprovider/content.json";
    static final String RESOURCE_TYPE_PREFIX = "/apps/";
    static final String VALUE_NONE = "none";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    private OptionProviderService optionProvider;

    @Before
    public void setUp() {
        context.load().json(MOCK_DATA, TestConstants.ROOT_RESOURCE);
        context.request().setResource(context.resourceResolver().getResource(TestConstants.ROOT_RESOURCE));
        optionProvider = context.registerInjectActivateService(new OptionProviderServiceImpl());
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setResourcePath(RESOURCE_TYPE_PREFIX + ResourceTypes.OPTION_PROVIDER);
    }

    @Test
    public void shouldParseParameters() {
        String path1 = "/path/to/first/resource";
        String attributeMember1 = "sling:resourceType";
        String textMember2 = "@id";
        String valueMember2 = "cq:template";

        String queryString = "path1=" + path1
            + "&path2=/path/to/second/resource"
            + "&attributeMembers=" + attributeMember1
            + "&textMember2=" + textMember2
            + "&valueMember2=" + valueMember2
            + "&sorted=true"
            + "&textTransform=uppercase"
            + "&valueTransform1=lowercase"
            + "&prepend=None:none"
            + "&append=More:prefix\\\\:value"
            + "&exclude=some*,*ing"
            + "&attributes=a:value,b:value";

        // This way, we merge parameters from the query string to those from the underlying resource
        context.request().setQueryString(queryString);
        OptionSourceParameters parameters = OptionSourceParameters.forRequest(context.request());

        // Checking the path
        assertEquals("/content/options", parameters.getPathParameters().get(0).getPath());
        assertEquals(path1, parameters.getPathParameters().get(1).getPath());

        // Checking attributes and the "attributeMembers" param
        assertEquals(2, parameters.getPathParameters().get(0).getAttributes().size());
        assertEquals(attributeMember1, parameters.getPathParameters().get(1).getAttributeMembers().get(0));

        // Checking the "textMember" and "valueMember" params
        assertEquals(textMember2, parameters.getPathParameters().get(2).getTextMember());
        assertEquals(valueMember2, parameters.getPathParameters().get(2).getValueMember());

        // Checking the "append" and "prepend" params
        assertEquals(
            VALUE_NONE,
            parameters.getPrependedOptions().get(0).getValue());
        assertEquals(
            "prefix:value",
            parameters.getAppendedOptions().get(0).getValue());

        // Checking "exclude" params
        assertEquals("some*", parameters.getExcludedOptions().get(0));
        assertEquals("*ing",  parameters.getExcludedOptions().get(1));

        // Checking transform params
        assertEquals(StringTransformation.UPPERCASE, parameters.getPathParameters().get(0).getTextTransform());
        assertEquals(StringTransformation.LOWERCASE, parameters.getPathParameters().get(1).getValueTransform());

        // Checking the "sorted" param
        assertTrue(parameters.isSorted());
    }

    @Test
    public void shouldMergePaths() {
        // We merge options from more than one source and implement the path-by-reference facility.
        // The "path" is already specified in the underlying resource, so "path2" adds to it
        String queryString = "path2=/content/optionsPathHolder@moreOptionsPath"
            + "&textMember2=text&exclude=Excluded*,*6";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());

        assertEquals(7, options.size());
        assertEquals("value4", options.get(5).getValueMap().get(CoreConstants.PN_VALUE));
        assertEquals("prefix:more", options.get(options.size() - 1).getValueMap().get(CoreConstants.PN_VALUE));
    }

    @Test
    public void shouldCreateJcrBasedDataSource() {
        String queryString = "path=/content/options&exclude=*more";
        context.request().setQueryString(queryString);
        List<Resource> options = optionProvider.getOptions(context.request());
        assertArrayEquals(
            new String[] {VALUE_NONE, "value0", "value1", "value2"},
            options.stream().map(resource -> resource.getValueMap().get(CoreConstants.PN_VALUE)).toArray());
    }

    @Test
    public void shouldCreateJcrBasedDataSource2() {
        String queryString = "path=/content/tags/colors&exclude=*more";
        context.request().setQueryString(queryString);
        List<Resource> options = optionProvider.getOptions(context.request());
        assertArrayEquals(
            new String[] {VALUE_NONE, "#FF0000", "#00FF00", "#0000FF"},
            options.stream().map(resource -> resource.getValueMap().get(CoreConstants.PN_VALUE)).toArray());
    }

    @Test
    public void shouldIgnoreFallback() {
        String queryString = "path1=/content/tags/colors&fallback1=true&exclude=*more,none";
        context.request().setQueryString(queryString);
        List<Resource> options = optionProvider.getOptions(context.request());
        assertArrayEquals(
            new String[] {"value0", "value1", "value2"},
            options.stream().map(resource -> resource.getValueMap().get(CoreConstants.PN_VALUE)).toArray());
    }
}
