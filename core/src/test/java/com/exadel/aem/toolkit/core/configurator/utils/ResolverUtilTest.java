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

import java.util.Collections;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

import com.exadel.aem.toolkit.core.AemContextFactory;

@RunWith(MockitoJUnitRunner.class)
public class ResolverUtilTest {

    private static final String USER_NAME = "testServiceUser";

    private static final String FACTORY_ERROR = "Could not obtain ResourceResolverFactory";
    private static final String LOGIN_ERROR = "Test error";

    @Rule
    public AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldCreateResolverWithServiceUser() throws LoginException {
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver expected = Mockito.mock(ResourceResolver.class);
        Mockito.when(factory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, USER_NAME))).thenReturn(expected);

        ResourceResolver resultFromFactory = ResolverUtil.newResolver(factory, USER_NAME);
        assertEquals(expected, resultFromFactory);

        ResourceResolver resultFromRequest = ResolverUtil.newResolver(newRequestWithFactory(factory), USER_NAME);
        assertEquals(expected, resultFromRequest);
    }

    @Test
    public void shouldThrowWhenDependenciesUnavailable() {
        MockSlingHttpServletRequest requestNullBindings =
            new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        requestNullBindings.setAttribute(SlingBindings.class.getName(), null);
        assertLoginException(requestNullBindings, FACTORY_ERROR);

        SlingBindings noScriptHelper = new SlingBindings();
        MockSlingHttpServletRequest requestNoHelper =
            new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        requestNoHelper.setAttribute(SlingBindings.class.getName(), noScriptHelper);
        assertLoginException(requestNoHelper, FACTORY_ERROR);

        SlingScriptHelper scriptHelper = Mockito.mock(SlingScriptHelper.class);
        Mockito.when(scriptHelper.getService(ResourceResolverFactory.class)).thenReturn(null);
        SlingBindings noFactory = new SlingBindings();
        noFactory.put(SlingBindings.SLING, scriptHelper);
        MockSlingHttpServletRequest requestNoFactory =
            new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        requestNoFactory.setAttribute(SlingBindings.class.getName(), noFactory);
        assertLoginException(requestNoFactory, FACTORY_ERROR);
    }

    @Test
    public void shouldThrowOnLoginException() throws LoginException {
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(factory.getServiceResourceResolver(Mockito.anyMap())).thenThrow(new LoginException(LOGIN_ERROR));
        assertLoginException(factory, LOGIN_ERROR);
        assertLoginException(newRequestWithFactory(factory), LOGIN_ERROR);
    }

    private MockSlingHttpServletRequest newRequestWithFactory(ResourceResolverFactory factory) {
        SlingScriptHelper scriptHelper = Mockito.mock(SlingScriptHelper.class);
        Mockito.when(scriptHelper.getService(ResourceResolverFactory.class)).thenReturn(factory);
        SlingBindings bindings = new SlingBindings();
        bindings.put(SlingBindings.SLING, scriptHelper);
        MockSlingHttpServletRequest request =
            new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        request.setAttribute(SlingBindings.class.getName(), bindings);
        return request;
    }

    private static void assertLoginException(SlingHttpServletRequest request, String errorMessage) {
        try (ResourceResolver ignored = ResolverUtil.newResolver(request, USER_NAME)) {
            Assert.fail("Expected LoginException");
        } catch (LoginException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertLoginException(ResourceResolverFactory factory, String errorMessage) {
        try (ResourceResolver ignored = ResolverUtil.newResolver(factory, USER_NAME)) {
            Assert.fail("Expected LoginException");
        } catch (LoginException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }
}

