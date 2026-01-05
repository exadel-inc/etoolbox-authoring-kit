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

package com.exadel.aem.toolkit.core.configurator.servlets.form;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigDefinition;

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

    /**
     * Processes GET requests to populate the data source for the {@code EToolbox Configurator} interface
     * @param request The HTTP request
     * @param response The HTTP response
     */
    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) {
        ConfigDefinition config = ConfigDefinition.from(request);
        if (config == null || !config.isValid()) {
            return;
        }
        FieldUtil.processRequest(request, config);
        ValueUtil.processRequest(request, config);
    }
}
