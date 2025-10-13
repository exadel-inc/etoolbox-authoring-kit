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

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

@RunWith(MockitoJUnitRunner.class)
public class PermissionUtilTest {

    private static final String CONFIG = "my.config";

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    @Test
    public void shouldCheckGlobalPermission() throws Exception {
        try (ResourceResolver resolver = createResourceResolver(true, true)) {
            MockSlingHttpServletRequest request = createRequest(resolver);
            assertTrue(PermissionUtil.hasGlobalModifyPermission(request));
        }

        try (ResourceResolver resolver = createResourceResolver(false, false)) {
            MockSlingHttpServletRequest request = createRequest(resolver);
            assertFalse(PermissionUtil.hasGlobalModifyPermission(request));
        }

        try (ResourceResolver resolver = createResourceResolver(true, false)) {
            MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
            ((MockRequestPathInfo) request.getRequestPathInfo()).setSuffix(null);
            assertTrue(PermissionUtil.hasGlobalModifyPermission(request));
        }

        ResourceResolver mockResolver = Mockito.mock(ResourceResolver.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockResolver.adaptTo(Mockito.eq(Session.class))).thenReturn(mockSession);
        Mockito.when(mockSession.hasPermission(Mockito.eq(ConfiguratorConstants.ROOT_PATH), Mockito.eq(Session.ACTION_SET_PROPERTY)))
            .thenThrow(new RepositoryException("forced"));
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(mockResolver, context.bundleContext());
        assertFalse(PermissionUtil.hasGlobalModifyPermission(request));
    }

    @Test
    public void shouldCheckLocalPermission() throws Exception {
        try (ResourceResolver resolver = createResourceResolver(true, true)) {
            MockSlingHttpServletRequest request = createRequest(resolver);
            assertTrue(PermissionUtil.hasModifyPermission(request));
        }

        try (ResourceResolver resolver = createResourceResolver(true, false)) {
            MockSlingHttpServletRequest request = createRequest(resolver);
            assertFalse(PermissionUtil.hasModifyPermission(request));
        }

        try (ResourceResolver resolver = createResourceResolver(false, true)) {
            MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
            ((MockRequestPathInfo) request.getRequestPathInfo()).setSuffix(null);
            assertFalse(PermissionUtil.hasModifyPermission(request));
        }

        ResourceResolver mockResolver = Mockito.mock(ResourceResolver.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockResolver.adaptTo(Mockito.eq(Session.class))).thenReturn(mockSession);
        Mockito.when(mockSession.hasPermission(Mockito.eq(ConfiguratorConstants.ROOT_PATH), Mockito.eq(Session.ACTION_SET_PROPERTY)))
            .thenThrow(new RepositoryException("forced"));
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(mockResolver, context.bundleContext());
        assertFalse(PermissionUtil.hasModifyPermission(request));
    }

    @Test
    public void shouldCheckOverridingPermissions() throws Exception {
        String configId = CONFIG;
        context.requestPathInfo().setSuffix(configId);

        Privilege read = Mockito.mock(Privilege.class);
        Privilege write = Mockito.mock(Privilege.class);
        Privilege[] rootPrivileges = new Privilege[]{read};
        Privilege[] configPrivileges = new Privilege[]{read, write};

        AccessControlManager acm = Mockito.mock(AccessControlManager.class);
        Mockito.when(acm.getPrivileges(Mockito.eq(ConfiguratorConstants.ROOT_PATH))).thenReturn(rootPrivileges);
        Mockito
            .when(acm.getPrivileges(Mockito.eq(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId)))
            .thenReturn(configPrivileges);

        try (ResourceResolver resolver = createResourceResolver(false, true)) {
            MockSlingHttpServletRequest request = createRequest(resolver);
            Session session = resolver.adaptTo(Session.class);
            assertNotNull(session);
            Mockito.when(session.getAccessControlManager()).thenReturn(acm);

            assertTrue(PermissionUtil.hasOverridingPermissions(request));

            rootPrivileges = new Privilege[]{read, write};
            Mockito.when(acm.getPrivileges(Mockito.eq(ConfiguratorConstants.ROOT_PATH))).thenReturn(rootPrivileges);
            assertFalse(PermissionUtil.hasOverridingPermissions(request));

            Mockito.when(acm.getPrivileges(Mockito.anyString())).thenThrow(new PathNotFoundException("forced"));
            assertFalse(PermissionUtil.hasOverridingPermissions(request));

            ((MockRequestPathInfo) request.getRequestPathInfo()).setSuffix(null);
            assertFalse(PermissionUtil.hasOverridingPermissions(request));
        }
    }

