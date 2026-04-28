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
package com.exadel.aem.toolkit.core.relay.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.spi.resource.provider.ObservationReporter;
import org.apache.sling.spi.resource.provider.ProviderContext;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;

@RunWith(MockitoJUnitRunner.class)
public class RelayProviderTest {

    private static final String PATH_ASSET = "/content/asset";
    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_TARGET = "/content/target";

    private static final String SUBPATH_CHILD = "/child";
    private static final String SUBPATH_PAGE = "/page";


    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldReturnRelayResource() {
        context.create().resource(PATH_TARGET);
        context.create().resource(PATH_TARGET + SUBPATH_CHILD);

        RelayProvider provider = newProvider();
        ResolveContext<Void> resolveContext = newResolveContext();
        ResourceContext mockResourceContext = Mockito.mock(ResourceContext.class);

        // Path outside the configured source → null regardless of what the resolver holds
        Resource unrelated = provider.getResource(resolveContext, "/content/other", mockResourceContext, null);
        assertNull(unrelated);

        // No target resource for source → null (parent provider also absent)
        Resource missingTarget = provider.getResource(resolveContext, PATH_SOURCE + "/missing", mockResourceContext, null);
        assertNull(missingTarget);

        // When the target resource exists, a relay resource is created based on the target resource,
        // but it reports the source path
        Resource existingTarget = provider.getResource(resolveContext, PATH_SOURCE, mockResourceContext, null);
        assertNotNull(existingTarget);
        assertEquals(PATH_SOURCE, existingTarget.getPath());

        // When the target resource exists with a child, a relay resource is created based on the target resource's child,
        // but it reports the source path
        Resource existingTargetChild = provider.getResource(
            resolveContext,
            PATH_SOURCE + SUBPATH_CHILD,
            mockResourceContext,
            null);
        assertNotNull(existingTargetChild);
        assertEquals(PATH_SOURCE + SUBPATH_CHILD, existingTargetChild.getPath());
    }

