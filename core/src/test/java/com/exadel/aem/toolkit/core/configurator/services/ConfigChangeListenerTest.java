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
import java.util.Objects;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.jcr.resource.internal.JcrResourceChange;
import org.apache.sling.settings.SlingSettingsService;
import org.apache.sling.testing.mock.sling.services.MockSlingSettingService;
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
    private static final String FIELD_SLING_SETTINGS_SERVICE = "slingSettingsService";

    private static final String TEST_PID = "com.example.test.Config";

    @Rule
    public AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldProcessEnabledProperty() throws Exception {
        ConfigChangeListener configChangeListener = registerInjectActivateListener();
        assertNotNull(PrivateAccessor.getField(configChangeListener, "registration"));

        ConfigChangeListenerConfiguration mockConfig = Mockito.mock(ConfigChangeListenerConfiguration.class);
        Mockito.when(mockConfig.enabled()).thenReturn(false);
        configChangeListener = registerInjectActivateListener(mockConfig);
        assertNull(PrivateAccessor.getField(configChangeListener, "registration"));
    }

    @Test
    public void shouldCleanUpConfigurationsOnActivate() throws PersistenceException, NoSuchFieldException {
        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;

        context.create().resource(configPath);
        context.resourceResolver().commit();

        assertNotNull(context.resourceResolver().getResource(configPath));

        ConfigChangeListenerConfiguration mockConfig = Mockito.mock(ConfigChangeListenerConfiguration.class);
        Mockito.when(mockConfig.enabled()).thenReturn(true);
        Mockito.when(mockConfig.cleanUp()).thenReturn(new String[] {TEST_PID, "com.example.test.AnotherConfig"});
        registerInjectActivateListener(mockConfig);

        assertNull(context.resourceResolver().getResource(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID));
    }

    @Test
    public void shouldUpdateConfigurationsOnActivate() throws IOException, NoSuchFieldException {
        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        context.create().resource(configPath);

        String dataPath = configPath + ConfiguratorConstants.SUFFIX_SLASH_DATA;
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

        String dataPath = configPath + ConfiguratorConstants.SUFFIX_SLASH_DATA;
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
    public void shouldHandleResourceRemovalEvents() throws IOException, NoSuchFieldException, InterruptedException {
        Configuration mockConfig = buildMockConfiguration("test.property$backup$", "original.value");
        ConfigurationAdmin mockConfigurationAdmin = buildMockConfigurationAdmin(mockConfig);

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.REMOVED,
            ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA,
            false,
            null);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500); // Allow some time for async processing
        Mockito.verify(mockConfig).update(new Hashtable<>(Collections.singletonMap("test.property", "original.value")));
    }

    @Test
    public void shouldIgnoreNonAccountableChanges() throws IOException, NoSuchFieldException, InterruptedException {
        ResourceChange rootChange = new JcrResourceChange(
            ResourceChange.ChangeType.REMOVED,
            ConfiguratorConstants.ROOT_PATH,
            false,
            null);
        ResourceChange nonDataChange = new JcrResourceChange(
            ResourceChange.ChangeType.ADDED,
            ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID,
            false,
            null);
        List<ResourceChange> changes = Arrays.asList(rootChange, nonDataChange);

        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);
        configChangeListener.onChange(changes);

        Thread.sleep(500);
        Mockito.verify(mockConfigurationAdmin, Mockito.never()).getConfiguration(Mockito.anyString(), Mockito.isNull());
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

        Map<String, Object> configProps = new HashMap<>();
        configProps.put("foreign.property", "foreign.value");
        context.create().resource(
            ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + foreignPid + ConfiguratorConstants.SUFFIX_SLASH_DATA,
            configProps);
        context.resourceResolver().commit();

        registerInjectActivateListener(mockConfigurationAdmin);

        Mockito.verify(foreignConfig).setBundleLocation("?" + foreignBundleLocation);
        Mockito.verify(foreignConfig).update(Mockito.any(Dictionary.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotUpdateConfigurationWhenPropertiesAreEqual() throws Exception {
        Configuration mockConfig = buildMockConfiguration(
            "test.property", "test.value",
            "test.number", 42);
        ConfigurationAdmin mockConfigurationAdmin = buildMockConfigurationAdmin(mockConfig);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);

        String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
        String dataPath = configPath + ConfiguratorConstants.SUFFIX_SLASH_DATA;
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

        Thread.sleep(500);
        Mockito.verify(mockConfig, Mockito.never()).update(Mockito.any(Dictionary.class));
        Mockito.verify(mockConfigurationAdmin).getConfiguration(TEST_PID, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHandlePartialConfiguration() throws Exception {
        Configuration mockConfig = buildMockConfiguration(
            "existing.property", "existing.value",
            "another.property", "another.value",
            "numeric.property", 42,
            "boolean.property", true);
        ConfigurationAdmin mockConfigurationAdmin = buildMockConfigurationAdmin(mockConfig);

        MockSlingSettingService mockSlingSettingsService = (MockSlingSettingService) context.getService(SlingSettingsService.class);
        assertNotNull(mockSlingSettingsService);
        mockSlingSettingsService.setRunModes(Collections.singleton("publish"));

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        Map<String, Object> partialProps = new HashMap<>();
        partialProps.put("existing.property", "modified.value");
        partialProps.put("another.property", "another.modified.value");
        partialProps.put("numeric.property", 99);
        partialProps.put("boolean.property", false);
        context.create().resource(dataPath, partialProps);

        Resource testResource = context.resourceResolver().getResource(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID);
        ModifiableValueMap valueMap = Objects.requireNonNull(testResource).adaptTo(ModifiableValueMap.class);
        assertNotNull(valueMap);
        valueMap.put(ConfiguratorConstants.PN_REPLICATION_PROPS, new String[] {"numeric.property", "boolean.property"});
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.ADDED,
            dataPath,
            false,
            null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Mockito.verify(mockConfig).update(Mockito.argThat(dict -> {
            Dictionary<String, Object> d = (Dictionary<String, Object>) dict;
            return "existing.value".equals(d.get("existing.property"))
                && "another.value".equals(d.get("another.property"))
                && Integer.valueOf(99).equals(d.get("numeric.property"))
                && Boolean.FALSE.equals(d.get("boolean.property"));
        }));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldHandlePartialConfigurationWithNewProperty() throws Exception {
        Configuration mockConfig = buildMockConfiguration("existing.property", "existing.value");
        ConfigurationAdmin mockConfigurationAdmin = buildMockConfigurationAdmin(mockConfig);

        MockSlingSettingService mockSlingSettingsService = (MockSlingSettingService) context.getService(SlingSettingsService.class);
        assertNotNull(mockSlingSettingsService);
        mockSlingSettingsService.setRunModes(Collections.singleton("publish"));

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        Map<String, Object> partialProps = new HashMap<>();
        partialProps.put("new.property", "new.value");
        context.create().resource(dataPath, partialProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Mockito.verify(mockConfig).update(Mockito.argThat(dict -> {
            Dictionary<String, Object> d = (Dictionary<String, Object>) dict;
            return "existing.value".equals(d.get("existing.property"))
                && "new.value".equals(d.get("new.property"));
        }));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateBackup() throws Exception {
        Configuration mockConfig = buildMockConfiguration(
            "original.property", "original.value",
            "another.property", 100);
        ConfigurationAdmin mockConfigurationAdmin = buildMockConfigurationAdmin(mockConfig);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        Map<String, Object> updateProps = new HashMap<>();
        updateProps.put("original.property", "updated.value");
        context.create().resource(dataPath, updateProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Mockito.verify(mockConfig).update(Mockito.argThat(dict -> {
            Dictionary<String, Object> d = (Dictionary<String, Object>) dict;
            return "updated.value".equals(d.get("original.property"))
                && Integer.valueOf(100).equals(d.get("another.property"))
                && "original.value".equals(d.get("original.property" + ConfiguratorConstants.SUFFIX_BACKUP))
                && Integer.valueOf(100).equals(d.get("another.property" + ConfiguratorConstants.SUFFIX_BACKUP));
        }));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPreserveBackupValuesWhenUpdatingPartialConfiguration() throws Exception {
        Configuration mockConfig = buildMockConfiguration(
            "property.one", "current.value.one",
            "property.two", "current.value.two",
            "property.one$backup$", "original.value.one",
            "property.two$backup$", "original.value.two");
        ConfigurationAdmin mockConfigurationAdmin = buildMockConfigurationAdmin(mockConfig);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        Map<String, Object> partialProps = new HashMap<>();
        partialProps.put("property.one", "updated.value.one");
        context.create().resource(dataPath, partialProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Mockito.verify(mockConfig).update(Mockito.argThat(dict -> {
            Dictionary<String, Object> d = (Dictionary<String, Object>) dict;
            return "updated.value.one".equals(d.get("property.one"))
                && "current.value.two".equals(d.get("property.two"))
                && "original.value.one".equals(d.get("property.one" + ConfiguratorConstants.SUFFIX_BACKUP))
                && "original.value.two".equals(d.get("property.two" + ConfiguratorConstants.SUFFIX_BACKUP));
        }));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRestoreConfigurationFromBackup() throws IOException, NoSuchFieldException, InterruptedException {
        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        context.resourceResolver().commit();

        Dictionary<String, Object> configProps = new Hashtable<>();
        configProps.put("property.one", "modified.value");
        configProps.put("property.two", 200);
        configProps.put("property.one" + ConfiguratorConstants.SUFFIX_BACKUP, "original.value");
        configProps.put("property.two" + ConfiguratorConstants.SUFFIX_BACKUP, 100);

        Configuration mockConfig = Mockito.mock(Configuration.class);
        Mockito.when(mockConfig.getProperties()).thenReturn(configProps);
        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null)).thenReturn(mockConfig);

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.REMOVED,
            dataPath,
            false,
            null);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Mockito.verify(mockConfig).update(Mockito.argThat(dict -> {
            Dictionary<String, Object> d = (Dictionary<String, Object>) dict;
            return "original.value".equals(d.get("property.one"))
                && Integer.valueOf(100).equals(d.get("property.two"))
                && d.get("property.one" + ConfiguratorConstants.SUFFIX_BACKUP) == null
                && d.get("property.two" + ConfiguratorConstants.SUFFIX_BACKUP) == null;
        }));
    }

    @Test
    public void shouldHandleException() throws IOException, NoSuchFieldException, InterruptedException {
        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        context.create().resource(dataPath);
        context.resourceResolver().commit();

        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null))
            .thenThrow(new IOException("NOT AN EXCEPTION: testing ConfigChangeListener logic"));

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.CHANGED,
            dataPath,
            false,
            null);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(mockConfigurationAdmin);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500); // Allow some time for async processing
        Mockito.verify(mockConfigurationAdmin, Mockito.atLeastOnce()).getConfiguration(TEST_PID, null);
    }

    /* ---------------
       Utility methods
       --------------- */

    private ConfigChangeListener registerInjectActivateListener() throws NoSuchFieldException {
        ConfigChangeListenerConfiguration mockConfig = Mockito.mock(ConfigChangeListenerConfiguration.class);
        Mockito.when(mockConfig.enabled()).thenReturn(true);
        return registerInjectActivateListener(context.getService(ConfigurationAdmin.class), mockConfig);
    }

    @SuppressWarnings("UnusedReturnValue")
    private ConfigChangeListener registerInjectActivateListener(ConfigChangeListenerConfiguration config) throws NoSuchFieldException {
        return registerInjectActivateListener(context.getService(ConfigurationAdmin.class), config);
    }

    private ConfigChangeListener registerInjectActivateListener(ConfigurationAdmin configurationAdmin) throws NoSuchFieldException {
        ConfigChangeListenerConfiguration mockConfig = Mockito.mock(ConfigChangeListenerConfiguration.class);
        Mockito.when(mockConfig.enabled()).thenReturn(true);
        return registerInjectActivateListener(configurationAdmin, mockConfig);
    }

    private ConfigChangeListener registerInjectActivateListener(
        ConfigurationAdmin configAdmin,
        ConfigChangeListenerConfiguration config) throws NoSuchFieldException {

        ConfigChangeListener configChangeListener = context.registerService(new ConfigChangeListener());
        PrivateAccessor.setField(configChangeListener, FIELD_CONFIG_ADMIN, configAdmin);
        PrivateAccessor.setField(configChangeListener, FIELD_RESOURCE_RESOLVER_FACTORY, context.getService(ResourceResolverFactory.class));
        PrivateAccessor.setField(configChangeListener, FIELD_SLING_SETTINGS_SERVICE, context.getService(SlingSettingsService.class));
        configChangeListener.activate(context.bundleContext(), config);
        return configChangeListener;
    }

    private static Configuration buildMockConfiguration(Object... properties) {
        Configuration mockConfig = Mockito.mock(Configuration.class);
        Dictionary<String, Object> props = new Hashtable<>();
        for (int i = 0; i < properties.length; i += 2) {
            props.put(properties[i].toString(), properties[i + 1]);
        }
        Mockito.when(mockConfig.getProperties()).thenReturn(props);
        return mockConfig;
    }

    private static ConfigurationAdmin buildMockConfigurationAdmin(Configuration config) throws IOException {
        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null)).thenReturn(config);
        return mockConfigurationAdmin;
    }
}
