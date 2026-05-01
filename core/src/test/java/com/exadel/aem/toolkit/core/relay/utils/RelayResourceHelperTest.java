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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.relay.models.RelayResource;

@RunWith(MockitoJUnitRunner.class)
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

        // Resource found → returned directly
        Resource found = RelayResourceHelper.getResource(
            resolver,
            UnaryOperator.identity(),
            PATH_TARGET);
        assertNotNull(found);
        assertEquals(PATH_TARGET, found.getPath());

        // Resource not found → null
        Resource notFound = RelayResourceHelper.getResource(
            resolver,
            UnaryOperator.identity(),
            PATH_TARGET + "/missing");
        assertNull(notFound);
    }

    @Test
    public void shouldTrackSubsidiaryResolver() {
        Resource targetResource = context.create().resource(PATH_TARGET);

        ResourceResolver modifiedResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(modifiedResolver.getResource(PATH_TARGET)).thenReturn(targetResource);
        Mockito.when(modifiedResolver.getUserID()).thenReturn("modified-user");

        Map<String, Object> propertyMap = new HashMap<>();
        ResourceResolver basicResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(basicResolver.getPropertyMap()).thenReturn(propertyMap);

        // Modified resolver → stored as subsidiary in the basic resolver's property map
        RelayResourceHelper.getResource(
            basicResolver,
            resolver -> modifiedResolver,
            PATH_TARGET);
        assertTrue(propertyMap.get("subsidiary") instanceof RelayResourceHelper.ResolverHolder);

        // Second call with a different modified resolver → existing subsidiary is closed, new one stored
        ResourceResolver nextResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(nextResolver.getResource(PATH_TARGET)).thenReturn(targetResource);
        Mockito.when(nextResolver.getUserID()).thenReturn("next-user");

        RelayResourceHelper.getResource(
            basicResolver,
            resolver -> nextResolver,
            PATH_TARGET);
        Mockito.verify(modifiedResolver).close();
        assertTrue(propertyMap.get(RelayResourceHelper.KEY_SUBSIDIARY) instanceof RelayResourceHelper.ResolverHolder);
    }

    @Test
    public void shouldHandleConcurrentSubsidiaryAccess() throws InterruptedException {
        Resource targetResource = context.create().resource(PATH_TARGET);

        int threadCount = 8;
        Map<String, Object> propertyMap = new ConcurrentHashMap<>();
        ResourceResolver basicResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(basicResolver.getPropertyMap()).thenReturn(propertyMap);

        AtomicInteger closedCount = new AtomicInteger();
        ResourceResolver[] resolvers = new ResourceResolver[threadCount];
        for (int i = 0; i < threadCount; i++) {
            ResourceResolver mock = Mockito.mock(ResourceResolver.class);
            Mockito.when(mock.getResource(PATH_TARGET)).thenReturn(targetResource);
            Mockito.when(mock.getUserID()).thenReturn("user-" + i);
            Mockito.doAnswer(inv -> {
                closedCount.incrementAndGet();
                return null;
            }).when(mock).close();
            resolvers[i] = mock;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        for (ResourceResolver resolver : resolvers) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                RelayResourceHelper.getResource(
                    basicResolver,
                    r -> resolver,
                    PATH_TARGET);
            });
        }
        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertNotNull(propertyMap.get(RelayResourceHelper.KEY_SUBSIDIARY));
        assertEquals(threadCount - 1, closedCount.get());
    }

    @Test
    public void shouldDelegateGetResourceToParentProvider() {
        Resource targetResource = context.create().resource(PATH_TARGET);

        ResourceProvider<Void> mockProvider = newMockProvider();
        ResolveContext<Void> mockParentContext = newMockResolveContext();
        ResourceContext mockResourceContext = Mockito.mock(ResourceContext.class);
        Mockito.when(mockProvider.getResource(Mockito.any(), Mockito.eq(PATH_TARGET), Mockito.any(), Mockito.any()))
            .thenReturn(targetResource);

        ResolveContext<Void> mockResolveContext = newMockResolveContext();
        Mockito.doReturn(mockProvider).when(mockResolveContext).getParentResourceProvider();
        Mockito.doReturn(mockParentContext).when(mockResolveContext).getParentResolveContext();

        // Happy path → delegates to parent provider
        Resource result = RelayResourceHelper.getResource(
            mockResolveContext,
            PATH_TARGET,
            mockResourceContext,
            null);
        assertNotNull(result);
        assertEquals(PATH_TARGET, result.getPath());

        // Null context → null
        assertNull(RelayResourceHelper.getResource(null, PATH_TARGET, mockResourceContext, null));

        // Null parent provider → null
        ResolveContext<Void> noProviderContext = newMockResolveContext();
        Mockito.doReturn(null).when(noProviderContext).getParentResourceProvider();
        assertNull(RelayResourceHelper.getResource(noProviderContext, PATH_TARGET, mockResourceContext, null));
    }

    @Test
    public void shouldDelegateListChildrenToParentProvider() {
        Resource parent = context.create().resource(PATH_TARGET);
        Resource child = context.create().resource(PATH_TARGET + PATH_CHILD);

        ResourceProvider<Void> mockProvider = newMockProvider();
        ResolveContext<Void> mockParentContext = newMockResolveContext();
        Mockito.when(mockProvider.listChildren(Mockito.any(), Mockito.eq(parent)))
            .thenReturn(Collections.singletonList(child).iterator());

        ResolveContext<Void> mockResolveContext = newMockResolveContext();
        Mockito.doReturn(mockProvider).when(mockResolveContext).getParentResourceProvider();
        Mockito.doReturn(mockParentContext).when(mockResolveContext).getParentResolveContext();

        // Happy path → delegates to parent provider
        Iterator<Resource> result = RelayResourceHelper.listChildren(mockResolveContext, parent);
        assertNotNull(result);
        assertTrue(result.hasNext());
        assertEquals(PATH_TARGET + PATH_CHILD, result.next().getPath());

        // Null context → null
        assertNull(RelayResourceHelper.listChildren(null, parent));

        // Null parent provider → null
        ResolveContext<Void> noProviderContext = newMockResolveContext();
        Mockito.doReturn(null).when(noProviderContext).getParentResourceProvider();
        assertNull(RelayResourceHelper.listChildren(noProviderContext, parent));
    }

    @Test
    public void shouldListChildrenFromTarget() {
        Resource target = context.create().resource(PATH_TARGET);
        context.create().resource(PATH_TARGET + PATH_CHILD);

        // Non-RelayResource → children wrapped in RelayResource using the supplied exposed path
        Iterator<Resource> result = RelayResourceHelper.listChildren(target, PATH_EXPOSED);
        assertNotNull(result);
        assertTrue(result.hasNext());
        assertEquals(PATH_EXPOSED + PATH_CHILD, result.next().getPath());

        // RelayResource → listChildren() passthrough; the path argument is ignored and the
        // relay resource's own path is used when mapping child names
        RelayResource relayTarget = new RelayResource(target, PATH_EXPOSED);
        Iterator<Resource> relayResult = RelayResourceHelper.listChildren(relayTarget, "/content/ignored");
        assertNotNull(relayResult);
        assertTrue(relayResult.hasNext());
        assertEquals(PATH_EXPOSED + PATH_CHILD, relayResult.next().getPath());
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