    @Test
    public void shouldDelegateChildListingToParentProvider() {
        context.create().resource(PATH_TARGET);
        Resource parent = context.create().resource(PATH_SOURCE);
        Resource expectedChild = context.create().resource(PATH_SOURCE + SUBPATH_CHILD);

        @SuppressWarnings("unchecked")
        ResourceProvider<Void> mockParentProvider = Mockito.mock(ResourceProvider.class);
        @SuppressWarnings("unchecked")
        ResolveContext<Void> mockParentContext = Mockito.mock(ResolveContext.class);
        Mockito.when(mockParentProvider.listChildren(Mockito.any(), Mockito.eq(parent)))
            .thenReturn(Collections.singletonList(expectedChild).iterator());

        ResolveContext<Void> resolveContext = newResolveContextWithParent(mockParentProvider, mockParentContext);

        RelayProvider provider = newProvider();
        Iterator<Resource> result = provider.listChildren(resolveContext, parent);

        assertNotNull(result);
        assertTrue(result.hasNext());
        assertEquals(PATH_SOURCE + SUBPATH_CHILD, result.next().getPath());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void shouldNotifyChangedPaths() throws LoginException {
        ResourceResolverFactory mockFactory = newMockFactory();
        RelayProvider provider = newProvider(mockFactory);

        // onChange before start() → no providerContext, must not throw
        ResourceChange earlyChange = new ResourceChange(
            ResourceChange.ChangeType.CHANGED,
            PATH_TARGET + SUBPATH_PAGE,
            false);
        provider.onChange(Collections.singletonList(earlyChange));

        // Start the provider so providerContext is established
        ProviderContext mockProviderContext = Mockito.mock(ProviderContext.class);
        ObservationReporter mockReporter = Mockito.mock(ObservationReporter.class);
        Mockito.when(mockProviderContext.getObservationReporter()).thenReturn(mockReporter);
        provider.start(mockProviderContext);

        // onChange after start → changes translated from target to source path
        ResourceChange change = new ResourceChange(
            ResourceChange.ChangeType.CHANGED,
            PATH_TARGET + SUBPATH_PAGE,
            false);
        provider.onChange(Collections.singletonList(change));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(mockReporter).reportChanges(captor.capture(), Mockito.eq(false));

        List<ResourceChange> reported = captor.getValue();
        assertEquals(1, reported.size());
        assertEquals(PATH_SOURCE + SUBPATH_PAGE, reported.get(0).getPath());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void shouldReportChangesOnStartAndStop() throws LoginException {
        ResourceResolverFactory mockFactory = newMockFactory();
        RelayProvider provider = RelayProvider
            .builder()
            .resolverFactory(mockFactory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .reportedPath(PATH_ASSET)
            .build();

        ProviderContext mockProviderContext = Mockito.mock(ProviderContext.class);
        ObservationReporter mockReporter = Mockito.mock(ObservationReporter.class);
        Mockito.when(mockProviderContext.getObservationReporter()).thenReturn(mockReporter);

        provider.start(mockProviderContext);

        ArgumentCaptor<Collection> startCaptor = ArgumentCaptor.forClass(Collection.class);
        Mockito.verify(mockReporter).reportChanges(startCaptor.capture(), Mockito.eq(false));
        Collection<ResourceChange> startChanges = startCaptor.getValue();
        assertEquals(1, startChanges.size());
        assertEquals(PATH_ASSET, startChanges.iterator().next().getPath());

        provider.stop();

        // reportChanges called once on start and once on stop
        Mockito.verify(mockReporter, Mockito.times(2))
            .reportChanges(Mockito.any(), Mockito.eq(false));
    }

    @Test
    public void shouldHandleLoginExceptionOnStart() throws LoginException {
        ResourceResolverFactory mockFactory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(mockFactory.getServiceResourceResolver(Mockito.anyMap()))
            .thenThrow(new LoginException("NOT AN EXCEPTION: Testing login failure handling"));

        RelayProvider provider = RelayProvider
            .builder()
            .resolverFactory(mockFactory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .reportedPath(PATH_ASSET)
            .build();

        // LoginException must be caught internally; no exception propagates to the caller
        provider.start(Mockito.mock(ProviderContext.class));
    }

    @Test
    public void shouldApplyUserMapping() throws LoginException {
        Resource targetResource = context.create().resource(PATH_TARGET);

        ResourceResolverFactory mockFactory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver mappedResolver = Mockito.mock(ResourceResolver.class);
        Map<String, Object> propertyMap = new HashMap<>();
        Mockito.when(mappedResolver.getResource(PATH_TARGET)).thenReturn(targetResource);
        Mockito.when(mappedResolver.getUserID()).thenReturn("mapped-user");

        ResourceResolver basicResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(basicResolver.getUserID()).thenReturn("testUser");
        Mockito.when(basicResolver.getPropertyMap()).thenReturn(propertyMap);

        @SuppressWarnings("unchecked")
        ResolveContext<Void> resolveContext = Mockito.mock(ResolveContext.class);
        Mockito.when(resolveContext.getResourceResolver()).thenReturn(basicResolver);
        ResourceContext mockResourceContext = Mockito.mock(ResourceContext.class);

        // Subservice user mapping: target is a plain service name (no colon)
        Mockito.when(mockFactory.getServiceResourceResolver(Mockito.anyMap())).thenReturn(mappedResolver);

        RelayProvider providerWithSubservice = RelayProvider
            .builder()
            .resolverFactory(mockFactory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .userMapping("testUser", "mySubservice")
            .build();

        Resource result = providerWithSubservice.getResource(resolveContext, PATH_SOURCE, mockResourceContext, null);
        assertNotNull(result);
        assertEquals(PATH_SOURCE, result.getPath());
        Mockito.verify(mockFactory).getServiceResourceResolver(
            Mockito.argThat(map -> "mySubservice".equals(map.get(ResourceResolverFactory.SUBSERVICE))));

        // Credentials user mapping: target is "user:password" (contains colon)
        Mockito.reset(mockFactory, mappedResolver);
        propertyMap.clear();
        Mockito.when(mappedResolver.getResource(PATH_TARGET)).thenReturn(targetResource);
        Mockito.when(mockFactory.getResourceResolver(Mockito.anyMap())).thenReturn(mappedResolver);

        RelayProvider providerWithCredentials = RelayProvider
            .builder()
            .resolverFactory(mockFactory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .userMapping("testUser", "svcUser:svcPass")
            .build();

        result = providerWithCredentials.getResource(resolveContext, PATH_SOURCE, mockResourceContext, null);
        assertNotNull(result);
        assertEquals(PATH_SOURCE, result.getPath());
        Mockito.verify(mockFactory).getResourceResolver(
            Mockito.argThat(map -> "svcUser".equals(map.get(ResourceResolverFactory.USER))
                && java.util.Arrays.equals(
                "svcPass".toCharArray(),
                (char[]) map.get(ResourceResolverFactory.PASSWORD))));
    }

    /* ---------------
       Utility methods
       --------------- */

    private RelayProvider newProvider() {
        return RelayProvider
            .builder()
            .resolverFactory(Mockito.mock(ResourceResolverFactory.class))
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .build();
    }

    private RelayProvider newProvider(ResourceResolverFactory factory) {
        return RelayProvider
            .builder()
            .resolverFactory(factory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .build();
    }

    @SuppressWarnings("unchecked")
    private ResolveContext<Void> newResolveContext() {
        ResolveContext<Void> resolveContext = Mockito.mock(ResolveContext.class);
        Mockito.when(resolveContext.getResourceResolver()).thenReturn(context.resourceResolver());
        return resolveContext;
    }

    @SuppressWarnings("unchecked")
    private ResolveContext<Void> newResolveContextWithParent(
        ResourceProvider<Void> parentProvider,
        ResolveContext<Void> parentContext) {

        ResolveContext<Void> resolveContext = Mockito.mock(ResolveContext.class);
        Mockito.when(resolveContext.getResourceResolver()).thenReturn(context.resourceResolver());
        // doReturn avoids generic wildcard type mismatch between ResourceProvider<?> and ResourceProvider<Void>
        Mockito.doReturn(parentProvider).when(resolveContext).getParentResourceProvider();
        Mockito.doReturn(parentContext).when(resolveContext).getParentResolveContext();
        return resolveContext;
    }

    private static ResourceResolverFactory newMockFactory() throws LoginException {
        ResourceResolverFactory mockFactory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(mockFactory.getServiceResourceResolver(Mockito.anyMap()))
            .thenReturn(Mockito.mock(ResourceResolver.class));
        return mockFactory;
    }
}
