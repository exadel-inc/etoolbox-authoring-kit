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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.configurator.models.ConfigDefinition;

/**
 * Provides utility methods to work with OSGi configurations
 */
class ConfigHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigHelper.class);

    private BundleContext bundleContext;
    private ConfigurationAdmin configurationAdmin;
    private MetaTypeService metaTypeService;

    /**
     * Default (instantiation-restricting) constructor
     */
    private ConfigHelper() {
    }

    /**
     * Retrieves a configuration definition by its identifier
     * @param id The configuration PID. A non-blank value is expected
     * @return The {@link ConfigDefinition} instance; or null if the configuration with the specified ID does not exist
     */
    public ConfigDefinition getConfig(String id) {
        Configuration configuration = getConfigurationObject(id);
        if (configuration == null) {
            return null;
        }

        boolean isFactoryInstance = StringUtils.isNotEmpty(configuration.getFactoryPid())
            && !StringUtils.equals(configuration.getPid(), configuration.getFactoryPid());
        String pid = isFactoryInstance ? configuration.getFactoryPid() : configuration.getPid();
        for (Bundle bundle : bundleContext.getBundles()) {
            MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
            if (metaTypeInformation == null) {
                continue;
            }
            ObjectClassDefinition ocd = null;
            boolean isFactoryConfig = ArrayUtils.contains(metaTypeInformation.getFactoryPids(), id);
            try {
                ocd = Objects.requireNonNull(metaTypeInformation.getObjectClassDefinition(pid, null));
            } catch (IllegalArgumentException | NullPointerException e) {
                // Not an error: this actually happens if the configuration is not present in the current bundle
                continue;
            }
            Map<String, Object> values = Collections.emptyMap();
            if (configuration.getProperties() != null) {
                values = Collections.list(configuration.getProperties().keys())
                    .stream()
                    .collect(Collectors.toMap(k -> k, k -> configuration.getProperties().get(k)));
            }
            return ConfigDefinition
                .builder()
                .changeCount(configuration.getChangeCount())
                .id(id)
                .isFactory(isFactoryConfig)
                .isFactoryInstance(isFactoryInstance)
                .ocd(ocd)
                .values(values)
                .build();
        }
        return null;
    }

    /**
     * Retrieves the OSGi native {@link Configuration} object by its identifier
     * @param id The configuration PID. A non-blank value is expected
     * @return The {@code Configuration} instance; or null if the configuration with the specified ID does not exist
     */
    private Configuration getConfigurationObject(String id) {
        try {
            return configurationAdmin.getConfiguration(id, null);
        } catch (Exception e) {
            LOG.error("Could not retrieve configuration for {}", id, e);
            return null;
        }
    }

    /* -------------
       Factory logic
       ------------- */

    /**
     * Prepares a new instance of the {@link Builder} class to create a {@code ConfigUtil} object
     * @return The {@code Builder} instance
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Implements the builder pattern for {@link ConfigHelper} instantiation
     */
    static class Builder {
        private BundleContext bundleContext;
        private ConfigurationAdmin configurationAdmin;
        private MetaTypeService metaTypeService;

        /**
         * Assigns the {@link BundleContext} instance to be used by the {@code ConfigHelper} object
         * @param value The {@code BundleContext} instance
         * @return This instance
         */
        Builder bundleContext(BundleContext value) {
            this.bundleContext = value;
            return this;
        }

        /**
         * Assigns the {@link ConfigurationAdmin} instance to be used by the {@code ConfigHelper} object
         * @param value A reference to {@code ConfigurationAdmin} instance
         * @return This instance
         */
        Builder configurationAdmin(ConfigurationAdmin value) {
            this.configurationAdmin = value;
            return this;
        }

        /**
         * Assigns the {@link MetaTypeService} instance to be used by the {@code ConfigHelper} object
         * @param value A reference to {@code MetaTypeService}
         * @return This instance
         */
        Builder metaTypeService(MetaTypeService value) {
            this.metaTypeService = value;
            return this;
        }

        /**
         * Creates a new {@link ConfigHelper} instance with the parameters set through this builder
         * @return The {@code ConfigHelper} instance
         */
        ConfigHelper build() {
            ConfigHelper helper = new ConfigHelper();
            helper.bundleContext = bundleContext;
            helper.configurationAdmin = configurationAdmin;
            helper.metaTypeService = metaTypeService;
            return helper;
        }
    }
}
