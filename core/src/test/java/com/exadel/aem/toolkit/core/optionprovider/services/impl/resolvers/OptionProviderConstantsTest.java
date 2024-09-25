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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.injectors.models.enums.ColorConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.OptionProviderService;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderServiceImpl;

public class OptionProviderConstantsTest {

    private static final String EXCLUSION_PARAM = "&exclude=none,*more";

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
        Mockito.doReturn(ColorConstants.class).when(bundle).loadClass(ColorConstants.class.getName());
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        Mockito.when(bundleContext.getBundle()).thenReturn(bundle);
        context.request().setAttribute(BundleContext.class.getName(), bundleContext);
    }

    @Test
    public void shouldCreateConstantsBasedDataSource1() {
        String queryString = "path=" + ColorConstants.class.getName() + EXCLUSION_PARAM;
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);
        assertEquals(
            Arrays.stream(ColorConstants.class.getDeclaredFields())
                .filter(field -> !Modifier.isPrivate(field.getModifiers()))
                .count(),
            options.size());
    }

    @Test
    public void shouldCreateConstantsBasedDataSource2() {
        String queryString = "path=" + ColorConstants.class.getName() + EXCLUSION_PARAM
            + "&textMember=LABEL_*&valueMember=VALUE_*";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);
        assertArrayEquals(
            new String[] {"Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"},
            options.stream().map(Resource::getValueMap).map(vm -> vm.get(CoreConstants.PN_TEXT)).toArray());
    }

    @Test
    public void shouldCreateConstantsBasedDataSource3() {
        String queryString = "path=" + ColorConstants.class.getName() + EXCLUSION_PARAM
            + "&textMember=*_LABEL&valueMember=*_VALUE";
        context.request().setQueryString(queryString);

        List<Resource> options = optionProvider.getOptions(context.request());
        assertNotNull(options);
        assertArrayEquals(
            new String[] {"White", "Black"},
            options.stream().map(Resource::getValueMap).map(vm -> vm.get(CoreConstants.PN_TEXT)).toArray());
    }
}
