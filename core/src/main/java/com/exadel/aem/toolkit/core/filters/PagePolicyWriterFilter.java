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

package com.exadel.aem.toolkit.core.filters;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentEditConfig;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.EngineConstants;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.osgi.framework.Constants;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

@org.osgi.service.component.annotations.Component(
        property = {
                Constants.SERVICE_RANKING + ":Integer=-2147483648",
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_COMPONENT,
                "sling.filter.resourceTypes" + "=" + "cq/gui/components/siteadmin/admin/page/winmode",
                EngineConstants.SLING_FILTER_PATTERN + "=" + "/editor.*"
        }
)
public class PagePolicyWriterFilter implements Filter {

    private static final String META_TAG_FORMAT = "<meta name=\"cq:template\" content=\"%s\">";
    private static final int LENGTH_TO_TRIM = "Granite.PolicyResolver.build('".length();
    private static final String SCRIPT_FORMAT = "<script>%s</script>";

    private static final String MOCK = "Granite.PolicyResolver.build('{\"isEditConfig\":false,\"rules\":[{\"value\":[\"hpeweb/components/content/design3/myAccount/subscriptionItem\"],\"containers\":[\"header-nav\",\"secondary-nav\"]}]}"
            .substring(LENGTH_TO_TRIM);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        SlingHttpServletRequest request = ((SlingHttpServletRequest) servletRequest);
        ResourceResolver resourceResolver = request.getResourceResolver();

        PageModel pageModel = Optional.of(request)
                .map(SlingHttpServletRequest::getRequestPathInfo)
                .map(RequestPathInfo::getSuffix)
                .map(resourceResolver::resolve)
                .map(res -> res.adaptTo(Page.class))
                .map(Page::getContentResource)
                .map(res -> res.adaptTo(PageModel.class))
                .orElse(null);

        if (pageModel == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        servletResponse.getWriter().write(String.format(META_TAG_FORMAT, pageModel.getTemplate()));

        String rules = Optional.of(pageModel)
                .map(PageModel::getResourceType)
                .map(resourceResolver::getResource)
                .map(res -> res.adaptTo(Component.class))
                .map(Component::getDeclaredChildEditConfig)
                .map(ComponentEditConfig::getListeners)
                .map(map -> map.get("updatecomponentlist"))
                .map(listener -> listener.substring(LENGTH_TO_TRIM, listener.length() - 2))
                .orElse(MOCK);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("script.js");
        if (inputStream == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String script = String.format(SCRIPT_FORMAT, new BufferedReader(
                new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining(System.lineSeparator())));

        servletResponse.getWriter().write(String.format(script, rules));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    @Model(adaptables = Resource.class)
    public static class PageModel {

        @ValueMapValue(name = "cq:template")
        private String template;

        @ValueMapValue(name = JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY)
        private String resourceType;

        public String getTemplate() {
            return this.template;
        }

        public String getResourceType() {
            return this.resourceType;
        }
    }
}
