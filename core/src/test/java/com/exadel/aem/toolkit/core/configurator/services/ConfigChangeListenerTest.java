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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.jcr.resource.internal.JcrResourceChange;
import org.apache.sling.settings.SlingSettingsService;
import org.apache.sling.testing.mock.sling.services.MockSlingSettingService;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import io.wcm.testing.mock.aem.junit.AemContext;
import junitx.util.PrivateAccessor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

public class ConfigChangeListenerTest {

    private static final String FIELD_CONFIG_ADMIN = "configurationAdmin";
    private static final String FIELD_RESOURCE_RESOLVER_FACTORY = "resourceResolverFactory";
    private static final String FIELD_SLING_SETTINGS_SERVICE = "slingSettingsService";

    private static final String TEST_PID = "com.example.test.Config";
    private static final String PATH_CONFIG = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + TEST_PID;
    private static final String PATH_DATA = PATH_CONFIG + ConfiguratorConstants.SUFFIX_SLASH_DATA;

    @Rule
    public AemContext context = AemContextFactory.newInstance();

    /* ----------
       Activation
       ---------- */

    @Test
    public void shouldProcessEnabledProperty() throws NoSuchFieldException {
        ConfigChangeListener configChangeListener = registerInjectActivateListener();
        assertTrue(configChangeListener.isEnabled());

        configChangeListener = registerInjectActivateListener(newConfig(false));
        assertFalse(configChangeListener.isEnabled());
    }

    @Test
    public void shouldCleanUpConfigurationsOnActivate() throws PersistenceException, NoSuchFieldException {
        context.create().resource(PATH_CONFIG);
        context.resourceResolver().commit();

        assertNotNull(context.resourceResolver().getResource(PATH_CONFIG));

        registerInjectActivateListener(newConfig(true, new String[]{TEST_PID, "com.example.test.AnotherConfig"}));

        assertNull(context.resourceResolver().getResource(PATH_CONFIG));
    }

    @Test
    public void shouldUpdateConfigurationsOnActivate() throws IOException, NoSuchFieldException {
        context.create().resource(PATH_CONFIG);
        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "test.value");
        props.put("test.number", 42);
        context.create().resource(PATH_DATA, props);
        context.resourceResolver().commit();

        ConfigurationAdmin configAdmin = new ConfigurationAdminFacade(context.getService(ConfigurationAdmin.class));
        registerInjectActivateListener(configAdmin);

