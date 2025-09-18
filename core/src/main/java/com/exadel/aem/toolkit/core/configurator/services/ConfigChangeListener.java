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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
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

    private static final String UPDATABLE_CONFIG_TOKEN = "?";

    private static final String NN_INITIAL = "initial";
    private static final String NN_DATA = "data";

    @Reference
    private transient ConfigurationAdmin configurationAdmin;

    @Reference
    private transient ResourceResolverFactory resourceResolverFactory;

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
            LOG.info("Configuration change listening is disabled");
            return;
        }
        LOG.info("Configuration change listening is enabled");
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
            String configPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + pid;
            Resource configResource = resolver.getResource(configPath);
            if (configResource != null) {
                resolver.delete(configResource);
            } else {
                LOG.warn("Clean up of {} skipped: configuration not found", pid);
            }

            String backupPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + NN_INITIAL + CoreConstants.SEPARATOR_SLASH + pid;
            Resource backupResource = resolver.getResource(backupPath);
            if (backupResource != null) {
                resolver.delete(backupResource);
            } else {
                LOG.warn("Backup configuration for {} not found", pid);
            }
        }
        resolver.commit();
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
            if (NN_INITIAL.equals(resource.getName())) {
                continue;
            }
            updateConfiguration(resource.getChild(NN_DATA));
        }
    }

    /**
     * Cleans up the instance, unregistering the resource change listener if it was registered previously
     */
    @Deactivate
    private void deactivate() {
        if (registration != null) {
            LOG.info("Configuration change listener is shutting down");
            registration.unregister();
        }
        registration = null;
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
        for (ResourceChange change : list) {
            if (!isAccountable(change)) {
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
                resetConfiguration(resolver, extractPid(path));
            }
        } catch (LoginException e) {
            LOG.error("Failed to process configuration changes", e);
        }
    }

    /**
     * Determines whether the specified resource change is relevant to configuration management
     * @param change The resource change to check
     * @return True or false
     */
    private static boolean isAccountable(ResourceChange change) {
        if (change.getType() == ResourceChange.ChangeType.REMOVED) {
            return !ConfiguratorConstants.ROOT_PATH.equals(change.getPath())
                && !StringUtils.endsWith(change.getPath(), CoreConstants.SEPARATOR_SLASH + NN_INITIAL)
                && !StringUtils.contains(change.getPath(), CoreConstants.SEPARATOR_SLASH + NN_INITIAL + CoreConstants.SEPARATOR_SLASH);
        }
        return StringUtils.endsWith(change.getPath(), CoreConstants.SEPARATOR_SLASH + NN_DATA);
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
            LOG.error("Could not retrieve configuration for {}", pid, e);
        }
        return null;
    }

    /**
     * Resets the configuration identified by the specified identifier to its last backed-up state, if any
     * @param resolver Resource resolver instance to use
     * @param pid      Configuration identifier
     */
    private void resetConfiguration(ResourceResolver resolver, String pid) {
        String backupPath = ConfiguratorConstants.ROOT_PATH
            + CoreConstants.SEPARATOR_SLASH + NN_INITIAL
            + CoreConstants.SEPARATOR_SLASH + pid;
        Resource backupResource = resolver.getResource(backupPath);
        if (backupResource == null) {
            return;
        }
        updateConfiguration(backupResource, false);
        try {
            resolver.delete(backupResource);
            resolver.commit();
        } catch (PersistenceException e) {
            LOG.error("Could not delete backup configuration for {}", pid, e);
        }
    }

    /**
     * Updates the configuration corresponding to the specified resource
     * @param resource The resource representing the configuration
     */
    private void updateConfiguration(Resource resource) {
        updateConfiguration(resource, true);
    }

    /**
     * Updates the configuration corresponding to the specified resource, optionally creating a backup copy beforehand
     * @param resource The resource representing the configuration
     * @param doBackup If true, a backup copy of the configuration will be created if it does not exist yet
     */
    private void updateConfiguration(Resource resource, boolean doBackup) {
        if (resource == null) {
            return;
        }
        String pid = extractPid(resource);
        Configuration configuration = readConfiguration(pid);
        if (configuration == null) {
            LOG.warn("Could not retrieve configuration for resource {}", resource.getPath());
            return;
        }
        if (doBackup && !hasBackup(resource)) {
            createBackup(resource, configuration);
        }
        try {
            updateOsgi(resource, configuration);
        } catch (Exception e) {
            LOG.error("Could not update configuration for {}", configuration.getPid(), e);
        }
    }

    /**
     * Updates the specified OSGi configuration using the properties of the specified resource
     * @param resource      The resource representing the configuration
     * @param configuration The configuration to update
     * @throws Exception If the configuration cannot be updated
     */
    private void updateOsgi(Resource resource, Configuration configuration) throws Exception {
        boolean isForeignConfig = !extractPid(resource).startsWith(CoreConstants.ROOT_PACKAGE);
        try {
            String bundleLocation = configuration.getBundleLocation();
            if (StringUtils.isNotEmpty(bundleLocation) && isForeignConfig && !bundleLocation.startsWith(UPDATABLE_CONFIG_TOKEN)) {
                configuration.setBundleLocation(UPDATABLE_CONFIG_TOKEN + bundleLocation);
            }
        } catch (UnsupportedOperationException e) {
            // Ignored for the sake of using with wcm.io mocks
        }
        configuration.update(MapUtil.toDictionary(resource.getValueMap()));
    }

    /**
     * Extracts the configuration PID from the specified resource
     * @param resource The resource representing the configuration
     * @return String value
     */
    private static String extractPid(Resource resource) {
        return NN_DATA.equals(resource.getName())
            ? Objects.requireNonNull(resource.getParent()).getName()
            : resource.getName();
    }

    /**
     * Extracts the configuration PID from the specified resource path
     * @param path The path to the resource representing the configuration
     * @return String value
     */
    private static String extractPid(String path) {
        String configRootPath = StringUtils.removeEnd(path, CoreConstants.SEPARATOR_SLASH + NN_DATA);
        return StringUtils.substringAfterLast(configRootPath, CoreConstants.SEPARATOR_SLASH);
    }

    /* --------------------------
       Configuration backup logic
       -------------------------- */

    /**
     * Checks if a backup copy of the specified configuration resource exists
     * @param resource The resource representing the configuration
     * @return True or false
     */
    private static boolean hasBackup(Resource resource) {
        String pid = extractPid(resource);
        String backupPath = ConfiguratorConstants.ROOT_PATH
            + CoreConstants.SEPARATOR_SLASH + NN_INITIAL
            + CoreConstants.SEPARATOR_SLASH + pid;
        return resource.getResourceResolver().getResource(backupPath) != null;
    }

    /**
     * Creates a backup copy of the specified configuration resource
     * @param resource The resource representing the configuration
     * @param config   The configuration to back up
     */
    private static void createBackup(Resource resource, Configuration config) {
        ResourceResolver resolver = resource.getResourceResolver();
        Resource root = resolver.getResource(ConfiguratorConstants.ROOT_PATH);
        if (root == null) {
            return;
        }
        try {
            Resource backupRoot = root.getChild(NN_INITIAL);
            if (backupRoot == null) {
                backupRoot = resolver.create(root, NN_INITIAL, null);
            }
            Resource backupResource = backupRoot.getChild(extractPid(resource));
            if (backupResource != null) {
                resolver.delete(backupResource);
            }
            Map<String, Object> properties = MapUtil.toMap(config.getProperties());
            properties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
            resolver.create(backupRoot, extractPid(resource), properties);
            resolver.commit();
        } catch (Exception e) {
            LOG.error("Could not create backup config resource", e);
        }
    }
}
