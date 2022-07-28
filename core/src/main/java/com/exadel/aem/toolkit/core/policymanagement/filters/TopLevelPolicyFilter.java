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

package com.exadel.aem.toolkit.core.policymanagement.filters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentEditConfig;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.policymanagement.models.PageInfo;

/**
 * Implements {@link Filter} in order to plug in the rendering flow of an AEM page in Touch UI edit mode for adding a
 * utility JavaScript method ({@code aekApplyTopLevelPolicy}). this method is involved in defining policies for
 * top-level page containers
 * <p><u>Note</u>: This class is not a part of the public API</p>
 */

@org.osgi.service.component.annotations.Component(
    property = {
        Constants.SERVICE_RANKING + ":Integer=" + Integer.MIN_VALUE,
        EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_COMPONENT,
        "sling.filter.resourceTypes=cq/gui/components/siteadmin/admin/page/winmode",
        EngineConstants.SLING_FILTER_PATTERN + "=/mnt/overlay/wcm/core/content/editor/jcr:content"
    }
)
public class TopLevelPolicyFilter implements Filter {

    private static final String SCRIPT_SOURCE = "policy-management/eakApplyTopLevelPolicy.js";
    private static final String META_TAG_FORMAT = "<meta name=\"cq:template\" content=\"%s\">";
    private static final String SCRIPT_TAG_FORMAT = "<script>\n%s</script>";

    private static final String OPENING_BRACKET_QUOTE = "('";
    private static final String CLOSING_BRACKET_QUOTE = "')";

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // Not implemented
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(
        ServletRequest servletRequest,
        ServletResponse servletResponse,
        FilterChain filterChain)
        throws IOException, ServletException {

        SlingHttpServletRequest request = ((SlingHttpServletRequest) servletRequest);
        ResourceResolver resourceResolver = request.getResourceResolver();

        PageInfo pageInfo = getPageInfo(request);

        String rules = Optional.ofNullable(pageInfo)
            .map(PageInfo::getResourceType)
            .map(resourceResolver::getResource)
            .map(resource -> resource.adaptTo(Component.class))
            .map(Component::getDeclaredChildEditConfig)
            .map(ComponentEditConfig::getListeners)
            .map(map -> map.get(CoreConstants.PN_UPDATE_COMPONENT_LIST))
            .map(TopLevelPolicyFilter::extractListenersJsonObject)
            .orElse(StringUtils.EMPTY);

        String scriptNodeText = StringUtils.isNotBlank(rules) ? getScriptTagText(rules) : StringUtils.EMPTY;

        if (pageInfo != null) {
            servletResponse.getWriter().println(String.format(META_TAG_FORMAT, pageInfo.getTemplate()));
        }

        if (StringUtils.isNotEmpty(scriptNodeText))  {
            servletResponse.getWriter().println(scriptNodeText);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // Not implemented
    }

    /**
     * Retrieves the {@link PageInfo} object that describes the target page of the current request, if there are any
     * @param request {@code SlingHttpServletRequest} instance
     * @return Nullable {@code PageInfo} object
     */
    private static PageInfo getPageInfo(SlingHttpServletRequest request) {
        return Optional.of(request)
            .map(SlingHttpServletRequest::getRequestPathInfo)
            .map(RequestPathInfo::getSuffix)
            .map(suffix -> request.getResourceResolver().resolve(suffix))
            .map(resource -> resource.adaptTo(Page.class))
            .map(Page::getContentResource)
            .map(resource -> resource.adaptTo(PageInfo.class))
            .orElse(null);
    }

    /**
     * Reads the stored script template and retrieves an embeddable {@code <script/>} tag populated with the
     * given policy rules
     * @param rules String value representing policy rules
     * @return String value or null if the bundle resource is not present
     * @throws IOException if reading the bundle resource failed
     */
    private static String getScriptTagText(String rules) throws IOException {
        try (InputStream inputStream = TopLevelPolicyFilter.class.getClassLoader().getResourceAsStream(SCRIPT_SOURCE)) {
            if (inputStream == null) {
                return null;
            }
            String scriptSource = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            return String.format(SCRIPT_TAG_FORMAT, String.format(scriptSource, rules));
        }
    }

    /**
     * Retrieves the stringified JSON describing the listeners for the current container out of the stored JavaScript
     * function. This operation is needed to support the common format of policy definitions
     * @param source Raw policy string
     * @return String value
     */
    private static String extractListenersJsonObject(String source) {
        int startPos = StringUtils.indexOf(source, OPENING_BRACKET_QUOTE) + OPENING_BRACKET_QUOTE.length();
        int endPos = StringUtils.lastIndexOf(source, CLOSING_BRACKET_QUOTE);
        if (startPos >= 0 && endPos > startPos) {
            return StringUtils.substring(source, startPos, endPos);
        }
        return source;
    }
}
