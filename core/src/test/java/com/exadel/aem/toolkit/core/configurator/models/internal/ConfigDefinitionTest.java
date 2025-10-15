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
package com.exadel.aem.toolkit.core.configurator.models.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import com.adobe.granite.ui.components.ExpressionResolver;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

@RunWith(MockitoJUnitRunner.class)
public class ConfigDefinitionTest {

    private static final String TEST_PID = "com.example.test.config";
    private static final String FACTORY_PID = "com.example.factory.config";
    private static final String FACTORY_INSTANCE_PID = "com.example.factory.config~instance1";
    private static final String CONFIG_NAME = "Test Configuration";
    private static final String CONFIG_DESCRIPTION = "Test configuration description";

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    @Mock
    private ConfigurationAdmin configurationAdmin;

    @Mock
    private MetaTypeService metaTypeService;

    @Mock
    private MetaTypeInformation metaTypeInformation;

    @Mock
    private ObjectClassDefinition ocd;

    @Mock
    private Configuration configuration;

    @Before
    public void setUp() {
        context.registerService(ConfigurationAdmin.class, configurationAdmin);
        context.registerService(MetaTypeService.class, metaTypeService);
        context.registerService(ExpressionResolver.class, Mockito.mock(ExpressionResolver.class));
    }

    @Test
    public void shouldCreateConfigDefinitionFromRequest() throws Exception {
        setUpMocksForConfig();
        long expectedChangeCount = 42L;
        Mockito.when(configuration.getChangeCount()).thenReturn(expectedChangeCount);

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(TEST_PID, result.getPid());
        assertEquals(CONFIG_NAME, result.getName());
        assertEquals(CONFIG_DESCRIPTION, result.getDescription());
        assertEquals(CONFIG_NAME, result.getTitle());
        assertFalse(result.isFactory());
        assertFalse(result.isFactoryInstance());
        assertFalse(result.isModified());
        assertFalse(result.isPublished());
        assertNotNull(result.getAction());
        assertEquals(expectedChangeCount, result.getChangeCount());
        assertEquals(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID, result.getAction());
    }

    @Test
    public void shouldCreateEmptyConfigDefinitionWhenRequestIsNull() {
        ConfigDefinition result = ConfigDefinition.from(null);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertNull(result.getAction());
        assertNull(result.getPid());

        MockSlingHttpServletRequest request = context.request();
        ((MockRequestPathInfo) request.getRequestPathInfo()).setSuffix(null);
        result = ConfigDefinition.from(request);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertNull(result.getPid());
    }

