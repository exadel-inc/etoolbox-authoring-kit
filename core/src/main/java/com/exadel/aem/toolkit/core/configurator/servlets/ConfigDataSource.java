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

import java.io.IOException;
import java.util.Collections;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.MetaTypeService;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;
import com.exadel.aem.toolkit.core.configurator.models.ConfigDefinition;
import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener;

/**
 * Implements a servlet that provides data source for the {@code EToolbox Configurator} interface
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.resourceTypes=" + "/bin/etoolbox/authoring-kit/configurator",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    }
)
public class ConfigDataSource extends SlingSafeMethodsServlet {

    private static final String VARIANT_ERROR = "error";
    private static final String VARIANT_WARNING = "warning";

    @Reference
    private transient ConfigurationAdmin configurationAdmin;

    @Reference
    private transient MetaTypeService metaTypeService;

    @Reference
    private transient ConfigChangeListener configChangeListener;

    private BundleContext bundleContext;

    /**
     * Activates this OSGi component and saves the {@link BundleContext} object for further use
     * @param context The {@code BundleContext} instance provided by the OSGi framework
     */
    @Activate
    private void activate(BundleContext context) {
        this.bundleContext = context;
    }

    /**
     * Processes GET requests to populate the data source for the {@code EToolbox Configurator} interface
     * @param request The HTTP request
     * @param response The HTTP response
     */
    @Override
    protected void doGet(
        @NotNull SlingHttpServletRequest request,
        @NotNull SlingHttpServletResponse response) throws IOException {

        if (!PermissionUtil.hasModifyPermission(request)) {
            outputMessage(request, "You don't have access to this feature", VARIANT_ERROR);
            return;
        }

        String configId = StringUtils.strip(request.getRequestPathInfo().getSuffix(), CoreConstants.SEPARATOR_SLASH);
        if (StringUtils.isBlank(configId)) {
            outputMessage(request, "No configuration specified", VARIANT_ERROR);
            return;
        }

        if (!configChangeListener.isEnabled()) {
            outputMessage(request, "This tool is disabled by OSGi configuration", VARIANT_ERROR);
            return;
        }

        ConfigDefinition config = ConfigHelper
            .builder()
            .bundleContext(bundleContext)
            .configurationAdmin(configurationAdmin)
            .metaTypeService(metaTypeService)
            .build()
            .getConfig(configId);

        if (config == null) {
            outputMessage(request, "Configuration \"" + configId + "\" is missing or invalid", VARIANT_ERROR);
            return;
        }
        if (config.isFactory()) {
            outputMessage(request, "Factory configs are currently not supported", VARIANT_WARNING);
            return;
        }

        Resource existingConfig = request
            .getResourceResolver()
            .getResource(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId);
        Resource existingConfigData = existingConfig != null
            ? existingConfig.getChild(ConfiguratorConstants.NN_DATA)
            : null;
        config.setModified(existingConfigData != null);

        boolean isPublished = existingConfig != null
            && existingConfig.getValueMap().get(ConfiguratorConstants.PN_REPLICATION_ACTION, StringUtils.EMPTY).equals("Activate");
        config.setPublished(isPublished);

        FieldUtil.processRequest(request, config);
        ValueUtil.processRequest(request, config);
    }

    /**
     * Prints out an alert message
     * @param request The HTTP request
     * @param message The message text. A non-blank string is expected
     * @param variant The message variant
     */
    private static void outputMessage(SlingHttpServletRequest request, String message, String variant) {
        Resource messageResource = FieldUtil.newAlert(request.getResourceResolver(), message, variant);
        request.setAttribute(
            DataSource.class.getName(),
            new SimpleDataSource(Collections.singletonList(messageResource).iterator()));
    }
}
