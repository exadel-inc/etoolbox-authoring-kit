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
        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        context.resourceResolver().commit();

        Configuration mockConfig = Mockito.mock(Configuration.class);
        Mockito.when(mockConfig.getProperties())
            .thenReturn(new Hashtable<>(Collections.singletonMap("test.property$backup$", "original.value")));
        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(TEST_PID, null))
            .thenReturn(mockConfig);

        ResourceChange change = new JcrResourceChange(
            ResourceChange.ChangeType.REMOVED,
            dataPath,
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

        Thread.sleep(500); // Allow some time for async processing
        Mockito.verify(mockConfigurationAdmin, Mockito.never()).getConfiguration(Mockito.anyString(), Mockito.isNull());
    }

    @Test
    public void shouldHandleConfigurationAdminException() throws IOException, NoSuchFieldException, InterruptedException {
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

    @SuppressWarnings("unchecked")
    @Test
    public void shouldUpdateForeignConfigurationBundleLocation() throws Exception {
        String foreignPid = "foreign.bundle.Config";
        String foreignBundleLocation = "test";
        Configuration foreignConfig = Mockito.mock(Configuration.class);
        ConfigurationAdmin mockConfigurationAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(mockConfigurationAdmin.getConfiguration(Mockito.eq(foreignPid), Mockito.isNull())).thenReturn(foreignConfig);
        Mockito.when(foreignConfig.getBundleLocation()).thenReturn(foreignBundleLocation);

        String dataPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + foreignPid + ConfiguratorConstants.SUFFIX_SLASH_DATA;
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("foreign.property", "foreign.value");

        context.create().resource(dataPath, configProps);
        context.resourceResolver().commit();

        registerInjectActivateListener(mockConfigurationAdmin);

        Mockito.verify(foreignConfig).setBundleLocation("?" + foreignBundleLocation);
        Mockito.verify(foreignConfig).update(Mockito.any(Dictionary.class));
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

        Thread.sleep(500); // Allow some time for async processing
        Mockito.verify(mockConfig, Mockito.never()).update(Mockito.any(Dictionary.class));
        Mockito.verify(mockConfigurationAdmin).getConfiguration(TEST_PID, null);
    }

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
        configChangeListener.activate(context.bundleContext(), config);
        return configChangeListener;
    }
}