    @Test
    public void shouldHandleNonExistentConfiguration() throws Exception {
        setUpMocksForConfig();

        Mockito.when(configurationAdmin.getConfiguration(Mockito.eq(TEST_PID), Mockito.isNull())).thenReturn(null);

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Test
    public void shouldDetectFactoryConfiguration() throws Exception {
        setUpMocksForConfig(true);

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + FACTORY_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(FACTORY_PID, result.getPid());
        assertTrue(result.isFactory());
        assertFalse(result.isFactoryInstance());
    }

    @Test
    public void shouldDetectFactoryInstanceConfiguration() throws Exception {
        setUpMocksForFactoryInstanceConfig();

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + FACTORY_INSTANCE_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(FACTORY_INSTANCE_PID, result.getPid());
        assertFalse(result.isFactory());
        assertTrue(result.isFactoryInstance());
    }

    @Test
    public void shouldDetectModifiedConfiguration() throws Exception {
        setUpMocksForConfig();

        createConfigurationInRepository(false);

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertTrue(result.isModified());
        assertFalse(result.isPublished());
    }

    @Test
    public void shouldDetectPublishedConfiguration() throws Exception {
        setUpMocksForConfig();

        createConfigurationInRepository(true);

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertTrue(result.isPublished());
    }

    @Test
    public void shouldGenerateCleanupUrl() throws Exception {
        setUpMocksForConfig();

        Privilege read = Mockito.mock(Privilege.class);
        Privilege write = Mockito.mock(Privilege.class);
        Privilege[] rootPrivileges = new Privilege[]{read};
        Privilege[] configPrivileges = new Privilege[]{read, write};

        AccessControlManager mockAcm = Mockito.mock(AccessControlManager.class);
        Mockito.when(mockAcm.getPrivileges(Mockito.eq(ConfiguratorConstants.ROOT_PATH))).thenReturn(rootPrivileges);
        Mockito
            .when(mockAcm.getPrivileges(Mockito.eq(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID)))
            .thenReturn(configPrivileges);

        ResourceResolver mockResolver = Mockito.mock(ResourceResolver.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockResolver.adaptTo(Mockito.eq(Session.class))).thenReturn(mockSession);
        Mockito.when(mockSession.getAccessControlManager()).thenReturn(mockAcm);

        SlingHttpServletRequest mockRequest = new MockSlingHttpServletRequest(mockResolver, context.bundleContext());
        mockRequest.setAttribute(BundleContext.class.getName(), context.request().getAttribute(BundleContext.class.getName()));
        ((MockRequestPathInfo) mockRequest.getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result = ConfigDefinition.from(mockRequest);

        assertNotNull(result);
        assertNotNull(result.getCleanupAction());
        assertTrue(result.getCleanupAction().endsWith("/data"));
    }

    @Test
    public void shouldPopulateAttributeValues() throws Exception {
        setUpMocksForConfig();

        AttributeDefinition attr1 = Mockito.mock(AttributeDefinition.class);
        Mockito.when(attr1.getID()).thenReturn("property1");
        AttributeDefinition attr2 = Mockito.mock(AttributeDefinition.class);
        Mockito.when(attr2.getID()).thenReturn("property2");
        AttributeDefinition attr3 = Mockito.mock(AttributeDefinition.class);
        Mockito.when(attr3.getID()).thenReturn("property3");
        Mockito.when(ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)).thenReturn(new AttributeDefinition[]{attr1, attr2, attr3});

        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("property1", "value1");
        properties.put("property2", 42);
        properties.put("property3", true);
        Mockito.when(configuration.getProperties()).thenReturn(properties);

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertNotNull(result.getAttributes());
        assertEquals(3, result.getAttributes().size());
        assertEquals("value1", result.getAttributes().get(0).getValue());
    }

    @Test
    public void shouldReturnTitleAsPidWhenNameIsEmpty() throws Exception {
        setUpMocksForConfig();

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        Mockito.when(ocd.getName()).thenReturn(null);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertEquals(TEST_PID, result.getTitle());

        Mockito.when(ocd.getName()).thenReturn(StringUtils.EMPTY);
        result = ConfigDefinition.from(context.request());
        assertNotNull(result);
        assertEquals(TEST_PID, result.getTitle());
    }

    @Test
    public void shouldHandleConfigurationAdminException() throws Exception {
        setUpMocksForConfig();

        Mockito.when(configurationAdmin.getConfiguration(Mockito.eq(TEST_PID), Mockito.isNull()))
            .thenThrow(new RuntimeException("NOT AN EXCEPTION: testing ConfigDefinition logic"));

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result = ConfigDefinition.from(context.request());

        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Test
    public void shouldCacheConfigDefinitionInRequest() throws Exception {
        setUpMocksForConfig();

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ConfigDefinition result1 = ConfigDefinition.from(context.request());
        ConfigDefinition result2 = ConfigDefinition.from(context.request());

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1, result2);
    }

    @Test
    public void shouldManageChildConfigurations() throws Exception {
        setUpMocksForConfig(true);

        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + FACTORY_PID);
        ConfigDefinition parent = ConfigDefinition.from(context.request());

        ConfigDefinition child1 = ConfigDefinition.from("com.example.factory~zzz", FACTORY_PID, ocd, false);
        ConfigDefinition child2 = ConfigDefinition.from("com.example.factory~aaa", FACTORY_PID, ocd, false);
        ConfigDefinition child3 = ConfigDefinition.from("com.example.factory~mmm", FACTORY_PID, ocd, false);

        parent.addChild(child1);
        parent.addChild(child2);
        parent.addChild(child3);

        assertNotNull(parent.getChildren());
        assertEquals(3, parent.getChildren().size());
        assertEquals("com.example.factory~aaa", parent.getChildren().get(0).getPid());
        assertEquals("com.example.factory~mmm", parent.getChildren().get(1).getPid());
        assertEquals("com.example.factory~zzz", parent.getChildren().get(2).getPid());
    }

