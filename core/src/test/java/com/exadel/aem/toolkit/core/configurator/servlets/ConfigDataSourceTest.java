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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.MetaTypeService;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertNull;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener;

@RunWith(MockitoJUnitRunner.class)
public class ConfigDataSourceTest {

    private static final String PID_SUFFIX = "/test.config.pid";
    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    @Before
    public void setUp() throws PersistenceException {
        MetaTypeService metaTypeService = Mockito.mock(MetaTypeService.class);
        context.registerService(MetaTypeService.class, metaTypeService);

        Resource resource = context.create().resource("/content/test");
        context.resourceResolver().commit();
        context.request().setResource(resource);
    }

    @Test
    public void shouldHandleNoConfigurationSpecified() {
        for (String suffix : new String[]{null, StringUtils.EMPTY, CoreConstants.SEPARATOR_SLASH, "/ "}) {
            context.requestPathInfo().setSuffix(suffix);

            context.registerService(new ConfigChangeListener());
            ConfigDataSource configDataSource = context.registerInjectActivateService(new ConfigDataSource());
            configDataSource.doGet(context.request(), context.response());

            DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
            assertNull(dataSource);
        }
    }

    @Test
    public void shouldHandleDisabledChangeListener() throws IOException {
        setUpBundleContext(StringUtils.stripStart(PID_SUFFIX, CoreConstants.SEPARATOR_SLASH));
        context.requestPathInfo().setSuffix(PID_SUFFIX);

        context.registerService(new ConfigChangeListener());
        ConfigDataSource configDataSource = context.registerInjectActivateService(new ConfigDataSource());

        configDataSource.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNull(dataSource);
    }

    @Test
    public void shouldHandleMissingConfig() throws IOException {
        setUpBundleContext(StringUtils.stripStart(PID_SUFFIX, CoreConstants.SEPARATOR_SLASH));
        context.requestPathInfo().setSuffix(PID_SUFFIX);

        context.registerInjectActivateService(
            new ConfigChangeListener(),
            Collections.singletonMap("enabled", true));
        ConfigDataSource configDataSource = context.registerInjectActivateService(new ConfigDataSource());

        configDataSource.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNull(dataSource);
    }

    private void setUpBundleContext(String configId) throws IOException {
        ConfigurationAdmin mockConfigAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigAdmin.getConfiguration(Mockito.eq(configId), Mockito.isNull())).thenReturn(null);

        MetaTypeService mockMetaTypeService = Mockito.mock(MetaTypeService.class);

        BundleContext mockBundleContext = Mockito.mock(BundleContext.class);
        @SuppressWarnings("unchecked")
        ServiceReference<ConfigurationAdmin> mockConfigAdminRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockBundleContext.getServiceReference(Mockito.eq(ConfigurationAdmin.class))).thenReturn(mockConfigAdminRef);
        Mockito.when(mockBundleContext.getService(Mockito.eq(mockConfigAdminRef))).thenReturn(mockConfigAdmin);

        @SuppressWarnings("unchecked")
        ServiceReference<MetaTypeService> mockMetaTypeServiceRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockBundleContext.getServiceReference(Mockito.eq(MetaTypeService.class))).thenReturn(mockMetaTypeServiceRef);
        Mockito.when(mockBundleContext.getService(Mockito.eq(mockMetaTypeServiceRef))).thenReturn(mockMetaTypeService);

        context.request().setAttribute(BundleContext.class.getName(), mockBundleContext);
    }
}
