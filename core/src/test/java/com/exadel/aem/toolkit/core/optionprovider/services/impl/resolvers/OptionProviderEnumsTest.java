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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.injectors.models.enums.Colors;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderServiceImpl;

public class OptionProviderEnumsTest {

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

        Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.doReturn(Colors.class).when(bundle).loadClass(Colors.class.getName());
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        Mockito.when(bundleContext.getBundle()).thenReturn(bundle);
        context.request().setAttribute(BundleContext.class.getName(), bundleContext);
    }

    @Test
    public void shouldCreateEnumBasedDataSource() {
        String queryString = "path=" + Colors.class.getName() + "&exclude=none,*more";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);

        assertTrue(CollectionUtils.isEqualCollection(
            Arrays.stream(Colors.values()).map(Colors::toString).collect(Collectors.toList()),
            options
                .stream()
                .map(Resource::getValueMap)
                .map(valueMap -> valueMap.get(CoreConstants.PN_VALUE, String.class))
                .collect(Collectors.toList())));
    }

    @Test
    public void shouldCreateEnumBasedDataSourceOverrideMembers() {
        String queryString = "path=" + Colors.class.getName() + "&exclude=none,*more"
            + "&textMember=value&valueMember=hexValue&textTransform=none&attributeMembers=getIntValue";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);

        assertTrue(CollectionUtils.isEqualCollection(
            Arrays.stream(Colors.values()).map(Objects::toString).collect(Collectors.toList()),
            options
                .stream()
                .map(Resource::getValueMap)
                .map(valueMap -> valueMap.get(CoreConstants.PN_TEXT, String.class))
                .collect(Collectors.toList())));

        assertTrue(CollectionUtils.isEqualCollection(
            Arrays.stream(Colors.values()).map(color -> color.hexValue).collect(Collectors.toList()),
            options
                .stream()
                .map(Resource::getValueMap)
                .map(valueMap -> valueMap.get(CoreConstants.PN_VALUE, String.class))
                .collect(Collectors.toList())));

        assertTrue(CollectionUtils.isEqualCollection(
            Arrays.stream(Colors.values()).map(Colors::getIntValue).collect(Collectors.toList()),
            options
                .stream()
                .map(resource -> resource.getChild(CoreConstants.NN_GRANITE_DATA))
                .filter(Objects::nonNull)
                .map(Resource::getValueMap)
                .map(valueMap -> valueMap.get("getIntValue", Integer.class))
                .collect(Collectors.toList())));
    }
}