    /* ---------------
       Utility methods
       --------------- */

    private void setUpMocksForConfig() throws Exception {
        setUpMocksForConfig(false);
    }

    private void setUpMocksForConfig(boolean isFactory) throws Exception {
        setUpBundleContext();

        Mockito.when(ocd.getName()).thenReturn(CONFIG_NAME);
        Mockito.when(ocd.getDescription()).thenReturn(CONFIG_DESCRIPTION);
        Mockito.when(ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)).thenReturn(new AttributeDefinition[0]);

        String pid = isFactory ? FACTORY_PID : TEST_PID;

        Mockito.when(configuration.getPid()).thenReturn(pid);
        Mockito.when(configuration.getFactoryPid()).thenReturn(null);
        Mockito.when(configuration.getChangeCount()).thenReturn(0L);
        Mockito.when(configuration.getProperties()).thenReturn(new Hashtable<>());
        Mockito.when(configurationAdmin.getConfiguration(Mockito.eq(pid), Mockito.isNull())).thenReturn(configuration);

        Mockito.when(metaTypeInformation.getObjectClassDefinition(Mockito.eq(pid), Mockito.isNull())).thenReturn(ocd);
        Mockito.when(metaTypeInformation.getFactoryPids()).thenReturn(isFactory ? new String[] {pid} : new String[0]);
        Mockito.when(metaTypeService.getMetaTypeInformation(Mockito.any(Bundle.class))).thenReturn(metaTypeInformation);
    }

    private void setUpMocksForFactoryInstanceConfig() throws Exception {
        setUpBundleContext();

        Mockito.when(ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)).thenReturn(new AttributeDefinition[0]);

        Mockito.when(configuration.getPid()).thenReturn(FACTORY_INSTANCE_PID);
        Mockito.when(configuration.getFactoryPid()).thenReturn(FACTORY_PID);
        Mockito.when(configuration.getChangeCount()).thenReturn(0L);
        Mockito.when(configuration.getProperties()).thenReturn(new Hashtable<>());
        Mockito.when(configurationAdmin.getConfiguration(Mockito.eq(FACTORY_INSTANCE_PID), Mockito.isNull())).thenReturn(configuration);

        Mockito.when(metaTypeInformation.getObjectClassDefinition(Mockito.eq(FACTORY_PID), Mockito.isNull())).thenReturn(ocd);
        Mockito.when(metaTypeInformation.getFactoryPids()).thenReturn(new String[]{FACTORY_PID});
        Mockito.when(metaTypeService.getMetaTypeInformation(Mockito.any(Bundle.class))).thenReturn(metaTypeInformation);
    }

    @SuppressWarnings("unchecked")
    private void setUpBundleContext() {
        BundleContext mockBundleContext = Mockito.mock(BundleContext.class);

        ServiceReference<ConfigurationAdmin> mockConfigAdminRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockBundleContext.getServiceReference(Mockito.eq(ConfigurationAdmin.class))).thenReturn(mockConfigAdminRef);
        Mockito.when(mockBundleContext.getService(mockConfigAdminRef)).thenReturn(configurationAdmin);

        ServiceReference<MetaTypeService> mockMetaTypeServiceRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockBundleContext.getServiceReference(Mockito.eq(MetaTypeService.class))).thenReturn(mockMetaTypeServiceRef);
        Mockito.when(mockBundleContext.getService(mockMetaTypeServiceRef)).thenReturn(metaTypeService);

        Mockito.when(mockBundleContext.getBundles()).thenReturn(new Bundle[]{Mockito.mock(Bundle.class)});

        context.request().setAttribute(BundleContext.class.getName(), mockBundleContext);
    }

    private void createConfigurationInRepository(boolean published) throws PersistenceException {
        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        context.create().resource(configPath + CoreConstants.SEPARATOR_SLASH + ConfiguratorConstants.NN_DATA);
        if (published) {
            context
                .build()
                .resource(configPath)
                .siblingsMode()
                .resource(configPath, ConfiguratorConstants.PN_REPLICATION_ACTION, "Activate")
                .commit();
        }
        context.resourceResolver().commit();
    }
}

