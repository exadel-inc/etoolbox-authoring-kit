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

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a list of OSGi configurations known to the system
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own code
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ConfigList {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigList.class);

    @OSGiService
    private ConfigurationAdmin configAdmin;

    @OSGiService
    private MetaTypeService metaTypeService;

    private List<ConfigDefinition> configurations;

    /**
     * Gets the list of configurations known to the system
     * @return List of {@link ConfigDefinition} instances
     */
    public List<ConfigDefinition> getConfigurations() {
        if (configurations != null) {
            return configurations;
        }

        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        if (context == null) {
            LOG.error("BundleContext is not available");
            return Collections.emptyList();
        }

        Map<String, ConfigDefinition> configDefsMap = createConfigDefinitions(context);
        assignExistingConfigs(configDefsMap);

        configurations = configDefsMap.values()
            .stream()
            .sorted(Comparator.comparing(ConfigDefinition::getTitle, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
        return configurations;
    }

    /**
     * Creates configuration definitions by scanning OSGi MetaType information of all bundles in the system
     * @param context The current {@link BundleContext}
     * @return Map of configuration definitions, keyed by PID
     */
    private Map<String, ConfigDefinition> createConfigDefinitions(BundleContext context) {
        Map<String, ConfigDefinition> configDefsMap = new HashMap<>();
        for (Bundle bundle : context.getBundles()) {
            MetaTypeInformation metaTypeInformation = metaTypeService.getMetaTypeInformation(bundle);
            if (metaTypeInformation == null) {
                continue;
            }
            for (String pid : ArrayUtils.nullToEmpty(metaTypeInformation.getPids())) {
                ObjectClassDefinition ocd = metaTypeInformation.getObjectClassDefinition(pid, null);
                ConfigDefinition configDef = ConfigDefinition.from(pid, null, ocd, false);
                configDefsMap.put(pid, configDef);
            }
            for (String factoryPid : ArrayUtils.nullToEmpty(metaTypeInformation.getFactoryPids())) {
                ObjectClassDefinition ocd = metaTypeInformation.getObjectClassDefinition(factoryPid, null);
                ConfigDefinition configDef = ConfigDefinition.from(factoryPid, null, ocd, true);
                configDefsMap.put(factoryPid, configDef);
            }
        }
        return configDefsMap;
    }

    /**
     * Assigns existing configurations to the definitions created by {@link #createConfigDefinitions(BundleContext)}
     * @param configDefsMap Map of configuration definitions, keyed by PID
     */
    private void assignExistingConfigs(Map<String, ConfigDefinition> configDefsMap) {
        Configuration[] listedConfigs = null;
        try {
            listedConfigs = configAdmin.listConfigurations(null);
        } catch (IOException | InvalidSyntaxException e) {
            LOG.error("Could not list existing configurations", e);
            return;
        }
        for (Configuration config : listedConfigs) {
            if (configDefsMap.containsKey(config.getPid())) {
                ConfigDefinition existingConfigDef = configDefsMap.get(config.getPid());
                existingConfigDef.setFactoryPid(config.getFactoryPid());
            } else if (StringUtils.isNotEmpty(config.getFactoryPid())) {
                ConfigDefinition existingFactoryDef = configDefsMap.get(StringUtils.defaultString(config.getFactoryPid()));
                if (existingFactoryDef != null) {
                    existingFactoryDef.addChild(ConfigDefinition.from(
                        config.getPid(),
                        config.getFactoryPid(),
                        null,
                        false));
                } else {
                    LOG.warn(
                        "Factory definition is not found for PID={}, factory PID={}",
                        config.getPid(),
                        config.getFactoryPid());
                    ConfigDefinition missingFactoryDef = ConfigDefinition.from(
                        config.getFactoryPid() + " (missing)",
                        null,
                        null,
                        true);
                    missingFactoryDef.addChild(ConfigDefinition.from(
                        config.getPid(),
                        config.getFactoryPid(),
                        null,
                        false));
                    configDefsMap.put(config.getFactoryPid(), missingFactoryDef);
                }
            } else {
                configDefsMap.put(
                    config.getPid(),
                    ConfigDefinition.from(config.getPid(), null, null, false));
            }
        }
    }
}
