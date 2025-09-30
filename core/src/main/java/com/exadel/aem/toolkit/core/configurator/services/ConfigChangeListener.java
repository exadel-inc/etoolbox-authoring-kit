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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.discovery.DiscoveryService;
import org.apache.sling.discovery.InstanceDescription;
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
import org.slf4j.event.Level;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

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
public class ConfigChangeListener implements ResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigChangeListener.class);

    private static final int ASYNC_THREAD_COUNT = 5;

    private static final String UPDATABLE_CONFIG_TOKEN = "?";

    private static final String SEPARATOR_COMMA_SPACE = ", ";

    @Reference
    private transient ConfigurationAdmin configurationAdmin;

    @Reference
    private transient DiscoveryService discoveryService;

    @Reference
    private transient ResourceResolverFactory resourceResolverFactory;

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
        log(Level.INFO, "Configuration change listener is {}", config.enabled() ? "enabled" : "disabled");
        try (ResourceResolver resolver = newResolver()) {
            if (ArrayUtils.isNotEmpty(config.cleanUp())) {
                activateWithCleanUp(resolver, config.cleanUp());
            }
            if (config.enabled()) {
                activateWithUpdating(resolver);
            }
        } catch (LoginException | PersistenceException e) {
            log(Level.ERROR, "Failed to initialize configurations", e);
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
            log(Level.INFO, "Cleaning up user-defined configuration {}", pid);
            String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + pid;
            Resource configResource = resolver.getResource(configPath);
            if (configResource != null) {
                resolver.delete(configResource);
            } else {
                log(Level.WARN, "User-defined configuration {} not found", pid);
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
            log(Level.ERROR, "Configuration root not found");
            return;
        }
        for (Resource resource : configRoot.getChildren()) {
            Resource dataNode = resource.getChild(ConfiguratorConstants.NN_DATA);
            if (dataNode == null) {
                log(Level.DEBUG, "Configuration {} has no data node", resource.getPath());
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
        log(Level.INFO, "Configuration change listener is shutting down");
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
        log(
            Level.DEBUG,
            "Received {} resource change(s): {}",
            list.size(),
            list.stream()
                .map(change -> change.getType() + " at " + change.getPath() + (isRelevantChange(change) ? " (processable)" : " (ignored)"))
                .reduce((a, b) -> a + SEPARATOR_COMMA_SPACE + b)
                .orElse("(none)")
        );
        for (ResourceChange change : list) {
            if (!isRelevantChange(change)) {
                continue;
            }
            if (change.getType() == ResourceChange.ChangeType.REMOVED) {
                configsToReset.add(change.getPath());
            } else {
                configsToUpdate.add(change.getPath());
            }
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
            } catch (LoginException e) {
                log(Level.ERROR, "Failed to process configuration changes", e);
            }
        });
    }

    /**
     * Determines whether the specified resource change is relevant to configuration management
     * @param change The resource change to check
     * @return True or false
     */
    private static boolean isRelevantChange(ResourceChange change) {
        if (change.getType() == ResourceChange.ChangeType.REMOVED) {
            return !ConfiguratorConstants.ROOT_PATH.equals(change.getPath());
        }
        return StringUtils.endsWith(change.getPath(), CoreConstants.SEPARATOR_SLASH + ConfiguratorConstants.NN_DATA);
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
        } catch (IOException | SecurityException e) {
            log(Level.ERROR, "Could not retrieve configuration for {}", pid, e);
        }
        return null;
    }

    /**
     * Resets the configuration identified by the specified identifier to its last backed-up state, if any
     * @param pid Configuration identifier
     */
    private void resetConfiguration(String pid) {
        log(Level.INFO, "Resetting configuration {}", pid);
        Configuration configuration = readConfiguration(pid);
        if (configuration == null) {
            return;
        }
        Dictionary<String, Object> backup = ConfigUtil.getBackup(configuration);
        if (backup.isEmpty()) {
            log(Level.DEBUG, "No backup found for {}. Probably the configuration was default or empty before user update", pid);
        }
        try {
            configuration.update(backup);
        } catch (IOException e) {
            log(Level.ERROR, "Could not reset configuration {}", pid, e);
        }
    }

    /**
     * Updates the configuration corresponding to the specified resource
     * @param resource The resource representing the configuration
     */
    private void updateConfiguration(Resource resource) {
        String pid = extractPid(resource);
        log(Level.INFO, "Updating configuration {} to match user settings", pid);
        Configuration configuration = readConfiguration(pid);
        if (configuration == null) {
            return;
        }
        if (ConfigUtil.equals(configuration.getProperties(), resource.getValueMap())) {
            log(Level.DEBUG, "Configuration {} is up to date with user settings", pid);
            return;
        }
        Dictionary<String, ?> embeddedBackup = ConfigUtil.getBackup(configuration);
        if (embeddedBackup.isEmpty()) {
            embeddedBackup = ConfigUtil.getData(configuration);
        }
        try {
            updateConfiguration(configuration, resource, embeddedBackup);
        } catch (Exception e) {
            log(Level.ERROR, "Could not update configuration {}", configuration.getPid(), e);
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
        Dictionary<String, Object> updateData = ConfigUtil.toDictionary(data.getValueMap());
        if (!backup.isEmpty()) {
            Enumeration<String> keys = backup.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                updateData.put(key + ConfiguratorConstants.SUFFIX_BACKUP, backup.get(key));
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
     * Logs a message with the Sling instance identifier and leadership status prepended
     * @param level   The logging level
     * @param message The message to log
     * @param args    Arguments to substitute into the message
     */
    private void log(Level level, String message, Object... args) {
        String marker;
        if (discoveryService != null) {
            InstanceDescription localInstance = discoveryService.getTopology().getLocalInstance();
            marker = localInstance.getSlingId() + (localInstance.isLeader() ? " (leader)" : StringUtils.EMPTY);
            message = marker + StringUtils.SPACE + message;
        }
        switch (level) {
            case ERROR:
                LOG.error(message, args);
                break;
            case WARN:
                LOG.warn(message, args);
                break;
            case DEBUG:
                LOG.debug(message, args);
                break;
            default:
                LOG.info(message, args);
        }
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
            CoreConstants.SEPARATOR_SLASH + ConfiguratorConstants.NN_DATA);
        return StringUtils.substringAfterLast(configRootPath, CoreConstants.SEPARATOR_SLASH);
    }
}
