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

import java.util.Collections;
import java.util.Hashtable;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
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
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;
import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener;

@RunWith(MockitoJUnitRunner.class)
public class RenderConditionTest {

    private static final String TEST_PID = "com.example.test.config";
    private static final String FACTORY_PID = "com.example.factory.config";

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.RESOURCERESOLVER_MOCK);

    @Mock
    private ConfigurationAdmin configurationAdmin;

    @Mock
    private MetaTypeService metaTypeService;

    @Mock
    private ConfigChangeListener configChangeListener;

    @Before
    public void setUp() {
        context.registerService(ConfigurationAdmin.class, configurationAdmin);
        context.registerService(MetaTypeService.class, metaTypeService);
        context.registerService(ExpressionResolver.class, Mockito.mock(ExpressionResolver.class));
        setUpBundleContext();
    }

    @Test
    public void shouldHandleCanBrowseFeature() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, false);
        context.request().setPathInfo("/libs/etoolbox/config/test");
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.canBrowse");

        assertRenderCondition(true);
    }

    @Test
    public void shouldAllowBrowseWithInvalidConfigAccess() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, false);

        context.request().setPathInfo("/libs/etoolbox/config/test");
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + "invalid");

        createAndInvokeRenderCondition("eak.configurator.canBrowse");

        assertRenderCondition(true);
    }

    @Test
    public void shouldDenyBrowseWhenNotConfigPath() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, false);
        context.request().setPathInfo("/apps/other/path");
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.canBrowse");

        assertRenderCondition(false);
    }

    @Test
    public void shouldHandleCanModifyFeature() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, true);
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.canModify");

        assertRenderCondition(true);
    }

    @Test
    public void shouldDenyModifyWithoutPermission() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(false, false, false);
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.canModify");

        assertRenderCondition(false);
    }

    @Test
    public void shouldDenyModifyForFactoryConfig() throws Exception {
        setUpMocksForConfig(true);
        setUpPermissions(true, true, false);
        setUpBundleContext();

        createAndInvokeRenderCondition("eak.configurator.canModify");

        assertRenderCondition(false);
    }

    @Test
    public void shouldHandleCanReplicateFeature() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, true);
        context.request().setPathInfo("/libs/etoolbox/config/test");
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        context.create().resource(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.canReplicate");

        assertRenderCondition(true);
    }

    @Test
    public void shouldDenyReplicateForLocalSettings() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, true);
        context.request().setPathInfo("/libs/etoolbox/localsettings/test");
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.canReplicate");

        assertRenderCondition(false);
    }

    @Test
    public void shouldDenyReplicateWithoutPermission() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, false);
        context.request().setPathInfo("/libs/etoolbox/config/test");
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);
        context.create().resource(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.canReplicate");

        assertRenderCondition(false);
    }

    @Test
    public void shouldHandleShowFormFeature() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, true);
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.showForm");

        assertRenderCondition(true);
    }

    @Test
    public void shouldHideFormWithoutConfigPid() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, true);
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(null);

        createAndInvokeRenderCondition("eak.configurator.showForm");

        assertRenderCondition(false);
    }

    @Test
    public void shouldHandleShowListFeature() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, true);
        context.request().setPathInfo("/etoolbox/config.html");

        createAndInvokeRenderCondition("eak.configurator.showList");

        assertRenderCondition(true);
    }

    @Test
    public void shouldHideListWithConfigPid() throws Exception {
        setUpMocksForConfig();
        setUpPermissions(true, true, false);
        ((MockRequestPathInfo) context.request().getRequestPathInfo()).setSuffix(CoreConstants.SEPARATOR_SLASH + TEST_PID);

        createAndInvokeRenderCondition("eak.configurator.showList");

        assertRenderCondition(false);
    }

    /* ---------------
       Utility methods
       --------------- */

    @SuppressWarnings("unchecked")
    private void setUpBundleContext() {
        BundleContext mockBundleContext = Mockito.mock(BundleContext.class);

        ServiceReference<ConfigurationAdmin> mockConfigAdminRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockBundleContext.getServiceReference(Mockito.eq(ConfigurationAdmin.class))).thenReturn(mockConfigAdminRef);
        Mockito.when(mockBundleContext.getService(mockConfigAdminRef)).thenReturn(configurationAdmin);

        ServiceReference<MetaTypeService> mockMetaTypeServiceRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockBundleContext.getServiceReference(Mockito.eq(MetaTypeService.class))).thenReturn(mockMetaTypeServiceRef);
        Mockito.when(mockBundleContext.getService(mockMetaTypeServiceRef)).thenReturn(metaTypeService);

        ServiceReference<ConfigChangeListener> mockConfigChangeListenerRef = Mockito.mock(ServiceReference.class);
        Mockito.when(mockBundleContext.getServiceReference(Mockito.eq(ConfigChangeListener.class))).thenReturn(mockConfigChangeListenerRef);
        Mockito.when(mockBundleContext.getService(mockConfigChangeListenerRef)).thenReturn(configChangeListener);
        Mockito.when(configChangeListener.isEnabled()).thenReturn(true);

        Mockito.when(mockBundleContext.getBundles()).thenReturn(new Bundle[]{Mockito.mock(Bundle.class)});

        context.request().setAttribute(BundleContext.class.getName(), mockBundleContext);
    }

    private void setUpMocksForConfig() throws Exception {
        setUpMocksForConfig(false);
    }

    private void setUpMocksForConfig(boolean isFactory) throws Exception {
        ObjectClassDefinition ocd = Mockito.mock(ObjectClassDefinition.class);
        Mockito.when(ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)).thenReturn(new AttributeDefinition[0]);

        String pid = isFactory ? FACTORY_PID : TEST_PID;

        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getPid()).thenReturn(pid);
        Mockito.when(configuration.getFactoryPid()).thenReturn(null);
        Mockito.when(configuration.getChangeCount()).thenReturn(0L);
        Mockito.when(configuration.getProperties()).thenReturn(new Hashtable<>());
        Mockito.when(configurationAdmin.getConfiguration(Mockito.eq(pid), Mockito.isNull())).thenReturn(configuration);

        MetaTypeInformation metaTypeInformation = Mockito.mock(MetaTypeInformation.class);
        Mockito.when(metaTypeInformation.getObjectClassDefinition(Mockito.eq(pid), Mockito.isNull())).thenReturn(ocd);
        Mockito.when(metaTypeInformation.getFactoryPids()).thenReturn(isFactory ? new String[]{pid} : new String[0]);
        Mockito.when(metaTypeService.getMetaTypeInformation(Mockito.any(Bundle.class))).thenReturn(metaTypeInformation);
    }

    private void setUpPermissions(boolean global, boolean local, boolean replication) throws Exception {
        Privilege read = Mockito.mock(Privilege.class);
        Privilege write = Mockito.mock(Privilege.class);
        Privilege replicate = Mockito.mock(Privilege.class);
        Mockito.when(replicate.getName()).thenReturn("crx:replicate");

        Privilege[] globalPrivileges = replication ? new Privilege[]{read, replicate} : new Privilege[]{read};
        Privilege[] localPrivileges = replication ? new Privilege[]{read, write, replicate} : new Privilege[]{read, write};

        AccessControlManager mockAcm = Mockito.mock(AccessControlManager.class);
        Mockito.when(mockAcm.getPrivileges(Mockito.eq(ConfiguratorConstants.ROOT_PATH))).thenReturn(globalPrivileges);
        Mockito
            .when(mockAcm.getPrivileges(Mockito.eq(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID)))
            .thenReturn(localPrivileges);

        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockSession.getAccessControlManager()).thenReturn(mockAcm);
        Mockito.when(mockSession.hasPermission(
                Mockito.eq(ConfiguratorConstants.ROOT_PATH),
                Mockito.eq(Session.ACTION_SET_PROPERTY)))
            .thenReturn(global);
        Mockito.when(mockSession.hasPermission(
                Mockito.eq(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID),
                Mockito.eq(Session.ACTION_SET_PROPERTY)))
            .thenReturn(local);
        Mockito.when(mockSession.hasPermission(
                Mockito.eq(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + "invalid"),
                Mockito.eq(Session.ACTION_SET_PROPERTY)))
            .thenThrow(new PathNotFoundException());

        context.registerAdapter(ResourceResolver.class, Session.class, mockSession);
    }

    private void createAndInvokeRenderCondition(String feature) {
        ValueMap valueMap = new ValueMapDecorator(Collections.singletonMap("feature", feature));
        Resource resource = new ValueMapResource(
            context.resourceResolver(),
            StringUtils.EMPTY,
            "etoolbox-authoring-kit/configurator/components/rendercondition",
            valueMap);
        context.request().setResource(resource);
        RenderCondition model = context.request().adaptTo(RenderCondition.class);
    }

    private void assertRenderCondition(boolean expected) {
        Object renderConditionObj = context.request().getAttribute(com.adobe.granite.ui.components.rendercondition.RenderCondition.class.getName());
        assertNotNull(renderConditionObj);
        assertTrue(renderConditionObj instanceof SimpleRenderCondition);
        assertEquals(expected, ((SimpleRenderCondition) renderConditionObj).check());
    }
}

