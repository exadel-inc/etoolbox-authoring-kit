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
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        String configId = StringUtils.strip(request.getRequestPathInfo().getSuffix(), CoreConstants.SEPARATOR_SLASH);
        if (StringUtils.isBlank(configId)) {
            Resource message = FieldUtil.newAlert(
                request.getResourceResolver(),
                "No configuration specified",
                "error");
            request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(Collections.singletonList(message).iterator()));
            return;
        }

        if (!configChangeListener.isEnabled()) {
            Resource message = FieldUtil.newAlert(
                request.getResourceResolver(),
                "This tool is disabled by OSGi configuration",
                "error");
            request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(Collections.singletonList(message).iterator()));
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
            Resource message = FieldUtil.newAlert(
                request.getResourceResolver(),
                "Configuration \"" + configId + "\" is missing or invalid",
                "error");
            request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(Collections.singletonList(message).iterator()));
            return;
        }
        if (config.isFactory()) {
            Resource message = FieldUtil.newAlert(
                request.getResourceResolver(),
                "Factory configs are currently not supported",
                "warning");
            request.setAttribute(
                DataSource.class.getName(),
                new SimpleDataSource(Collections.singletonList(message).iterator()));
            return;
        }

        Resource existingConfig = request
            .getResourceResolver()
            .getResource(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId);
        config.setModified(existingConfig != null);

        boolean isPublished = existingConfig != null
            && existingConfig.getValueMap().get("cq:lastReplicationAction", StringUtils.EMPTY).equals("Activate");
        config.setPublished(isPublished);

        FieldUtil.processRequest(request, config);
        ValueUtil.processRequest(request, config);
    }
}
