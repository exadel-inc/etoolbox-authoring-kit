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
package com.exadel.aem.toolkit.core.configurator.utils;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;

public class RequestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RequestUtil.class);

    private RequestUtil() {}

    public static ResourceResolver getResourceResolver(HttpServletRequest request) {
        if (request instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) request).getResourceResolver();
        }
        SlingBindings slingBindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        if (slingBindings == null) {
            LOG.warn("Invalid request: no Sling bindings found");
            return null;
        }
        ResourceResolver result = slingBindings.getResourceResolver();
        if (result == null) {
            LOG.warn("Invalid request: no ResourceResolver found in Sling bindings");
            return null;
        }
        return result;
    }

    public static Session getSession(HttpServletRequest request) {
        ResourceResolver resolver = getResourceResolver(request);
        return resolver != null ? resolver.adaptTo(Session.class) : null;
    }

    public static String getConfigId(HttpServletRequest request) {
        return request instanceof SlingHttpServletRequest
            ? StringUtils.strip(((SlingHttpServletRequest) request).getRequestPathInfo().getSuffix(), CoreConstants.SEPARATOR_SLASH)
            : StringUtils.substringAfterLast(request.getRequestURI(), ".html/");
    }

}