        Configuration configuration = configAdmin.getConfiguration(TEST_PID, null);
        assertNotNull(configuration);
        assertEquals("test.value", configuration.getProperties().get("test.property"));
        assertEquals(42, configuration.getProperties().get("test.number"));
    }

    /* ------------
       Deactivation
       ------------ */

    @Test
    public void shouldDeactivateCleanly() throws Throwable {
        ConfigChangeListener configChangeListener = registerInjectActivateListener();
        assertTrue(configChangeListener.isEnabled());

        PrivateAccessor.invoke(configChangeListener, "deactivate", new Class[0], new Object[0]);

        assertFalse(configChangeListener.isEnabled());
    }

    /* --------------
       Change events
       -------------- */

    @Test
    public void shouldProcessResourceChangeEvents() throws IOException, NoSuchFieldException, InterruptedException {
        context.create().resource(PATH_CONFIG);
        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "updated.value");
        context.create().resource(PATH_DATA, props);
        context.resourceResolver().commit();

        ConfigurationAdmin configAdmin = new ConfigurationAdminFacade(context.getService(ConfigurationAdmin.class));
        ConfigChangeListener configChangeListener = registerInjectActivateListener(configAdmin);

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Configuration configuration = configAdmin.getConfiguration(TEST_PID, null);
        assertNotNull(configuration);
        assertEquals("updated.value", configuration.getProperties().get("test.property"));
    }

    @Test
    public void shouldIgnoreNonAccountableChanges() throws NoSuchFieldException, InterruptedException {
        // Root removal and non-data ADDED are ignored in author mode; in publish mode, non-data ADDED is processed
        ResourceChange rootChange = new JcrResourceChange(
            ResourceChange.ChangeType.REMOVED,
            ConfiguratorConstants.ROOT_PATH,
            false,
            null);
        ResourceChange nonDataChange = new JcrResourceChange(
            ResourceChange.ChangeType.ADDED,
            PATH_CONFIG,
            false,
            null);
        List<ResourceChange> changes = Arrays.asList(rootChange, nonDataChange);

        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        MockSlingSettingService settingsService = (MockSlingSettingService) context.getService(SlingSettingsService.class);
        assertNotNull(settingsService);

        settingsService.setRunModes(Collections.singleton("author"));
        configChangeListener.onChange(changes);
        Thread.sleep(500);
        assertEquals(0, admin.callCount);

        settingsService.setRunModes(Collections.singleton("publish"));
        configChangeListener.onChange(changes);
        Thread.sleep(500);
        assertEquals(1, admin.callCount);
    }

    @Test
    public void shouldUpdateConfigurationAfterFailure() throws NoSuchFieldException, InterruptedException, PersistenceException {
        StubConfiguration config = new StubConfiguration(TEST_PID);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        // Activate first (with no resources present, so activation does nothing)
        ConfigChangeListener configChangeListener = registerInjectActivateListener(
            admin, newConfig(true, new String[0], 2, 0L));

        // Create the resource after activation so it is found only on the retry attempt
        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "retry.value");
        context.create().resource(PATH_DATA, props);
        context.resourceResolver().commit();

        // Replace the factory with one that returns null on the very first getResource(PATH_DATA) call
        ResourceResolverFactory realFactory = context.getService(ResourceResolverFactory.class);
        PrivateAccessor.setField(
            configChangeListener,
            FIELD_RESOURCE_RESOLVER_FACTORY,
            new FailFirstResourceResolverFactory(realFactory, PATH_DATA));

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> lastUpdate = config.lastUpdate();
        assertNotNull(lastUpdate);
        assertEquals("retry.value", lastUpdate.get("test.property"));
        assertEquals(1, config.updateCount());
    }

    @Test
    public void shouldUpdateOnPublishNonDataNodeChange() throws IOException, NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        MockSlingSettingService settingsService = (MockSlingSettingService) context.getService(SlingSettingsService.class);
        assertNotNull(settingsService);
        settingsService.setRunModes(Collections.singleton("publish"));

        // Register listener before creating JCR resources so activation finds nothing
        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "test.value");
        context.create().resource(PATH_DATA, props);
        context.resourceResolver().commit();

        // ADDED on the parent node (non-data path) simulates a publish replication
        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.ADDED, PATH_CONFIG, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> lastUpdate = config.lastUpdate();
        assertNotNull(lastUpdate);
        assertEquals("test.value", lastUpdate.get("test.property"));
    }

    /* ---------------------
       Configuration update
       --------------------- */

    @Test
    public void shouldNotUpdateConfigurationWhenPropertiesAreEqual() throws IOException, NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID,
            "test.property", "test.value",
            "test.number", 42);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        Map<String, Object> props = new HashMap<>();
        props.put("test.property", "test.value");
        props.put("test.number", 42);
        context.create().resource(PATH_DATA, props);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        assertEquals(0, config.updateCount());
        assertEquals(1, admin.callCount);
    }

    @Test
    public void shouldUpdateForeignConfigurationBundleLocation() throws IOException, NoSuchFieldException {
        String foreignPid = "foreign.bundle.Config";
        String foreignDataPath = ConfiguratorConstants.ROOT_PATH
            + CoreConstants.SEPARATOR_SLASH + foreignPid + ConfiguratorConstants.SUFFIX_SLASH_DATA;

        StubConfiguration foreignConfig = new StubConfiguration(foreignPid);
        foreignConfig.setBundleLocation("test");
        foreignConfig.bundleLocationHistory.clear(); // discard the setup call from recorded history

        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(foreignPid, foreignConfig);

        Map<String, Object> configProps = new HashMap<>();
        configProps.put("foreign.property", "foreign.value");
        context.create().resource(foreignDataPath, configProps);
        context.resourceResolver().commit();

        registerInjectActivateListener(admin);

        assertTrue(foreignConfig.bundleLocationHistory.contains("?test"));
        assertTrue(foreignConfig.updateCount() > 0);
    }

    @Test
    public void shouldHandlePartialConfiguration() throws IOException, NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID,
            "existing.property", "existing.value",
            "another.property", "another.value",
            "numeric.property", 42,
            "boolean.property", true);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        MockSlingSettingService settingsService = (MockSlingSettingService) context.getService(SlingSettingsService.class);
        assertNotNull(settingsService);
        settingsService.setRunModes(Collections.singleton("publish"));

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        Map<String, Object> partialProps = new HashMap<>();
        partialProps.put("existing.property", "modified.value");
        partialProps.put("another.property", "another.modified.value");
        partialProps.put("numeric.property", 99);
        partialProps.put("boolean.property", false);
        context.create().resource(PATH_DATA, partialProps);

        Resource testResource = context.resourceResolver().getResource(PATH_CONFIG);
        ModifiableValueMap valueMap = Objects.requireNonNull(testResource).adaptTo(ModifiableValueMap.class);
        assertNotNull(valueMap);
        valueMap.put(ConfiguratorConstants.PN_REPLICATION_PROPS, new String[]{"numeric.property", "boolean.property"});
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.ADDED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertEquals("existing.value", result.get("existing.property"));
        assertEquals("another.value", result.get("another.property"));
        assertEquals(99, result.get("numeric.property"));
        assertEquals(Boolean.FALSE, result.get("boolean.property"));
    }

    @Test
    public void shouldHandlePartialConfigurationWithNewProperty() throws IOException, NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID, "existing.property", "existing.value");
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        MockSlingSettingService settingsService = (MockSlingSettingService) context.getService(SlingSettingsService.class);
        assertNotNull(settingsService);
        settingsService.setRunModes(Collections.singleton("publish"));

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        Map<String, Object> partialProps = new HashMap<>();
        partialProps.put("new.property", "new.value");
        context.create().resource(PATH_DATA, partialProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertEquals("existing.value", result.get("existing.property"));
        assertEquals("new.value", result.get("new.property"));
    }

    @Test
    public void shouldCreateBackup() throws IOException, NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID,
            "original.property", "original.value",
            "another.property", 100);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        Map<String, Object> updateProps = new HashMap<>();
        updateProps.put("original.property", "updated.value");
        context.create().resource(PATH_DATA, updateProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertEquals("updated.value", result.get("original.property"));
        assertEquals(100, result.get("another.property"));
        assertEquals("original.value", result.get("original.property" + ConfiguratorConstants.SUFFIX_BACKUP));
        assertEquals(100, result.get("another.property" + ConfiguratorConstants.SUFFIX_BACKUP));
    }

    @Test
    public void shouldCreateRemoveMarkerBackup() throws IOException, NoSuchFieldException {
        // When the original config has no real data, the backup is {$backup$: remove} so
        // a subsequent reset will erase all applied properties rather than restoring old values
        StubConfiguration config = new StubConfiguration(TEST_PID);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        Map<String, Object> updateProps = new HashMap<>();
        updateProps.put("new.property", "new.value");
        context.create().resource(PATH_DATA, updateProps);
        context.resourceResolver().commit();

        registerInjectActivateListener(admin);

        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertEquals("new.value", result.get("new.property"));
        assertEquals(Session.ACTION_REMOVE, result.get(ConfiguratorConstants.SUFFIX_BACKUP));
    }

    @Test
    public void shouldPreserveBackupValuesWhenUpdatingPartialConfiguration() throws IOException, NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID,
            "property.one", "current.value.one",
            "property.two", "current.value.two",
            "property.one" + ConfiguratorConstants.SUFFIX_BACKUP, "original.value.one",
            "property.two" + ConfiguratorConstants.SUFFIX_BACKUP, "original.value.two");
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        Map<String, Object> partialProps = new HashMap<>();
        partialProps.put("property.one", "updated.value.one");
        context.create().resource(PATH_DATA, partialProps);
        context.resourceResolver().commit();

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertEquals("updated.value.one", result.get("property.one"));
        assertEquals("current.value.two", result.get("property.two"));
        assertEquals("original.value.one", result.get("property.one" + ConfiguratorConstants.SUFFIX_BACKUP));
        assertEquals("original.value.two", result.get("property.two" + ConfiguratorConstants.SUFFIX_BACKUP));
    }

    /* --------------------
       Configuration reset
       -------------------- */

    @Test
    public void shouldRestoreConfigurationFromBackup() throws NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID,
            "property.one", "modified.value",
            "property.two", 200,
            "property.one" + ConfiguratorConstants.SUFFIX_BACKUP, "original.value",
            "property.two" + ConfiguratorConstants.SUFFIX_BACKUP, 100);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.REMOVED, PATH_DATA, false, null);
        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertEquals("original.value", result.get("property.one"));
        assertEquals(100, result.get("property.two"));
        assertNull(result.get("property.one" + ConfiguratorConstants.SUFFIX_BACKUP));
        assertNull(result.get("property.two" + ConfiguratorConstants.SUFFIX_BACKUP));
    }

    @Test
    public void shouldNotResetWithoutBackup() throws NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID, "property", "value");
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.REMOVED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        assertEquals(0, config.updateCount());
    }

    @Test
    public void shouldEraseConfigOnRemoveMarkerBackup() throws NoSuchFieldException, InterruptedException {
        StubConfiguration config = new StubConfiguration(TEST_PID,
            ConfiguratorConstants.SUFFIX_BACKUP, Session.ACTION_REMOVE);
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.REMOVED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldFallbackToResetWhenResourceMissing() throws NoSuchFieldException, InterruptedException {
        // Data path does not exist  resolved resource will be null, triggering reset instead of update
        StubConfiguration config = new StubConfiguration(TEST_PID,
            "test.property" + ConfiguratorConstants.SUFFIX_BACKUP, "original.value");
        StubConfigurationAdmin admin = new StubConfigurationAdmin();
        admin.register(TEST_PID, config);

        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        Dictionary<String, ?> result = config.lastUpdate();
        assertNotNull(result);
        assertEquals("original.value", result.get("test.property"));
    }

    /* ---------------
       Error handling
       --------------- */

    @Test
    public void shouldHandleException() throws IOException, NoSuchFieldException, InterruptedException {
        context.create().resource(PATH_DATA);
        context.resourceResolver().commit();

        StubConfigurationAdmin admin = new StubConfigurationAdmin(true);

        ResourceChange change = new JcrResourceChange(ResourceChange.ChangeType.CHANGED, PATH_DATA, false, null);
        ConfigChangeListener configChangeListener = registerInjectActivateListener(admin);
        configChangeListener.onChange(Collections.singletonList(change));

        Thread.sleep(500);
        assertTrue(admin.callCount >= 1);
    }

    /* ---------------
       Utility methods
       --------------- */

    private ConfigChangeListener registerInjectActivateListener() throws NoSuchFieldException {
        return registerInjectActivateListener(context.getService(ConfigurationAdmin.class), newConfig(true));
    }

    @SuppressWarnings("UnusedReturnValue")
    private ConfigChangeListener registerInjectActivateListener(ConfigChangeListenerConfiguration config) throws NoSuchFieldException {
        return registerInjectActivateListener(context.getService(ConfigurationAdmin.class), config);
    }

    private ConfigChangeListener registerInjectActivateListener(ConfigurationAdmin configurationAdmin) throws NoSuchFieldException {
        return registerInjectActivateListener(configurationAdmin, newConfig(true));
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

    private static ConfigChangeListenerConfiguration newConfig(boolean enabled) {
        return newConfig(enabled, new String[0]);
    }

    private static ConfigChangeListenerConfiguration newConfig(boolean enabled, String[] cleanUp) {
        return newConfig(enabled, cleanUp, 0, 10L);
    }

    private static ConfigChangeListenerConfiguration newConfig(
            boolean enabled,
            String[] cleanUp,
            int retryCount,
            long retryDelay) {

        return new ConfigChangeListenerConfiguration() {
            @Override public boolean enabled() { return enabled; }
            @Override public String[] cleanUp() { return cleanUp; }
            @Override public int resolveRetryCount() { return retryCount; }
            @Override public long resolveRetryDelay() { return retryDelay; }
            @Override public Class<? extends Annotation> annotationType() { return ConfigChangeListenerConfiguration.class; }
        };
    }

    private static class StubConfiguration implements Configuration {

        private final String pid;
        private Dictionary<String, Object> props = new Hashtable<>();
        private final List<Dictionary<String, ?>> updates = new ArrayList<>();
        final List<String> bundleLocationHistory = new ArrayList<>();
        private String bundleLocation;

        StubConfiguration(String pid, Object... entries) {
            this.pid = pid;
            for (int i = 0; i + 1 < entries.length; i += 2) {
                props.put(String.valueOf(entries[i]), entries[i + 1]);
            }
        }

        @Override public String getPid() { return pid; }
        @Override public String getFactoryPid() { return null; }
        @Override public Dictionary<String, Object> getProperties() { return props; }
        @Override public String getBundleLocation() { return bundleLocation; }

        @Override
        public void setBundleLocation(String location) {
            bundleLocationHistory.add(location);
            bundleLocation = location;
        }

        @Override
        public void update(Dictionary<String, ?> properties) {
            Hashtable<String, Object> copy = new Hashtable<>();
            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                copy.put(key, properties.get(key));
            }
            updates.add(copy);
            props = copy;
        }

        @Override public void update() { /* no-op */ }
        @Override public void delete() { /* no-op */ }
        @Override public long getChangeCount() { return updates.size(); }

        Dictionary<String, ?> lastUpdate() {
            return updates.isEmpty() ? null : updates.get(updates.size() - 1);
        }

        int updateCount() { return updates.size(); }
    }

    private static class StubConfigurationAdmin implements ConfigurationAdmin {

        private final Map<String, Configuration> configs = new HashMap<>();
        private final boolean throwOnGet;
        int callCount = 0;

        StubConfigurationAdmin() { this(false); }

        StubConfigurationAdmin(boolean throwOnGet) { this.throwOnGet = throwOnGet; }

        void register(String pid, Configuration config) { configs.put(pid, config); }

        @Override
        public Configuration getConfiguration(String pid, String location) throws IOException {
            callCount++;
            if (throwOnGet) {
                throw new IOException("NOT AN EXCEPTION: testing ConfigChangeListener logic");
            }
            return configs.get(pid);
        }

        @Override
        public Configuration getConfiguration(String pid) throws IOException { return getConfiguration(pid, null); }

        @Override
        public Configuration createFactoryConfiguration(String factoryPid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Configuration createFactoryConfiguration(String factoryPid, String location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Configuration[] listConfigurations(String filter) {
            return null;
        }
    }

    private static class FailFirstResourceResolverFactory implements ResourceResolverFactory {

        private final ResourceResolverFactory delegate;
        private final String failPath;

        FailFirstResourceResolverFactory(ResourceResolverFactory delegate, String failPath) {
            this.delegate = delegate;
            this.failPath = failPath;
        }

        @Override
        @Nonnull
        public ResourceResolver getServiceResourceResolver(Map<String, Object> authInfo) throws LoginException {
            ResourceResolver real = delegate.getServiceResourceResolver(authInfo);
            AtomicBoolean firstCall = new AtomicBoolean(true);
            return (ResourceResolver) Proxy.newProxyInstance(
                ResourceResolver.class.getClassLoader(),
                new Class[]{ResourceResolver.class},
                (proxy, method, args) -> {
                    if ("getResource".equals(method.getName())
                            && args != null
                            && args.length == 1
                            && failPath.equals(args[0])
                            && firstCall.getAndSet(false)) {
                        return null;
                    }
                    try {
                        return method.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
        }

        @Override
        @Nonnull
        public ResourceResolver getResourceResolver(Map<String, Object> authInfo) throws LoginException {
            return delegate.getResourceResolver(authInfo);
        }

        @Override
        @Nonnull
        @SuppressWarnings("deprecation")
        public ResourceResolver getAdministrativeResourceResolver(Map<String, Object> authInfo) throws LoginException {
            return delegate.getAdministrativeResourceResolver(authInfo);
        }

        @Override
        @Nullable
        public ResourceResolver getThreadResourceResolver() {
            return delegate.getThreadResourceResolver();
        }

        @Override
        @Nonnull
        public List<String> getSearchPath() {
            return delegate.getSearchPath();
        }
    }
}
