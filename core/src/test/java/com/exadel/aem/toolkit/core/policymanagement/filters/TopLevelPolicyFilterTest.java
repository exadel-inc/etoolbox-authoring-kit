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
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import com.google.common.collect.ImmutableMap;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentEditConfig;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;

public class TopLevelPolicyFilterTest {

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    private TopLevelPolicyFilter topLevelPolicyFilter;
    private MockFilterChain filterChain;

    @Before
    public void setUp() {
        context.load().json("/com/exadel/aem/toolkit/core/policymanagement/apps.json","/apps");
        context.load().json("/com/exadel/aem/toolkit/core/policymanagement/content.json","/content");

        topLevelPolicyFilter = context.registerInjectActivateService(new TopLevelPolicyFilter());
        filterChain = context.registerService(new MockFilterChain());

        context.registerAdapter(Resource.class, Component.class, getMockComponent());
    }

    @Test
    public void shouldInjectMetaAndScript() throws ServletException, IOException {
        context.requestPathInfo().setResourcePath("/libs/cq/gui/content/editor");
        context.requestPathInfo().setSuffix("/content/acme/page");

        topLevelPolicyFilter.doFilter(context.request(), context.response(), filterChain);
        assertTrue(StringUtils.contains(context.response().getOutputAsString(), "<meta name=\"cq:template\""));
        assertTrue(StringUtils.contains(context.response().getOutputAsString(), "window.eakApplyTopLevelPolicy"));
    }

    private Component getMockComponent() {
        Component mockComponent = Mockito.mock(Component.class);
        ComponentEditConfig mockEditConfig = Mockito.mock(ComponentEditConfig.class);
        Resource listenersNode = Objects.requireNonNull(context.resourceResolver().getResource("/apps/acme/components/pages/generic/cq:childEditConfig/listeners"));
        Mockito.when(mockComponent.getDeclaredChildEditConfig()).thenReturn(mockEditConfig);
        Mockito.when(mockEditConfig.getListeners()).thenReturn(ImmutableMap.of(
            CoreConstants.PN_UPDATE_COMPONENT_LIST,
            listenersNode.getValueMap().get(CoreConstants.PN_UPDATE_COMPONENT_LIST, StringUtils.EMPTY)
        ));
        return mockComponent;
    }

    private static class MockFilterChain implements FilterChain {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            // Not implemented
        }
    }
}
