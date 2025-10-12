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

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.ui.components.ExpressionCustomizer;

import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener;
import com.exadel.aem.toolkit.core.configurator.servlets.PermissionUtil;
import com.exadel.aem.toolkit.core.configurator.utils.RequestUtil;

/**
 * Enumerates possible outcomes of a configuration access request
 * <u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own code
 */
public enum ConfigAccess {

    GRANTED(null),

    DISABLED("This tool is disabled by OSGi configuration"),
    FACTORY_CONFIG("Factory configs are currently not supported"),
    INVALID_CONFIG("Configuration is missing or invalid"),
    NO_ACCESS("You don't have access to this feature"),
    NO_CONFIG("No configuration specified"),
    OSGI_FAILURE("Could not acquire OSGi entity");

    private static final Logger LOG = LoggerFactory.getLogger(ConfigAccess.class);

    private static final String KEY = "configAccess";

    private final String error;

    /**
     * Default (instantiation-restricting) constructor
     * @param error Error message; null if access is granted
     */
    ConfigAccess(String error) {
        this.error = error;
    }

    /**
     * Gets the error message associated with this access result
     * @return String value; null if access is granted
     */
    public String getError() {
        return error;
    }

    /**
     * Indicates whether access is granted
     * @return True or false
     */
    public boolean isGranted() {
        return StringUtils.isEmpty(error);
    }

    /**
     * Determines the access result for the current request; caches the result in the request attribute for future
     * reference
     * @param request The current request
     * @return The access result
     */
    public static ConfigAccess from(HttpServletRequest request) {
        if (request == null) {
            return OSGI_FAILURE;
        }

        ExpressionCustomizer customizer = ExpressionCustomizer.from(request);
        ConfigAccess result = (ConfigAccess) customizer.getVariable(KEY);
        if (result != null) {
            return result;
        }
        result = pick(request);
        customizer.setVariable(KEY, result);
        return result;
    }

    /**
     * Determines the access result for the current request
     * @param request The current request
     * @return The access result
     */
    private static ConfigAccess pick(HttpServletRequest request) {
        if (!PermissionUtil.hasModifyPermission(request)) {
            return NO_ACCESS;
        }

        String configId = RequestUtil.getConfigId(request);
        if (StringUtils.isEmpty(configId) && !PermissionUtil.hasGlobalModifyPermission(request)) {
            return NO_CONFIG;
        }

        try {
            BundleContext context = Objects.requireNonNull(FrameworkUtil.getBundle(ConfigAccess.class).getBundleContext());
            ConfigChangeListener listener = Objects.requireNonNull(context.getService(context.getServiceReference(ConfigChangeListener.class)));
            if (!listener.isEnabled()) {;
                return DISABLED;
            }
        } catch (RuntimeException e) {
            LOG.error(OSGI_FAILURE.getError(), e);
            return OSGI_FAILURE;
        }

        ConfigDefinition config = ConfigDefinition.from(request);
        if (!config.isValid()) {
            return INVALID_CONFIG;
        }

        if (config.isFactory()) {
            return FACTORY_CONFIG;
        }

        return GRANTED;
    }
}
