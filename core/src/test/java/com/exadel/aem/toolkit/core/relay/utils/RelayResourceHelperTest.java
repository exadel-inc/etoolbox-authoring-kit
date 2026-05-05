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
package com.exadel.aem.toolkit.core.relay.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.relay.models.RelayResource;

public class RelayResourceHelperTest {

    private static final String PATH_TARGET = "/content/target";
    private static final String PATH_CHILD = "/child";
    private static final String PATH_EXPOSED = "/content/exposed";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldResolveResource() {
        context.create().resource(PATH_TARGET);
        ResourceResolver resolver = context.resourceResolver();
        ResourceResolverFactory resolverFactory = context.getService(ResourceResolverFactory.class);

        assertNotNull(resolverFactory);

        // userId matches resolver's own ID → uses original resolver
        Resource sameUserResult = RelayResourceHelper.getResource(resolver, resolverFactory, resolver.getUserID(), PATH_TARGET);
        assertNotNull(sameUserResult);
        assertEquals(PATH_TARGET, sameUserResult.getPath());

        // userId is null → no subsidiary resolver lookup; uses original resolver
        Resource nullUserResult = RelayResourceHelper.getResource(resolver, resolverFactory, null, PATH_TARGET);
        assertNotNull(nullUserResult);
        assertEquals(PATH_TARGET, nullUserResult.getPath());

        // resource not found → null
        Resource missingTarget = RelayResourceHelper.getResource(resolver, resolverFactory, resolver.getUserID(), PATH_TARGET + "/missing");
        assertNull(missingTarget);
    }

    @Test
    public void shouldTrackSubsidiaryResolver() {
        Map<String, Object> propertyMap = new HashMap<>();
        ResourceResolver resolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(resolver.getPropertyMap()).thenReturn(propertyMap);

        ResourceResolverFactory resolverFactory = context.getService(ResourceResolverFactory.class);
        assertNotNull(resolverFactory);

        RelayResourceHelper.getResource(resolver, resolverFactory, "modified-user", PATH_TARGET);

        Object stored = propertyMap.get(ResourceResolver.class.getName() + "@modified-user");
        assertTrue(stored instanceof ResourceResolver);
    }

    @Test
    public void shouldReuseExistingSubsidiaryResolver() {
        Map<String, Object> propertyMap = new HashMap<>();
        ResourceResolver resolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(resolver.getPropertyMap()).thenReturn(propertyMap);

        ResourceResolverFactory resolverFactory = context.getService(ResourceResolverFactory.class);
        assertNotNull(resolverFactory);

        RelayResourceHelper.getResource(resolver, resolverFactory, "modified-user", PATH_TARGET);
        Object firstResolver = propertyMap.get(ResourceResolver.class.getName() + "@modified-user");

        RelayResourceHelper.getResource(resolver, resolverFactory, "modified-user", PATH_TARGET);
        Object secondResolver = propertyMap.get(ResourceResolver.class.getName() + "@modified-user");

        assertSame(firstResolver, secondResolver);
    }

    @Test
    public void shouldStoreSentinelWhenLoginFails() throws LoginException {
        Map<String, Object> propertyMap = new HashMap<>();
        ResourceResolver resolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(resolver.getPropertyMap()).thenReturn(propertyMap);

        ResourceResolverFactory resolverFactory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(resolverFactory.getServiceResourceResolver(Mockito.any()))
            .thenThrow(new LoginException("Simulated login failure"));

        // First call: LoginException → sentinel stored; original resolver used as fallback
        RelayResourceHelper.getResource(resolver, resolverFactory, "blocked-user", PATH_TARGET);

        Object stored = propertyMap.get(ResourceResolver.class.getName() + "@blocked-user");
        assertNotNull(stored);
        assertFalse(stored instanceof ResourceResolver); // sentinel, not a resolver
        Mockito.verify(resolverFactory, Mockito.times(1)).getServiceResourceResolver(Mockito.any());

        // Second call with same userId: sentinel already in map, factory not called again
        RelayResourceHelper.getResource(resolver, resolverFactory, "blocked-user", PATH_TARGET);
        Mockito.verify(resolverFactory, Mockito.times(1)).getServiceResourceResolver(Mockito.any());
    }

    @Test
    public void shouldDelegateToParentProvider() {
        Resource fallback = context.create().resource(PATH_TARGET + PATH_CHILD);
        ResourceProvider<Void> mockProvider = newMockProvider();
        ResolveContext<Void> parentCtx = newMockResolveContext();
        Mockito.when(mockProvider.getResource(Mockito.same(parentCtx), Mockito.eq(PATH_TARGET + PATH_CHILD), Mockito.any(), Mockito.isNull()))
            .thenReturn(fallback);

        ResolveContext<Void> resolveContext = newMockResolveContext();
        Mockito.doReturn(mockProvider).when(resolveContext).getParentResourceProvider();
        Mockito.doReturn(parentCtx).when(resolveContext).getParentResolveContext();

        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        Resource result = RelayResourceHelper.getResource(resolveContext, PATH_TARGET + PATH_CHILD, resourceContext, null);

        assertNotNull(result);
        assertEquals(PATH_TARGET + PATH_CHILD, result.getPath());
        Mockito.verify(mockProvider).getResource(Mockito.same(parentCtx), Mockito.eq(PATH_TARGET + PATH_CHILD), Mockito.same(resourceContext), Mockito.isNull());
    }

