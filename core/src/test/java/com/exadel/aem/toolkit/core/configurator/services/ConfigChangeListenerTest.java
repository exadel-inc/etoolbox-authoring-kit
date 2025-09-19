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
package com.exadel.aem.toolkit.core.configurator.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.jcr.resource.internal.JcrResourceChange;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import io.wcm.testing.mock.aem.junit.AemContext;
import junitx.util.PrivateAccessor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

@RunWith(MockitoJUnitRunner.class)
public class ConfigChangeListenerTest {

    private static final String FIELD_CONFIG_ADMIN = "configurationAdmin";
    private static final String FIELD_RESOURCE_RESOLVER_FACTORY = "resourceResolverFactory";

    private static final String NODE_DATA = "/data";
    private static final String NODE_INITIAL = "/initial/";

    private static final Map<String, Object> CONFIG_ENABLED = Collections.singletonMap("enabled", true);

    private static final String TEST_PID = "com.example.test.Config";

    @Rule
    public AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldProcessEnabledProperty() throws Exception {
        ConfigChangeListener configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            CONFIG_ENABLED);
        assertNotNull(PrivateAccessor.getField(configChangeListener, "registration"));

        configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            Collections.singletonMap("enabled", false));
        assertNull(PrivateAccessor.getField(configChangeListener, "registration"));
    }

    @Test
    public void shouldCleanUpConfigurationsOnActivate() throws PersistenceException {
        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        String backupPath = ConfiguratorConstants.ROOT_PATH + NODE_INITIAL + TEST_PID;

        context.create().resource(configPath);
        context.create().resource(backupPath);
        context.resourceResolver().commit();

        assertNotNull(context.resourceResolver().getResource(configPath));
        assertNotNull(context.resourceResolver().getResource(backupPath));

        Map<String, Object> configProps = new HashMap<>();
        configProps.put("enabled", true);
        configProps.put("cleanUp", new String[] {TEST_PID, "com.example.test.AnotherConfig"});
        context.registerInjectActivateService(new ConfigChangeListener(), configProps);

        assertNull(context.resourceResolver().getResource(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID));
        assertNull(context.resourceResolver().getResource(ConfiguratorConstants.ROOT_PATH + NODE_INITIAL + TEST_PID));
    }

    @Test
    public void shouldUpdateConfigurationUponActivate() throws IOException, NoSuchFieldException {
        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        context.create().resource(configPath);

        String dataPath = configPath + NODE_DATA;
        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "test.value");
        props.put("test.number", 42);
        context.create().resource(dataPath, props);
        context.resourceResolver().commit();

        ConfigurationAdmin configurationAdmin = new ConfigurationAdminFacade(context.getService(ConfigurationAdmin.class));
        registerInjectActivateListener(configurationAdmin);

        Configuration configuration = configurationAdmin.getConfiguration(TEST_PID, null);
        assertNotNull(configuration);
        assertEquals("test.value", configuration.getProperties().get("test.property"));
        assertEquals(42, configuration.getProperties().get("test.number"));
    }

    @Test
    public void shouldProcessResourceChangeEvents() throws IOException, NoSuchFieldException {
        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        context.create().resource(configPath);

        String dataPath = configPath + NODE_DATA;
        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "updated.value");
        context.create().resource(dataPath, props);
        context.resourceResolver().commit();

        ConfigurationAdmin configurationAdmin = new ConfigurationAdminFacade(context.getService(ConfigurationAdmin.class));

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(configurationAdmin);
        configChangeListener.onChange(Collections.singletonList(change));

        assertNotNull(configurationAdmin);
        Configuration configuration = configurationAdmin.getConfiguration(TEST_PID, null);
        assertNotNull(configuration);
        assertEquals("updated.value", configuration.getProperties().get("test.property"));
    }

    @Test
    public void shouldHandleResourceRemovalEvents() throws IOException, NoSuchFieldException {
        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + NODE_DATA;
        String backupPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + NODE_INITIAL + TEST_PID;
        Map<String, Object> backupProps = new HashMap<>();
        backupProps.put("test.property", "original.value");
        context.create().resource(backupPath, backupProps);
        context.resourceResolver().commit();

        ConfigurationAdmin configurationAdmin = new ConfigurationAdminFacade(context.getService(ConfigurationAdmin.class));

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.REMOVED,
            dataPath,
            false,
            null);

        ConfigChangeListener configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            CONFIG_ENABLED);
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, configurationAdmin);
        configChangeListener.onChange(Collections.singletonList(change));

        Configuration configuration = configurationAdmin.getConfiguration(TEST_PID, null);
        assertNotNull(configuration);
        assertEquals("original.value", configuration.getProperties().get("test.property"));
    }

    @Test
    public void shouldIgnoreNonAccountableChanges() throws IOException, NoSuchFieldException {
        ResourceChange rootChange = new JcrResourceChange(
            ResourceChange.ChangeType.REMOVED,
            ConfiguratorConstants.ROOT_PATH,
            false,
            null);
        ResourceChange initialChange = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            ConfiguratorConstants.ROOT_PATH + NODE_INITIAL + TEST_PID,
            false,
            null);
        ResourceChange nonDataChange = new JcrResourceChange(
            ResourceChange.ChangeType.ADDED,
            ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID,
            false,
            null);
        List<ResourceChange> changes = Arrays.asList(rootChange, initialChange, nonDataChange);

        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);

        ConfigChangeListener configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            CONFIG_ENABLED);
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, mockConfigurationAdmin);
        configChangeListener.onChange(changes);

        Mockito.verify(mockConfigurationAdmin, Mockito.never()).getConfiguration(Mockito.anyString(), Mockito.isNull());
    }

    @Test
    public void shouldHandleConfigurationAdminException() throws IOException, NoSuchFieldException {
        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + NODE_DATA;
        context.create().resource(dataPath);
        context.resourceResolver().commit();

        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null))
            .thenThrow(new IOException("Test configuration exception"));

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);
        configChangeListener.onChange(Collections.singletonList(change));

        Mockito.verify(mockConfigurationAdmin, Mockito.atLeastOnce()).getConfiguration(TEST_PID, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldUpdateForeignConfigurationBundleLocation() throws Exception {
        String foreignPid = "foreign.bundle.Config";
        String foreignBundleLocation = "test";
        Configuration foreignConfig = Mockito.mock(Configuration.class);
        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(Mockito.eq(foreignPid), Mockito.isNull())).thenReturn(foreignConfig);
        Mockito.when(foreignConfig.getBundleLocation()).thenReturn(foreignBundleLocation);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + foreignPid + NODE_DATA;
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("foreign.property", "foreign.value");

        context.create().resource(dataPath, configProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);

        registerInjectActivateListener(mockConfigurationAdmin);

        Mockito.verify(foreignConfig).setBundleLocation("?" + foreignBundleLocation);
        Mockito.verify(foreignConfig).update(Mockito.any(Dictionary.class));
    }

    @Test
    public void shouldCreateBackup() throws Exception {
        Dictionary<String, Object> existingProps = new Hashtable<>();
        existingProps.put("existing.property", "existing.value");
        Configuration mockConfig = Mockito.mock(Configuration.class);
        Mockito.when(mockConfig.getProperties()).thenReturn(existingProps);

        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null)).thenReturn(mockConfig);

        ConfigChangeListener configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            CONFIG_ENABLED);
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, mockConfigurationAdmin);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + NODE_DATA;
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("new.property", "new.value");

        context.create().resource(dataPath, configProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);
        configChangeListener.onChange(Collections.singletonList(change));

        String backupPath = ConfiguratorConstants.ROOT_PATH + NODE_INITIAL + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        Resource backupResource = context.resourceResolver().getResource(backupPath);
        assertNotNull(backupResource);
        assertEquals("existing.value", backupResource.getValueMap().get("existing.property"));
    }

    @Test
    public void shouldNotCreateBackupWhenAlreadyExists() throws Exception {
        Dictionary<String, Object> existingProps = new Hashtable<>();
        existingProps.put("existing.property", "existing.value");
        Configuration mockConfig = Mockito.mock(Configuration.class);

        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null)).thenReturn(mockConfig);

        String backupPath = ConfiguratorConstants.ROOT_PATH + NODE_INITIAL + TEST_PID;
        Map<String, Object> backupProperties = new HashMap<>();
        backupProperties.put("original.property", "original.value");
        context.create().resource(backupPath, backupProperties);
        context.resourceResolver().commit();

        Resource backupResource = context.resourceResolver().getResource(backupPath);
        assertNotNull(backupResource);

        ConfigChangeListener configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            CONFIG_ENABLED);
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, mockConfigurationAdmin);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + NODE_DATA;
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("new.property", "new.value");
        context.create().resource(dataPath, configProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);
        configChangeListener.onChange(Collections.singletonList(change));

        backupResource = context.resourceResolver().getResource(backupPath);
        assertNotNull(backupResource);
        assertEquals("original.value", backupResource.getValueMap().get("original.property"));
    }

    @Test
    public void shouldHandlePersistenceExceptionDuringBackupDeletion() throws Exception {
        Configuration mockConfig = Mockito.mock(Configuration.class);
        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null)).thenReturn(mockConfig);

        String backupPath = ConfiguratorConstants.ROOT_PATH + NODE_INITIAL + TEST_PID;
        context.create().resource(backupPath);
        context.resourceResolver().commit();

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + NODE_DATA;
        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);

        ConfigChangeListener configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            CONFIG_ENABLED);
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, mockConfigurationAdmin);
        configChangeListener.onChange(Collections.singletonList(change));

        Mockito.verify(mockConfigurationAdmin).getConfiguration(TEST_PID, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotUpdateConfigurationWhenPropertiesAreEqual() throws Exception {
        Dictionary<String, Object> existingProps = new Hashtable<>();
        existingProps.put("test.property", "test.value");
        existingProps.put("test.number", 42);

        Configuration mockConfig = Mockito.mock(Configuration.class);
        Mockito.when(mockConfig.getProperties()).thenReturn(existingProps);

        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null)).thenReturn(mockConfig);

        ConfigChangeListener configChangeListener = context.registerInjectActivateService(
            new ConfigChangeListener(),
            CONFIG_ENABLED);
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, mockConfigurationAdmin);

        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        String dataPath = configPath + NODE_DATA;
        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "test.value");
        props.put("test.number", 42);
        context.create().resource(dataPath, props);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);
        configChangeListener.onChange(Collections.singletonList(change));

        // Verify that configuration.update() was never called since properties are equal
        Mockito.verify(mockConfig, Mockito.never()).update(Mockito.any(Dictionary.class));
        // Verify that getConfiguration was called to retrieve the config for comparison
        Mockito.verify(mockConfigurationAdmin).getConfiguration(TEST_PID, null);
    }

    private ConfigChangeListener registerInjectActivateListener(ConfigurationAdmin configAdmin) throws NoSuchFieldException {
        ConfigChangeListener configChangeListener = context.registerService(new ConfigChangeListener());
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, configAdmin);
        PrivateAccessor.setField(configChangeListener, FIELD_RESOURCE_RESOLVER_FACTORY, context.getService(ResourceResolverFactory.class));
        ConfigChangeListenerConfiguration mockConfig = Mockito.mock(ConfigChangeListenerConfiguration.class);
        Mockito.when(mockConfig.enabled()).thenReturn(true);
        configChangeListener.activate(context.bundleContext(), mockConfig);
        return configChangeListener;
    }
}
