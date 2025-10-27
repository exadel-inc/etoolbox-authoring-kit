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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.ui.components.ExpressionCustomizer;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;
import com.exadel.aem.toolkit.core.configurator.utils.PermissionUtil;
import com.exadel.aem.toolkit.core.configurator.utils.RequestUtil;

/**
 * Represents a configuration definition, i.e., a set of configuration attributes united by the same PID together
 * with metadata usefult to build the {@code EToolbox Configurator} user experience
 * <p><b>Note</b>: This class is not a part of the public API and is subject to change. Do not use it in your own code
 * @see ConfigAttribute
 */
public class ConfigDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigDefinition.class);

    private static final ConfigDefinition EMPTY = new ConfigDefinition();

    private static final String KEY = "config";

    private String pid;
    private String factoryPid;

    private List<ConfigAttribute> attributes;
    private boolean canCleanup;
    private long changeCount;
    private List<ConfigDefinition> children;
    private boolean isFactory;
    private boolean modified;
    private ObjectClassDefinition ocd;
    private boolean published;
    private boolean replicable;

    /**
     * Default (instantiation-restricting) constructor
     */
    private ConfigDefinition() {
    }

    /* --------------
       Main accessors
       -------------- */

    /**
     * Gets the action URL for the configuration to be used in a web form
     * @return The string value; or null if this instance is empty
     */
    public String getAction() {
        if (!isValid()) {
            return null;
        }
        return ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + pid;
    }

    /**
     * Gets the list of configuration attributes
     * @return The list of {@link ConfigAttribute} instances
     */
    public List<ConfigAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Gets the number of times the configuration has been changed
     * @return The integer value
     */
    @SuppressWarnings("unused") // Used in Granite page
    public long getChangeCount() {
        return changeCount;
    }

    /**
     * Gets the list of child configuration definitions (factory configuration instances)
     * @return The nullable list of {@link ConfigDefinition} instances
     */
    public List<ConfigDefinition> getChildren() {
        return children;
    }

    /**
     * Gets the cleanup action URL for the configuration to be used in a web form
     * @return The string value; or null if this instance is empty
     */
    @SuppressWarnings("unused") // Used in Granite page
    public String getCleanupAction() {
        return canCleanup ? getAction() : getAction() + "/data";
    }

    /**
     * Gets the configuration description
     * @return The string value
     */
    public String getDescription() {
        return ocd != null ? ocd.getDescription() : null;
    }

    /**
     * Gets the configuration PID
     * @return The string value
     */
    public String getPid() {
        return pid;
    }

    /**
     * Gets the displayable configuration name
     * @return The string value
     */
    public String getName() {
        return ocd != null ? ocd.getName() : null;
    }

    /**
     * Gets the displayable configuration title, which is the name if available or the PID otherwise
     * @return The string value
     */
    public String getTitle() {
        return StringUtils.defaultIfEmpty(getName(), getPid());
    }

    /**
     * Determines whether the configuration is a factory configuration
     * @return True or false
     */
    public boolean isFactory() {
        return isFactory;
    }

    /**
     * Determines whether the configuration is an instance of a factory configuration
     * @return True or false
     */
    public boolean isFactoryInstance() {
        return StringUtils.isNotEmpty(factoryPid) && !StringUtils.equals(factoryPid, pid);
    }

    /**
     * Determines whether the configuration has been modified by a user
     * @return True or false
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Determines whether the configuration has been replicated to publisher instance(-s)
     * @return True or false
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * Gets whether the current user has permissions to replicate the configuration data
     * @return True or false
     */
    @SuppressWarnings("unused") // Used in Granite page
    public boolean isReplicable() {
        return replicable;
    }

    /**
     * Gets whether this instance is not data-empty
     * @return True or false
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(pid);
    }

    /* ---------
       Mutations
       --------- */

    /**
     * Assigns a child configuration definition to this instance
     * @param value The {@code ConfigDefinition} instance to be assigned as a child (usually, a factory configuration
     *              instance)
     */
    void addChild(ConfigDefinition value) {
        isFactory = true;
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(value);
        if (children.size() > 1) {
            children.sort((a, b) -> StringUtils.compare(a.getPid(), b.getPid()));
        }
    }

    /**
     * Sets the factory PID
     * @param value The string value; may be null
     */
    void setFactoryPid(String value) {
        this.factoryPid = value;
    }

    /* -------------
       Factory logic
       ------------- */

    /**
     * Creates a {@code ConfigDefinition} instance based on the current request
     * @param request The current request
     * @return The {@code ConfigDefinition} instance; or an empty instance if the configuration with the specified PID
     * does not exist or the request is null
     */
    public static ConfigDefinition from(HttpServletRequest request) {
        if (request == null) {
            return EMPTY;
        }

        ExpressionCustomizer customizer = ExpressionCustomizer.from(request);
        ConfigDefinition result = (ConfigDefinition) customizer.getVariable(KEY);
        if (result != null) {
            return result;
        }

        String pid = RequestUtil.getConfigPid(request);
        if (StringUtils.isEmpty(pid)) {
            LOG.debug("No config PID specified in the request");
            customizer.setVariable(KEY, EMPTY);
            return EMPTY;
        }

        BundleContext context = (BundleContext) request.getAttribute(BundleContext.class.getName());
        if (context == null) {
            context = FrameworkUtil.getBundle(ConfigDefinition.class).getBundleContext();
        }
        result = from(pid, context);

        String existingConfigPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + pid;
        Resource existingConfig = Optional.ofNullable(RequestUtil.getResourceResolver(request))
            .map(resolver -> resolver.getResource(existingConfigPath))
            .orElse(null);

        result.canCleanup = !PermissionUtil.hasOverridingPermissions(request);
        result.replicable = PermissionUtil.hasReplicatePermission(request);
        result.modified = existingConfig != null
            && existingConfig.getChild(ConfiguratorConstants.NN_DATA) != null;
        result.published = existingConfig != null
            && existingConfig.getValueMap().get(ConfiguratorConstants.PN_REPLICATION_ACTION, StringUtils.EMPTY).equals("Activate");

        customizer.setVariable(KEY, result);
        return result;
    }

    /**
     * Called from {@link ConfigDefinition#from(HttpServletRequest)} to create a {@code ConfigDefinition} instance based
     * on the configuration PID
     * @param pid The configuration PID. A non-blank value is expected
     * @param context The {@link BundleContext} instance
     * @return The {@code ConfigDefinition} instance; or an empty instance if the configuration with the specified PID
     * does not exist
     */
    private static ConfigDefinition from(String pid, BundleContext context) {
        ConfigurationAdmin configurationAdmin;
        MetaTypeService metaTypeService;
        try {
            configurationAdmin = Objects.requireNonNull(context.getService(context.getServiceReference(ConfigurationAdmin.class)));
            metaTypeService = Objects.requireNonNull(context.getService(context.getServiceReference(MetaTypeService.class)));
        } catch (RuntimeException e) {
            LOG.error("Could not acquire OSGi entity", e);
            return EMPTY;
        }

        Configuration configuration = getConfigurationObject(configurationAdmin, pid);
        if (configuration == null) {
            return EMPTY;
        }

        boolean isFactoryInstance = StringUtils.isNotEmpty(configuration.getFactoryPid())
            && !StringUtils.equals(configuration.getPid(), configuration.getFactoryPid());
        String metatypePid = isFactoryInstance ? configuration.getFactoryPid() : configuration.getPid();

        for (Bundle bundle : context.getBundles()) {
            MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
            if (metaTypeInformation == null) {
                continue;
            }
            ObjectClassDefinition ocd;
            try {
                ocd = Objects.requireNonNull(metaTypeInformation.getObjectClassDefinition(metatypePid, null));
            } catch (IllegalArgumentException | NullPointerException e) {
                // Not an error: this actually happens if the configuration is not present in the current bundle
                continue;
            }
            ConfigDefinition result = from(configuration, ocd);
            result.isFactory = ArrayUtils.contains(metaTypeInformation.getFactoryPids(), pid);
            result.pid = pid;
            result.factoryPid = configuration.getFactoryPid();
            return result;
        }
        return EMPTY;
    }

    /**
     * Called from {@link ConfigDefinition#from(String, BundleContext)} to create a {@code ConfigDefinition} instance
     * based on the {@link Configuration} and {@link ObjectClassDefinition} objects
     * @param configuration The {@code Configuration} instance. A non-null value is expected
     * @param ocd           The {@code ObjectClassDefinition} instance
     * @return The {@code ConfigDefinition} instance
     */
    private static ConfigDefinition from(Configuration configuration, ObjectClassDefinition ocd) {
        Map<String, Object> configProperties = Collections.emptyMap();
        if (configuration.getProperties() != null) {
            configProperties = Collections.list(configuration.getProperties().keys())
                .stream()
                .collect(Collectors.toMap(k -> k, k -> configuration.getProperties().get(k)));
        }

        ConfigDefinition result = new ConfigDefinition();
        result.attributes = new ArrayList<>();
        for (AttributeDefinition definition : ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
            result.attributes.add(
                new ConfigAttribute(
                    definition,
                    configProperties.get(definition.getID()))
            );
        }
        result.changeCount = configuration.getChangeCount();
        result.ocd = ocd;
        return result;
    }

    /**
     * Creates a shallow {@link ConfigDefinition} instance based on the specified parameters
     * @param pid        The configuration PID. A non-blank value is expected
     * @param factoryPid The factory PID if this configuration is an instance of a factory configuration; may be null
     * @param ocd        The {@link ObjectClassDefinition} instance. Can be null
     * @param isFactory  True if the configuration is a factory configuration; false otherwise
     * @return The {@code ConfigDefinition} instance; or an empty instance if the specified PID is blank
     */
    static ConfigDefinition from(String pid, String factoryPid, ObjectClassDefinition ocd, boolean isFactory) {
        ConfigDefinition result = new ConfigDefinition();
        result.pid = pid;
        result.factoryPid = factoryPid;
        result.isFactory = isFactory;
        result.ocd = ocd;
        if (ocd == null)  {
            return result;
        }
        result.attributes = new ArrayList<>();
        for (AttributeDefinition definition : ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
            result.attributes.add(new ConfigAttribute(definition, null));
        }
        return result;
    }

    /**
     * Retrieves the OSGi native {@link Configuration} object by its identifier
     * @param configAdmin The {@code ConfigurationAdmin} service instance. A non-null value is expected
     * @param pid          The configuration PID. A non-blank value is expected
     * @return The {@code Configuration} instance; or null if the configuration with the specified PID does not exist
     */
    private static Configuration getConfigurationObject(ConfigurationAdmin configAdmin, String pid) {
        try {
            return configAdmin.getConfiguration(pid, null);
        } catch (Exception e) {
            LOG.error("Could not retrieve configuration for {}", pid, e);
            return null;
        }
    }
}