    @Test
    public void shouldReturnNullWithoutContext() {
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        // null resolveContext
        assertNull(RelayResourceHelper.getResource(null, PATH_TARGET, resourceContext, null));

        // missing parent resource provider
        ResolveContext<Void> noProvider = newMockResolveContext();
        Mockito.doReturn(null).when(noProvider).getParentResourceProvider();
        Mockito.doReturn(newMockResolveContext()).when(noProvider).getParentResolveContext();
        assertNull(RelayResourceHelper.getResource(noProvider, PATH_TARGET, resourceContext, null));

        // missing parent resolve context
        ResolveContext<Void> noParentCtx = newMockResolveContext();
        Mockito.doReturn(newMockProvider()).when(noParentCtx).getParentResourceProvider();
        Mockito.doReturn(null).when(noParentCtx).getParentResolveContext();
        assertNull(RelayResourceHelper.getResource(noParentCtx, PATH_TARGET, resourceContext, null));
    }

    @Test
    public void shouldListChildrenWithOverriddenPath() {
        Resource parent = context.create().resource(PATH_TARGET);
        context.create().resource(PATH_TARGET + PATH_CHILD);

        Iterator<Resource> children = RelayResourceHelper.listChildren(parent, PATH_EXPOSED);

        assertNotNull(children);
        assertTrue(children.hasNext());
        Resource child = children.next();
        assertEquals(PATH_EXPOSED + PATH_CHILD, child.getPath());
        assertTrue(child instanceof RelayResource);
        assertFalse(children.hasNext());
    }

    @Test
    public void shouldListChildrenOfUnwrappedRelay() {
        Resource original = context.create().resource(PATH_TARGET);
        context.create().resource(PATH_TARGET + PATH_CHILD);
        RelayResource relay = new RelayResource(original, PATH_EXPOSED);

        // target is a RelayResource → helper must unwrap and use the underlying resource's children
        Iterator<Resource> children = RelayResourceHelper.listChildren(relay, PATH_EXPOSED);

        assertNotNull(children);
        assertTrue(children.hasNext());
        Resource child = children.next();
        assertEquals(PATH_EXPOSED + PATH_CHILD, child.getPath());
        assertTrue(child instanceof RelayResource);
        assertFalse(children.hasNext());
    }

    @Test
    public void shouldDelegateListChildrenToParent() {
        Resource parent = context.create().resource(PATH_TARGET);
        Resource expectedChild = context.create().resource(PATH_TARGET + PATH_CHILD);

        ResourceProvider<Void> mockProvider = newMockProvider();
        ResolveContext<Void> parentCtx = newMockResolveContext();
        Mockito.when(mockProvider.listChildren(Mockito.same(parentCtx), Mockito.same(parent)))
            .thenReturn(Collections.singletonList(expectedChild).iterator());

        ResolveContext<Void> resolveContext = newMockResolveContext();
        Mockito.doReturn(mockProvider).when(resolveContext).getParentResourceProvider();
        Mockito.doReturn(parentCtx).when(resolveContext).getParentResolveContext();

        Iterator<Resource> children = RelayResourceHelper.listChildren(resolveContext, parent);

        assertNotNull(children);
        assertTrue(children.hasNext());
        assertEquals(PATH_TARGET + PATH_CHILD, children.next().getPath());
        Mockito.verify(mockProvider).listChildren(Mockito.same(parentCtx), Mockito.same(parent));
    }

    @Test
    public void shouldReturnNullChildrenWithoutContext() {
        Resource parent = context.create().resource(PATH_TARGET);

        // null resolveContext
        assertNull(RelayResourceHelper.listChildren(null, parent));

        // missing parent resource provider
        ResolveContext<Void> noProvider = newMockResolveContext();
        Mockito.doReturn(null).when(noProvider).getParentResourceProvider();
        Mockito.doReturn(newMockResolveContext()).when(noProvider).getParentResolveContext();
        assertNull(RelayResourceHelper.listChildren(noProvider, parent));

        // missing parent resolve context
        ResolveContext<Void> noParentCtx = newMockResolveContext();
        Mockito.doReturn(newMockProvider()).when(noParentCtx).getParentResourceProvider();
        Mockito.doReturn(null).when(noParentCtx).getParentResolveContext();
        assertNull(RelayResourceHelper.listChildren(noParentCtx, parent));
    }

    /* ---------------
       Utility methods
       --------------- */

    @SuppressWarnings("unchecked")
    private static ResourceProvider<Void> newMockProvider() {
        return Mockito.mock(ResourceProvider.class);
    }

    @SuppressWarnings("unchecked")
    private static ResolveContext<Void> newMockResolveContext() {
        return Mockito.mock(ResolveContext.class);
    }
}
