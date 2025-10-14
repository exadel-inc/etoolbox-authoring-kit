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
package com.exadel.aem.toolkit.core.configurator.models;

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
import org.apache.sling.api.SlingHttpServletRequest;
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
 * @see ConfigAttribute
 */
@SuppressWarnings("unused")
public class ConfigDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigDefinition.class);

    private static final ConfigDefinition EMPTY = new ConfigDefinition();

    private static final String KEY = "config";

    private String id;
    private ObjectClassDefinition ocd;
    private List<ConfigAttribute> attributes;
    private boolean canCleanup;
    private long changeCount;
    private boolean factory;
    private boolean factoryInstance;
    private boolean modified;
    private boolean published;
    private boolean replicable;

    /**
     * Default (instantiation-restricting) constructor
     */
    private ConfigDefinition() {
    }

    /**
     * Gets the action URL for the configuration to be used in a web form
     * @return The string value; or null if this instance is empty
     */
    public String getAction() {
        if (!isValid()) {
            return null;
        }
        return ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + id;
    }

    /**
     * Gets the cleanup action URL for the configuration to be used in a web form
     * @return The string value; or null if this instance is empty
     */
    public String getCleanupAction() {
        return canCleanup ? getAction() : getAction() + "/data";
    }

    /**
     * Gets the configuration PID
     * @return The string value
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the displayable configuration name
     * @return The string value
     */
    public String getName() {
        return ocd != null ? ocd.getName() : null;
    }

    /**
     * Gets the configuration description
     * @return The string value
     */
    public String getDescription() {
        return ocd != null ? ocd.getDescription() : null;
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
    public long getChangeCount() {
        return changeCount;
    }

    /**
     * Determines whether the configuration is a factory configuration
     * @return True or false
     */
    public boolean isFactory() {
        return factory;
    }

    /**
     * Determines whether the configuration is an instance of a factory configuration
     * @return True or false
     */
    public boolean isFactoryInstance() {
        return factoryInstance;
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
    public boolean isReplicable() {
        return replicable;
    }

    /**
     * Gets whether this instance is not data-empty
     * @return True or false
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(id);
    }

    /* -------------
       Factory logic
       ------------- */

    /**
     * Creates a {@code ConfigDefinition} instance based on the current request
     * @param request The current request
     * @return The {@code ConfigDefinition} instance; or an empty instance if the configuration with the specified ID
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

        String configId = request instanceof SlingHttpServletRequest
            ? StringUtils.strip(((SlingHttpServletRequest) request).getRequestPathInfo().getSuffix(), CoreConstants.SEPARATOR_SLASH)
            : StringUtils.substringAfterLast(request.getRequestURI(), ".html/");
        if (StringUtils.isEmpty(configId)) {
            LOG.debug("No config ID specified in the request");
            customizer.setVariable(KEY, EMPTY);
            return EMPTY;
        }

        result = from(configId);

        String existingConfigPath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId;
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
     * on the configuration ID (PID)
     * @param value The configuration ID (PID). A non-blank value is expected
     * @return The {@code ConfigDefinition} instance; or an empty instance if the configuration with the specified ID
     * does not exist
     */
    private static ConfigDefinition from(String value) {
        BundleContext context;
        ConfigurationAdmin configurationAdmin;
        MetaTypeService metaTypeService;
        try {
            context = Objects.requireNonNull(FrameworkUtil.getBundle(ConfigDefinition.class).getBundleContext());
            configurationAdmin = Objects.requireNonNull(context.getService(context.getServiceReference(ConfigurationAdmin.class)));
            metaTypeService = Objects.requireNonNull(context.getService(context.getServiceReference(MetaTypeService.class)));
        } catch (RuntimeException e) {
            LOG.error("Could not acquire OSGi entity", e);
            return EMPTY;
        }

        Configuration configuration = getConfigurationObject(configurationAdmin, value);
        if (configuration == null) {
            return EMPTY;
        }

        boolean isFactoryInstance = StringUtils.isNotEmpty(configuration.getFactoryPid())
            && !StringUtils.equals(configuration.getPid(), configuration.getFactoryPid());
        String pid = isFactoryInstance ? configuration.getFactoryPid() : configuration.getPid();

        for (Bundle bundle : context.getBundles()) {
            MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
            if (metaTypeInformation == null) {
                continue;
            }
            ObjectClassDefinition ocd;
            try {
                ocd = Objects.requireNonNull(metaTypeInformation.getObjectClassDefinition(pid, null));
            } catch (IllegalArgumentException | NullPointerException e) {
                // Not an error: this actually happens if the configuration is not present in the current bundle
                continue;
            }
            ConfigDefinition result = from(configuration, ocd);
            result.factory = ArrayUtils.contains(metaTypeInformation.getFactoryPids(), value);
            result.factoryInstance = isFactoryInstance;
            result.id = value;
            return result;
        }
        return EMPTY;
    }

    /**
     * Called from {@link ConfigDefinition#from(String)} to create a {@code ConfigDefinition} instance based on the
     * {@link Configuration} and {@link ObjectClassDefinition} objects
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
     * Retrieves the OSGi native {@link Configuration} object by its identifier
     * @param configAdmin The {@code ConfigurationAdmin} service instance. A non-null value is expected
     * @param id          The configuration PID. A non-blank value is expected
     * @return The {@code Configuration} instance; or null if the configuration with the specified ID does not exist
     */
    private static Configuration getConfigurationObject(ConfigurationAdmin configAdmin, String id) {
        try {
            return configAdmin.getConfiguration(id, null);
        } catch (Exception e) {
            LOG.error("Could not retrieve configuration for {}", id, e);
            return null;
        }
    }
}