    @Test
    public void shouldCheckReplicatePermission() throws Exception {
        context.requestPathInfo().setSuffix(CONFIG);

        Privilege replicatePrivilege = Mockito.mock(Privilege.class);
        Mockito.when(replicatePrivilege.getName()).thenReturn("crx:replicate");
        Privilege jcrAllPrivilege = Mockito.mock(Privilege.class);
        Mockito.when(jcrAllPrivilege.getName()).thenReturn(Privilege.JCR_ALL);

        AccessControlManager acm = Mockito.mock(AccessControlManager.class);

        try (ResourceResolver resolver = createResourceResolver(false, true)) {
            MockSlingHttpServletRequest request = createRequest(resolver);
            Session session = resolver.adaptTo(Session.class);
            assertNotNull(session);
            Mockito.when(session.getAccessControlManager()).thenReturn(acm);

            Privilege[] userPrivileges = new Privilege[]{replicatePrivilege};
            Mockito.when(acm.getPrivileges(Mockito.startsWith(ConfiguratorConstants.ROOT_PATH))).thenReturn(userPrivileges);
            assertTrue(PermissionUtil.hasReplicatePermission(request));

            userPrivileges = new Privilege[]{jcrAllPrivilege};
            Mockito.when(acm.getPrivileges(Mockito.startsWith(ConfiguratorConstants.ROOT_PATH))).thenReturn(userPrivileges);
            assertTrue(PermissionUtil.hasReplicatePermission(request));

            Privilege readPrivilege = Mockito.mock(Privilege.class);
            Mockito.when(readPrivilege.getName()).thenReturn(Privilege.JCR_READ);
            userPrivileges = new Privilege[]{readPrivilege};
            Mockito.when(acm.getPrivileges(Mockito.startsWith(ConfiguratorConstants.ROOT_PATH))).thenReturn(userPrivileges);
            assertFalse(PermissionUtil.hasReplicatePermission(request));

            Mockito.when(acm.getPrivileges(Mockito.anyString())).thenThrow(new RepositoryException("forced"));
            assertFalse(PermissionUtil.hasReplicatePermission(request));

            ((MockRequestPathInfo) request.getRequestPathInfo()).setSuffix(null);
            assertFalse(PermissionUtil.hasReplicatePermission(request));
        }
    }

    private MockSlingHttpServletRequest createRequest(ResourceResolver resolver) {
        MockSlingHttpServletRequest request = new MockSlingHttpServletRequest(resolver, context.bundleContext());
        ((MockRequestPathInfo) request.getRequestPathInfo()).setSuffix(CONFIG);
        return request;
    }

    private ResourceResolver createResourceResolver(
        boolean canWriteToRoot,
        boolean canWriteToConfig) throws RepositoryException {
        Session session = Mockito.mock(Session.class);
        Mockito
            .when(session.hasPermission(Mockito.eq(ConfiguratorConstants.ROOT_PATH), Mockito.eq(Session.ACTION_SET_PROPERTY)))
            .thenReturn(canWriteToRoot);
        Mockito
            .when(session.hasPermission(Mockito.eq(
                    ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + CONFIG),
                Mockito.eq(Session.ACTION_SET_PROPERTY)))
            .thenReturn(canWriteToConfig);

        ResourceResolver resolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(resolver.adaptTo(Mockito.eq(Session.class))).thenReturn(session);
        return resolver;
    }
}
