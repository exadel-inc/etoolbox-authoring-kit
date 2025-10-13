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
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.MetaTypeService;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import junitx.util.PrivateAccessor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener;

@RunWith(MockitoJUnitRunner.class)
public class ConfigDataSourceTest {

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
    @SuppressWarnings("unchecked")
    public void shouldHandleNoConfigurationSpecified() throws IOException {
        for (String suffix : new String[]{null, StringUtils.EMPTY, CoreConstants.SEPARATOR_SLASH, "/ "}) {
            context.requestPathInfo().setSuffix(suffix);

            context.registerService(new ConfigChangeListener());
            ConfigDataSource configDataSource = context.registerInjectActivateService(new ConfigDataSource());
            configDataSource.doGet(context.request(), context.response());

            DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
            assertNotNull(dataSource);

            List<Resource> resources = (List<Resource>) IteratorUtils.toList(dataSource.iterator());
            assertEquals(1, resources.size());

            Resource alertResource = resources.get(0);
            ValueMap properties = alertResource.getValueMap();
            assertTrue(properties.get("text", StringUtils.EMPTY).contains("No configuration specified"));
            assertEquals("error", properties.get("variant", String.class));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleDisabledChangeListener() throws IOException {
        context.requestPathInfo().setSuffix("/test.config.pid");

        context.registerService(new ConfigChangeListener());
        ConfigDataSource configDataSource = context.registerInjectActivateService(new ConfigDataSource());
        configDataSource.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);

        List<Resource> resources = (List<Resource>) IteratorUtils.toList(dataSource.iterator());
        assertEquals(1, resources.size());

        Resource alertResource = resources.get(0);
        ValueMap properties = alertResource.getValueMap();
        assertTrue(properties.get("text", StringUtils.EMPTY).contains("This tool is disabled by OSGi configuration"));
        assertEquals("error", properties.get("variant", String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldHandleMissingConfig() throws IOException, NoSuchFieldException {
        String configId = "test.config.pid";
        context.requestPathInfo().setSuffix(CoreConstants.SEPARATOR_SLASH + configId);

        ConfigurationAdmin mockConfigAdmin = Mockito.mock(ConfigurationAdmin.class);

        context.registerInjectActivateService(
            new ConfigChangeListener(),
            Collections.singletonMap("enabled", true));
        ConfigDataSource configDataSource = context.registerInjectActivateService(new ConfigDataSource());
        PrivateAccessor.setField(configDataSource, "configurationAdmin", mockConfigAdmin);
        configDataSource.doGet(context.request(), context.response());

        DataSource dataSource = (DataSource) context.request().getAttribute(DataSource.class.getName());
        assertNotNull(dataSource);

        List<Resource> resources = (List<Resource>) IteratorUtils.toList(dataSource.iterator());
        assertEquals(1, resources.size());

        Resource alertResource = resources.get(0);
        ValueMap properties = alertResource.getValueMap();
        assertTrue(properties.get("text", StringUtils.EMPTY).contains("Configuration \"" + configId + "\" is missing or invalid"));
        assertEquals("error", properties.get("variant", String.class));
    }
}
