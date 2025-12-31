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

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.jcr.Session;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ExternalResourceChangeListener;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;
import com.exadel.aem.toolkit.core.utils.ValueMapUtil;

/**
 * Listens to changes in the repository under the specified root and updates OSGi configurations accordingly
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@Component(
    service = ConfigChangeListener.class,
    immediate = true
)
@Designate(ocd = ConfigChangeListenerConfiguration.class)
public class ConfigChangeListener implements ResourceChangeListener, ExternalResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigChangeListener.class);

    private static final int ASYNC_THREAD_COUNT = 5;

    private static final String UPDATABLE_CONFIG_TOKEN = "?";

    @Reference
    private transient ConfigurationAdmin configurationAdmin;

    @Reference
    private transient ResourceResolverFactory resourceResolverFactory;

    @Reference
    private transient SlingSettingsService slingSettingsService;

    private ExecutorService asyncExecutor;

    private ServiceRegistration<ResourceChangeListener> registration;

    /* --------------------
       Startup and shutdown
       -------------------- */

    /**
     * Initializes the instance, reading existing configurations from the repository
     * @param context Bundle context to use for registering the resource change listener
     * @param config  Configuration instance
     */
    @Activate
    void activate(BundleContext context, ConfigChangeListenerConfiguration config) {
        LOG.info("Configuration change listener is {}", config.enabled() ? "enabled" : "disabled");
        try (ResourceResolver resolver = newResolver()) {
            if (ArrayUtils.isNotEmpty(config.cleanUp())) {
                activateWithCleanUp(resolver, config.cleanUp());
            }
            if (config.enabled()) {
                activateWithUpdating(resolver);
            }
        } catch (LoginException | PersistenceException e) {
            LOG.error("Failed to initialize configurations", e);
        }
        if (!config.enabled()) {
            return;
        }
        asyncExecutor = Executors.newFixedThreadPool(ASYNC_THREAD_COUNT);
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(ResourceChangeListener.PATHS, new String[]{ConfiguratorConstants.ROOT_PATH});
        properties.put(ResourceChangeListener.CHANGES, new String[]{"ADDED", "CHANGED", "REMOVED"});
        registration = context.registerService(ResourceChangeListener.class, this, properties);
    }

    /**
     * Called from {@link ConfigChangeListener#activate(BundleContext, ConfigChangeListenerConfiguration)} to clean up
     * specified configurations and their backups, if any
     * @param resolver Resource resolver to use
     * @param pids     Array of configuration PIDs to clean up
     * @throws PersistenceException If an error occurs during resource deletion
     */
    private void activateWithCleanUp(ResourceResolver resolver, String[] pids) throws PersistenceException {
        for (String pid : pids) {
            LOG.info("Cleaning up user-defined configuration {}", pid);
            String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + pid;
            Resource configResource = resolver.getResource(configPath);
            if (configResource != null) {
                resolver.delete(configResource);
            } else {
                LOG.warn("User-defined configuration {} not found", pid);
            }
        }
        if (resolver.hasChanges()) {
            resolver.commit();
        }
    }

    /**
     * Called from {@link ConfigChangeListener#activate(BundleContext, ConfigChangeListenerConfiguration)} to read
     * existing configurations from the repository and update OSGi configurations accordingly
     * @param resolver Resource resolver to use
     */
    private void activateWithUpdating(ResourceResolver resolver) {
        Resource configRoot = resolver.getResource(ConfiguratorConstants.ROOT_PATH);
        if (configRoot == null) {
            LOG.error("Configuration root not found");
            return;
        }
        for (Resource resource : configRoot.getChildren()) {
            Resource dataNode = resource.getChild(ConfiguratorConstants.NN_DATA);
            if (dataNode == null) {
                LOG.debug("Configuration {} has no data node", resource.getPath());
                continue;
            }
            updateConfiguration(dataNode);
        }
    }

    /**
     * Cleans up the instance, unregistering the resource change listener if it was registered previously
     */
    @Deactivate
    private void deactivate() {
        LOG.info("Configuration change listener is shutting down");
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            asyncExecutor = null;
        }
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }

    /* -------------
       Changes logic
       ------------- */

    /**
     * Determines whether this listener is currently enabled
     * @return True or false
     */
    public boolean isEnabled() {
        return registration != null;
    }

    /**
     * Handles resource change events
     * @param list List of resource changes to process
     */
    @Override
    public void onChange(List<ResourceChange> list) {
        Set<String> configsToReset = new HashSet<>();
        Set<String> configsToUpdate = new HashSet<>();
        LOG.debug("Received {} resource change(s)", list.size());
        for (ResourceChange change : list) {
            boolean isRelevant = false;
            boolean isDataNode = StringUtils.endsWith(change.getPath(), ConfiguratorConstants.SUFFIX_SLASH_DATA);
            if (
                (change.getType() == ResourceChange.ChangeType.ADDED
                    || change.getType() == ResourceChange.ChangeType.CHANGED)
                    && isDataNode
            ) {
                configsToUpdate.add(change.getPath());
                isRelevant = true;

            } else if (change.getType() == ResourceChange.ChangeType.REMOVED
                && !ConfiguratorConstants.ROOT_PATH.equals(change.getPath())) {

                configsToReset.add(change.getPath());
                isRelevant = true;
            }
            LOG.debug("{} at {}{}", change.getType(), change.getPath(), isRelevant ? " (processable)" : " (ignored)");
        }
        if (configsToReset.isEmpty() && configsToUpdate.isEmpty()) {
            return;
        }
        asyncExecutor.submit(() -> {
            try (ResourceResolver resolver = newResolver()) {
                for (String path : configsToUpdate) {
                    Resource resource = resolver.getResource(path);
                    if (resource == null) {
                        // Config removal may produce a {@code CHANGE} event, but then the resource cannot be found
                        configsToReset.add(path);
                    } else {
                        updateConfiguration(resource);
                    }
                }
                for (String path : configsToReset) {
                    resetConfiguration(extractPid(path));
                }
                if (resolver.hasChanges()) {
                    resolver.commit();
                }
            } catch (LoginException | PersistenceException e) {
                LOG.error("Failed to process configuration changes", e);
            }
        });
    }

    /* --------------------
       Sling instance logic
       -------------------- */

    /**
     * Creates a new {@link ResourceResolver} instance for accessing repository resources
     * @return New instance of {@code ResourceResolver}
     * @throws LoginException If the resolver cannot be created
     */
    private ResourceResolver newResolver() throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, "eak-service")
        );
    }

    /* -------------------
       Configuration logic
       ------------------- */

    /**
     * Retrieves an OSGi configuration by its identifier
     * @param pid Configuration identifier
     * @return The configuration instance, or null if it cannot be retrieved
     */
    private Configuration readConfiguration(String pid) {
        try {
            return configurationAdmin.getConfiguration(pid, null);
        } catch (Exception e) {
            LOG.error("Could not retrieve configuration for {}", pid, e);
        }
        return null;
    }

    /**
     * Resets the configuration identified by the specified identifier to its last backed-up state, if any
     * @param pid Configuration identifier
     */
    private void resetConfiguration(String pid) {
        LOG.info("Resetting configuration {}", pid);
        Configuration configuration = readConfiguration(pid);
        if (configuration == null) {
            return;
        }
        Dictionary<String, Object> backup = ConfigDataUtil.getBackup(configuration);
        boolean shouldSkip = backup.isEmpty();
        if (shouldSkip) {
            // If there is not any backup, even consisting of the only "remove" marker, it means that the configuration
            // has never been modified, or else has already been reset, so there is nothing to do
            return;
        }
        boolean shouldErase = backup.size() == 1
            && Session.ACTION_REMOVE.equals(backup.get(ConfiguratorConstants.SUFFIX_BACKUP));
        if (shouldErase) {
            backup = new Hashtable<>(Collections.emptyMap());
        }
        try {
            configuration.update(backup);
        } catch (Exception e) {
            LOG.error("Could not reset configuration {}", pid, e);
        }
    }

    /**
     * Updates the configuration corresponding to the specified resource
     * @param resource The resource representing the configuration
     */
    private void updateConfiguration(Resource resource) {
        String pid = extractPid(resource);
        LOG.info("Updating configuration {} to match user settings", pid);
        Configuration configuration = readConfiguration(pid);
        if (configuration == null) {
            return;
        }
        String[] selectedProperties = isAuthorInstance() ? null : extractSelectedProperties(resource);
        if (ConfigDataUtil.containsAll(
            configuration.getProperties(),
            ValueMapUtil.filter(resource.getValueMap(), selectedProperties))) {
            LOG.debug("Configuration {} is up to date with user settings", pid);
            return;
        }
        Dictionary<String, ?> embeddedBackup = ConfigDataUtil.getBackup(configuration);
        if (embeddedBackup.isEmpty()) {
            embeddedBackup = ConfigDataUtil.getData(configuration);
            if (embeddedBackup.isEmpty()) {
                // There is not a "real" configuration other than default values. When doing a reset, we will need to
                // erase the properties of a current configuration to bring back the defaults
                embeddedBackup = new Hashtable<>(
                    Collections.singletonMap(
                        ConfiguratorConstants.SUFFIX_BACKUP,
                        Session.ACTION_REMOVE)
                );
            }
        }
        try {
            updateConfiguration(configuration, resource, embeddedBackup);
        } catch (Exception e) {
            LOG.error("Could not update configuration {}", configuration.getPid(), e);
        }
    }

    /**
     * Updates the configuration using the properties of the specified resource and embedding a backup of the current
     * configuration state
     * @param configuration The configuration to update
     * @param data          The resource containing data to use for the update
     * @param backup        The backup copy of the configuration, if any
     * @throws Exception If the configuration cannot be updated
     */
    private void updateConfiguration(
        Configuration configuration,
        Resource data,
        Dictionary<String, ?> backup) throws Exception {

        boolean isForeignConfig = !extractPid(data).startsWith(CoreConstants.ROOT_PACKAGE);
        String originalBundleLocation = null;
        try {
            originalBundleLocation = configuration.getBundleLocation();
            if (
                StringUtils.isNotEmpty(originalBundleLocation)
                    && isForeignConfig
                    && !originalBundleLocation.startsWith(UPDATABLE_CONFIG_TOKEN)
            ) {
                configuration.setBundleLocation(UPDATABLE_CONFIG_TOKEN + originalBundleLocation);
            }
        } catch (UnsupportedOperationException e) {
            // Ignored for the sake of using with wcm.io mocks
        }
        Dictionary<String, Object> updateData = ConfigDataUtil.getData(configuration);
        String[] selectedProperties = isAuthorInstance() ? null : extractSelectedProperties(data);
        ValueMapUtil
            .filter(
                ValueMapUtil.excludeSystemProperties(data.getValueMap()),
                selectedProperties)
            .forEach(updateData::put);
        if (!backup.isEmpty()) {
            Enumeration<String> keys = backup.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                updateData.put(StringUtils.appendIfMissing(key, ConfiguratorConstants.SUFFIX_BACKUP), backup.get(key));
            }
        }
        configuration.update(updateData);
        try {
            if (!StringUtils.equals(originalBundleLocation, configuration.getBundleLocation())) {
                configuration.setBundleLocation(originalBundleLocation);
            }
        } catch (UnsupportedOperationException e) {
            // Ignored for the sake of using with wcm.io mocks
        }
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Determines whether the current Sling instance is an author instance
     * @return True or false
     */
    private boolean isAuthorInstance() {
        return slingSettingsService == null || slingSettingsService.getRunModes().contains("author");
    }

    /**
     * Extracts the configuration PID from the specified resource
     * @param resource The resource representing the configuration
     * @return String value
     */
    private static String extractPid(Resource resource) {
        return ConfiguratorConstants.NN_DATA.equals(resource.getName())
            ? Objects.requireNonNull(resource.getParent()).getName()
            : resource.getName();
    }

    /**
     * Extracts the configuration PID from the specified resource path
     * @param path The path to the resource representing the configuration
     * @return String value
     */
    private static String extractPid(String path) {
        String configRootPath = StringUtils.removeEnd(
            path,
            ConfiguratorConstants.SUFFIX_SLASH_DATA);
        return StringUtils.substringAfterLast(configRootPath, CoreConstants.SEPARATOR_SLASH);
    }

    /**
     * Extracts the selected properties from the specified resource
     * @param resource The resource representing the configuration
     * @return String array
     */
    private static String[] extractSelectedProperties(Resource resource) {
        ValueMap valueMap = ConfiguratorConstants.NN_DATA.equals(resource.getName())
            ? Objects.requireNonNull(resource.getParent()).getValueMap()
            : resource.getValueMap();
        return valueMap.get(ConfiguratorConstants.PN_REPLICATION_PROPS, String[].class);
    }
}
