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
package com.exadel.aem.toolkit.core.utils;

import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
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
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.exadel.aem.toolkit.core.AemContextFactory;

public class ResolverUtilTest {

    private static final String FACTORY_ERROR = "Could not obtain ResourceResolverFactory";
    private static final String LOGIN_ERROR = "Test error";
    private static final String OSGI_ERROR = "Not running in an OSGi container";
    private static final String SERVICE_USER = "eak-service";
    private static final String SERVICE_USER_CUSTOM = "custom-service-user";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldCreateResolverWithServiceUser() throws LoginException {
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver expected = Mockito.mock(ResourceResolver.class);
        Mockito.when(factory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER))).thenReturn(expected);

        try (ResourceResolver resultFromFactory = ResolverUtil.newResolver(factory)) {
            assertEquals(expected, resultFromFactory);
        }
        try (ResourceResolver resultFromRequest = ResolverUtil.newResolver(newRequestWithFactory(factory))) {
            assertEquals(expected, resultFromRequest);
        }
    }

    @Test
    public void shouldHandleBlankUser() throws LoginException {
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver expected = Mockito.mock(ResourceResolver.class);
        Mockito.when(factory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER))).thenReturn(expected);

        for (String blankUser : new String[]{null, StringUtils.EMPTY, StringUtils.SPACE}) {
            try (ResourceResolver result = ResolverUtil.newResolver(factory, blankUser)) {
                assertEquals(expected, result);
            }
        }
    }

    @Test
    public void shouldCreateResolverWithCustomUser() throws LoginException {
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver expected = Mockito.mock(ResourceResolver.class);
        Mockito.when(factory.getServiceResourceResolver(
            Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER_CUSTOM))).thenReturn(expected);

        try (ResourceResolver result = ResolverUtil.newResolver(factory, SERVICE_USER_CUSTOM)) {
            assertEquals(expected, result);
        }
    }

    @Test
    public void shouldThrowWhenNotInOsgiContainer() {
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        assertLoginException(factory, "user@some.bundle", OSGI_ERROR);
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
        assertLoginException(factory, SERVICE_USER_CUSTOM, LOGIN_ERROR);
        assertLoginException(newRequestWithFactory(factory), LOGIN_ERROR);
    }

    @Test
    @SuppressWarnings({"resource", "unchecked"})
    public void shouldGetUserFromForeignBundle() throws LoginException {
        ResourceResolver mockResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(mockResolver.getPropertyMap()).thenReturn(new HashMap<>());
        ResourceResolverFactory mockResolverFactory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(mockResolverFactory.getServiceResourceResolver(Mockito.any())).thenReturn(mockResolver);

        ServiceReference<ResourceResolverFactory> mockServiceReference =
            (ServiceReference<ResourceResolverFactory>) Mockito.mock(ServiceReference.class);

        BundleContext mockBundleContext = Mockito.mock(BundleContext.class);
        Mockito
            .when(mockBundleContext.getServiceReference(Mockito.eq(ResourceResolverFactory.class)))
            .thenReturn(mockServiceReference);
        Mockito
            .when(mockBundleContext.getService(Mockito.eq(mockServiceReference)))
            .thenReturn(mockResolverFactory);

        Bundle foreignBundle = Mockito.mock(Bundle.class);
        Mockito.when(foreignBundle.getSymbolicName()).thenReturn("foreign.bundle");
        Mockito.when(foreignBundle.getBundleContext()).thenReturn(mockBundleContext);

        Mockito.when(mockBundleContext.getBundles()).thenReturn(new Bundle[] {context.bundleContext().getBundle(), foreignBundle});

        // Verify that resolver can be obtained for a user associated with the foreign bundle
        try (ResourceResolver resolver = ResolverUtil.newResolver(
            mockResolverFactory,
            "user@foreign.bundle",
            mockBundleContext)) {
            assertNotNull(resolver);
        }

        // Verify that LoginException is thrown when the user is associated with a non-existent bundle
        assertThrows(
            LoginException.class,
            () -> ResolverUtil.newResolver(mockResolverFactory, "user@other.bundle", mockBundleContext));

        // Verify that LoginException is thrown when the user is not associated with any bundle
        Mockito.when(mockResolverFactory.getServiceResourceResolver(Mockito.any())).thenThrow(new LoginException(LOGIN_ERROR));
        assertThrows(
            LoginException.class,
            () -> ResolverUtil.newResolver(mockResolverFactory, "user@foreign.bundle", mockBundleContext));
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
        try (ResourceResolver ignored = ResolverUtil.newResolver(request)) {
            Assert.fail("Expected LoginException");
        } catch (LoginException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertLoginException(ResourceResolverFactory factory, String errorMessage) {
        try (ResourceResolver ignored = ResolverUtil.newResolver(factory)) {
            Assert.fail("Expected LoginException");
        } catch (LoginException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    private static void assertLoginException(ResourceResolverFactory factory, String user, String errorMessage) {
        try (ResourceResolver ignored = ResolverUtil.newResolver(factory, user)) {
            Assert.fail("Expected LoginException");
        } catch (LoginException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }
}

