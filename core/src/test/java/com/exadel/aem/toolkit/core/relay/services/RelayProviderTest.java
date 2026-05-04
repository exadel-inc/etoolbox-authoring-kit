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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.relay.models.ChangeSample;
import com.exadel.aem.toolkit.core.relay.models.RelayInfo;
import com.exadel.aem.toolkit.core.relay.models.RelayMapping;
import com.exadel.aem.toolkit.core.relay.models.RelayResource;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

public class RelayProviderTest {

    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_TARGET = "/content/target";
    private static final String PATH_CHILD_A = "/page1";
    private static final String PATH_CHILD_B = "/page2";
    private static final String USER_AUTHOR = "author";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    /* ------------------------------
       getResource(): path resolution
       ------------------------------ */

    @Test
    public void shouldReturnRelayResource() {
        // Path under source prefix
        context.create().resource(PATH_TARGET);
        context.create().resource(PATH_TARGET + PATH_CHILD_A);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        ResolveContext<Void> resolveContext = newResolveContext();
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        Resource result = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);

        assertNotNull(result);
        assertTrue(result instanceof RelayResource);
        assertEquals(PATH_SOURCE + PATH_CHILD_A, result.getPath());

        // Exact source path
        provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        resolveContext = newResolveContext();
        resourceContext = Mockito.mock(ResourceContext.class);

        result = provider.getResource(resolveContext, PATH_SOURCE, resourceContext, null);

        assertNotNull(result);
        assertTrue(result instanceof RelayResource);
        assertEquals(PATH_SOURCE, result.getPath());
    }

    @Test
    public void shouldReturnNullWhenPathNotUnderSource() {
        String otherPath = "/content/other/page";
        context.create().resource(otherPath);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        ResolveContext<Void> resolveContext = newResolveContext();
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        Resource result = provider.getResource(resolveContext, otherPath, resourceContext, null);

        assertNull(result);
    }

    /* ---------------------------------------
       getResource(): parent provider fallback
       --------------------------------------- */

    @Test
    public void shouldFallBackToParentProvider() {
        ResourceProvider<Void> mockProvider = newMockProvider();
        ResolveContext<Void> parentCtx = newMockResolveContext();
        Resource fallbackResource = context.create().resource(PATH_SOURCE + PATH_CHILD_A);
        Mockito.when(mockProvider.getResource(Mockito.any(), Mockito.eq(PATH_SOURCE + PATH_CHILD_A), Mockito.any(), Mockito.any()))
            .thenReturn(fallbackResource);

        ResolveContext<Void> resolveContext = newResolveContext();
        Mockito.doReturn(mockProvider).when(resolveContext).getParentResourceProvider();
        Mockito.doReturn(parentCtx).when(resolveContext).getParentResolveContext();

        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        Resource result = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);

        assertNotNull(result);
        assertEquals(PATH_SOURCE + PATH_CHILD_A, result.getPath());
        assertFalse(result instanceof RelayResource);
    }

    @Test
    public void shouldReturnNullWhenTargetAndParentMiss() {
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        ResolveContext<Void> resolveContext = newResolveContext();
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        Resource result = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);

        assertNull(result);
    }

    /* --------------
       listChildren()
       -------------- */

    @Test
    public void shouldListRelayChildren() {
        context.create().resource(PATH_TARGET);
        context.create().resource(PATH_TARGET + PATH_CHILD_A);

        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        ResolveContext<Void> resolveContext = newResolveContext();
        Resource parent = context.create().resource(PATH_SOURCE);

        // Single child
        Iterator<Resource> result = provider.listChildren(resolveContext, parent);

        assertNotNull(result);
        assertTrue(result.hasNext());
        Resource child = result.next();
        assertEquals(PATH_SOURCE + PATH_CHILD_A, child.getPath());
        assertTrue(child instanceof RelayResource);
        assertFalse(result.hasNext());

        // Multiple children
        context.create().resource(PATH_TARGET + PATH_CHILD_B);
        result = provider.listChildren(resolveContext, parent);

        assertNotNull(result);
        Map<String, Resource> children = new HashMap<>();
        while (result.hasNext()) {
            Resource c = result.next();
            children.put(c.getPath(), c);
        }
        assertEquals(2, children.size());
        assertTrue(children.containsKey(PATH_SOURCE + PATH_CHILD_A));
        assertTrue(children.containsKey(PATH_SOURCE + PATH_CHILD_B));
        assertTrue(children.get(PATH_SOURCE + PATH_CHILD_A) instanceof RelayResource);
        assertTrue(children.get(PATH_SOURCE + PATH_CHILD_B) instanceof RelayResource);
    }

    @Test
    public void shouldReturnNullWhenTargetNotFound() {
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        ResolveContext<Void> resolveContext = newResolveContext();

        Resource parent = context.create().resource(PATH_SOURCE);

        Iterator<Resource> result = provider.listChildren(resolveContext, parent);

        assertNull(result);
    }

    @Test
    public void shouldDelegateToParentProvider() {
        ResourceProvider<Void> mockProvider = newMockProvider();
        ResolveContext<Void> parentCtx = newMockResolveContext();
        Resource fallbackResource = context.create().resource("/content/fallback");
        Mockito
            .when(mockProvider.getResource(Mockito.any(), Mockito.eq(PATH_SOURCE), Mockito.isNull(), Mockito.any()))
            .thenReturn(fallbackResource);

        Resource parent = context.create().resource(PATH_SOURCE);
        Resource expectedChild = context.create().resource(parent, "page1");
        Mockito
            .when(mockProvider.listChildren(Mockito.any(), Mockito.any()))
            .thenReturn(Collections.singletonList(expectedChild).iterator());

        ResolveContext<Void> resolveContext = newResolveContext();
        Mockito.doReturn(mockProvider).when(resolveContext).getParentResourceProvider();
        Mockito.doReturn(parentCtx).when(resolveContext).getParentResolveContext();

        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));

        Iterator<Resource> result = provider.listChildren(resolveContext, parent);

        assertNotNull(result);
        assertTrue(result.hasNext());
        assertEquals(PATH_SOURCE + PATH_CHILD_A, result.next().getPath());
    }

    /* ----------------------
       Start / stop lifecycle
       ---------------------- */

    @Test
    public void shouldStartWithEmptyChangeSamples() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);

        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        provider.start(providerContext);

        Mockito.verify(reporter, Mockito.never()).reportChanges(Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void shouldStartAndReportChangeSamples() {
        ChangeSample sample = newChangeSample(PATH_TARGET + PATH_CHILD_A);
        RelayInfo relay = newRelayInfo(
            PATH_SOURCE, PATH_TARGET,
            Collections.emptyList(),
            Collections.singletonList(sample));

        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);

        RelayProvider provider = newProvider(relay);
        provider.start(providerContext);

        ArgumentCaptor<Collection<ResourceChange>> captor = changeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));

        Collection<ResourceChange> changes = captor.getValue();
        assertEquals(1, changes.size());
        ResourceChange change = changes.iterator().next();
        assertEquals(PATH_SOURCE + PATH_CHILD_A, change.getPath());
        assertEquals(ResourceChange.ChangeType.CHANGED, change.getType());
    }

    @Test
    public void shouldSetProviderContext() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));

        // Before start: onChange is a no-op because providerContext is null
        provider.onChange(Collections.singletonList(
            new ResourceChange(ResourceChange.ChangeType.CHANGED, PATH_TARGET + PATH_CHILD_A, false)));
        Mockito.verify(reporter, Mockito.never()).reportChanges(Mockito.any(), Mockito.anyBoolean());

        // After start: onChange calls reportChanges because providerContext is set
        provider.start(providerContext);
        provider.onChange(Collections.singletonList(
            new ResourceChange(ResourceChange.ChangeType.CHANGED, PATH_TARGET + PATH_CHILD_A, false)));
        Mockito.verify(reporter, Mockito.times(1)).reportChanges(Mockito.any(), Mockito.eq(false));
    }

    @Test
    public void shouldStopGracefullyBeforeStart() {
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));

        // Calling stop() without a prior start() must not throw (providerContext and sampler are null)
        provider.stop();
    }

    @Test
    public void shouldStopWithEmptyChangeSamples() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);

        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        provider.start(providerContext);
        Mockito.reset(reporter);

        provider.stop();

        Mockito.verify(reporter, Mockito.never()).reportChanges(Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void shouldStopAndReportChanges() {
        ChangeSample sample = newChangeSample(PATH_TARGET + PATH_CHILD_A);
        RelayInfo relay = newRelayInfo(
            PATH_SOURCE, PATH_TARGET,
            Collections.emptyList(),
            Collections.singletonList(sample));

        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);

        RelayProvider provider = newProvider(relay);
        provider.start(providerContext);

        Mockito.reset(reporter);

        provider.stop();

        ArgumentCaptor<Collection<ResourceChange>> captor = changeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));

        Collection<ResourceChange> changes = captor.getValue();
        assertEquals(1, changes.size());
        assertEquals(PATH_SOURCE + PATH_CHILD_A, changes.iterator().next().getPath());
    }

    /* --------------------
       Change event mapping
       -------------------- */

    @Test
    public void shouldNoOpWhenProviderContextIsNull() {
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));

        // Not started: providerContext is null, so onChange returns immediately without throwing
        provider.onChange(Collections.singletonList(
            new ResourceChange(ResourceChange.ChangeType.CHANGED, PATH_TARGET + PATH_CHILD_A, false)));
    }

    @Test
    public void shouldMapSingleChange() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        provider.start(providerContext);

        provider.onChange(Collections.singletonList(
            new ResourceChange(ResourceChange.ChangeType.REMOVED, PATH_TARGET + PATH_CHILD_A, false)));

        ArgumentCaptor<List<ResourceChange>> captor = listChangeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));

        List<ResourceChange> mapped = captor.getValue();
        assertEquals(1, mapped.size());
        assertEquals(PATH_SOURCE + PATH_CHILD_A, mapped.get(0).getPath());
        assertEquals(ResourceChange.ChangeType.REMOVED, mapped.get(0).getType());
    }

    @Test
    public void shouldMapChangePathsFromTargetToSource() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        provider.start(providerContext);

        // Single change
        provider.onChange(Collections.singletonList(
            new ResourceChange(ResourceChange.ChangeType.REMOVED, PATH_TARGET + PATH_CHILD_A, false)));

        ArgumentCaptor<List<ResourceChange>> captor = listChangeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));

        List<ResourceChange> mapped = captor.getValue();
        assertEquals(1, mapped.size());
        assertEquals(PATH_SOURCE + PATH_CHILD_A, mapped.get(0).getPath());
        assertEquals(ResourceChange.ChangeType.REMOVED, mapped.get(0).getType());

        Mockito.reset(reporter);

        // Multiple changes
        List<ResourceChange> incoming = Arrays.asList(
            new ResourceChange(ResourceChange.ChangeType.CHANGED, PATH_TARGET + PATH_CHILD_A, false),
            new ResourceChange(ResourceChange.ChangeType.ADDED, PATH_TARGET + PATH_CHILD_B, false));
        provider.onChange(incoming);

        captor = listChangeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));

        mapped = captor.getValue();
        assertEquals(2, mapped.size());
        assertEquals(PATH_SOURCE + PATH_CHILD_A, mapped.get(0).getPath());
        assertEquals(ResourceChange.ChangeType.CHANGED, mapped.get(0).getType());
        assertEquals(PATH_SOURCE + PATH_CHILD_B, mapped.get(1).getPath());
        assertEquals(ResourceChange.ChangeType.ADDED, mapped.get(1).getType());
    }

    @Test
    public void shouldHandleEmptyChangeList() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        provider.start(providerContext);

        provider.onChange(Collections.emptyList());

        ArgumentCaptor<Collection<ResourceChange>> captor = changeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));
        assertTrue(captor.getValue().isEmpty());
    }

    @Test
    public void shouldPreserveChangeExternalFlag() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        provider.start(providerContext);

        provider.onChange(Collections.singletonList(
            new ResourceChange(ResourceChange.ChangeType.CHANGED, PATH_TARGET + PATH_CHILD_A, true)));

        ArgumentCaptor<List<ResourceChange>> captor = listChangeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));

        List<ResourceChange> mapped = captor.getValue();
        assertEquals(1, mapped.size());
        assertTrue(mapped.get(0).isExternal());
        assertEquals(PATH_SOURCE + PATH_CHILD_A, mapped.get(0).getPath());
    }

    @Test
    public void shouldNotMapChangesOutsideTarget() {
        ObservationReporter reporter = Mockito.mock(ObservationReporter.class);
        ProviderContext providerContext = newMockProviderContext(reporter);
        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        provider.start(providerContext);

        String unrelatedPath = "/content/unrelated/page";
        provider.onChange(Collections.singletonList(
            new ResourceChange(ResourceChange.ChangeType.CHANGED, unrelatedPath, false)));

        ArgumentCaptor<List<ResourceChange>> captor = listChangeCaptor();
        Mockito.verify(reporter).reportChanges(captor.capture(), Mockito.eq(false));

        List<ResourceChange> mapped = captor.getValue();
        assertEquals(1, mapped.size());
        // Path not under target prefix → returned unchanged
        assertEquals(unrelatedPath, mapped.get(0).getPath());
    }

    /* -------------
       Config update
       ------------- */

    @Test
    public void shouldUpdateRelayConfig() {
        String altTarget = PATH_TARGET + "2";
        String altSource = PATH_SOURCE + "2";
        context.create().resource(altTarget + PATH_CHILD_A);

        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        RelayInfo newRelay = newRelayInfo(altSource, altTarget);
        provider.update(newRelay);

        ResolveContext<Void> resolveContext = newResolveContext();
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        // Old source path no longer matches after the relay was updated
        Resource oldResult = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);
        assertNull(oldResult);

        // New source path resolves to a RelayResource
        Resource newResult = provider.getResource(resolveContext, altSource + PATH_CHILD_A, resourceContext, null);
        assertNotNull(newResult);
        assertTrue(newResult instanceof RelayResource);
        assertEquals(altSource + PATH_CHILD_A, newResult.getPath());
    }

    /* ---------------------
       User resolver mapping
       --------------------- */

    @Test
    public void shouldUseOriginalResolverWhenNoUserMapping() {
        context.create().resource(PATH_TARGET + PATH_CHILD_A);

        RelayProvider provider = newProvider(newRelayInfo(PATH_SOURCE, PATH_TARGET));
        ResolveContext<Void> resolveContext = newResolveContext();
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        Resource result = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);

        assertNotNull(result);
        assertTrue(result instanceof RelayResource);
        assertEquals(PATH_SOURCE + PATH_CHILD_A, result.getPath());
    }

    @Test
    public void shouldUseMappedUserResolver() throws LoginException {
        String userService = "eak-service";
        RelayInfo relay = newRelayInfo(
            PATH_SOURCE, PATH_TARGET,
            Collections.singletonList(newMapping(USER_AUTHOR, userService)),
            Collections.emptyList());

        Resource mockTargetResource = Mockito.mock(Resource.class);
        Mockito.when(mockTargetResource.getPath()).thenReturn(PATH_TARGET + PATH_CHILD_A);
        Mockito.when(mockTargetResource.getResourceMetadata()).thenReturn(new ResourceMetadata());

        ResourceResolver mappedResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(mappedResolver.getResource(PATH_TARGET + PATH_CHILD_A)).thenReturn(mockTargetResource);
        Mockito.when(mappedResolver.getUserID()).thenReturn(userService);

        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(factory.getServiceResourceResolver(Mockito.any())).thenReturn(mappedResolver);

        Map<String, Object> propertyMap = new HashMap<>();
        ResourceResolver basicResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(basicResolver.getUserID()).thenReturn(USER_AUTHOR);
        Mockito.when(basicResolver.getPropertyMap()).thenReturn(propertyMap);

        ResolveContext<Void> resolveContext = newResolveContext(basicResolver);
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        RelayProvider provider = newProvider(factory, relay);
        Resource result = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);

        assertNotNull(result);
        assertTrue(result instanceof RelayResource);
        assertEquals(PATH_SOURCE + PATH_CHILD_A, result.getPath());
        Mockito.verify(mappedResolver).getResource(PATH_TARGET + PATH_CHILD_A);
    }

    @Test
    public void shouldUseOriginalResolver() {
        RelayInfo relay = newRelayInfo(
            PATH_SOURCE, PATH_TARGET,
            Collections.singletonList(newMapping(USER_AUTHOR, USER_AUTHOR)),
            Collections.emptyList());

        Resource mockTargetResource = Mockito.mock(Resource.class);
        Mockito.when(mockTargetResource.getPath()).thenReturn(PATH_TARGET + PATH_CHILD_A);
        Mockito.when(mockTargetResource.getResourceMetadata()).thenReturn(new ResourceMetadata());

        ResourceResolver basicResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(basicResolver.getUserID()).thenReturn(USER_AUTHOR);
        Mockito.when(basicResolver.getResource(PATH_TARGET + PATH_CHILD_A)).thenReturn(mockTargetResource);

        ResolveContext<Void> resolveContext = newResolveContext(basicResolver);
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        RelayProvider provider = newProvider(context.getService(ResourceResolverFactory.class), relay);
        Resource result = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);

        assertNotNull(result);
        assertTrue(result instanceof RelayResource);
        assertEquals(PATH_SOURCE + PATH_CHILD_A, result.getPath());
    }

    @Test
    public void shouldFallBackToOriginalResolver() throws LoginException {
        String brokenService = "broken-service";
        RelayInfo relay = newRelayInfo(
            PATH_SOURCE, PATH_TARGET,
            Collections.singletonList(newMapping(USER_AUTHOR, brokenService)),
            Collections.emptyList());

        Resource mockTargetResource = Mockito.mock(Resource.class);
        Mockito.when(mockTargetResource.getPath()).thenReturn(PATH_TARGET + PATH_CHILD_A);
        Mockito.when(mockTargetResource.getResourceMetadata()).thenReturn(new ResourceMetadata());

        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(factory.getServiceResourceResolver(Mockito.any()))
            .thenThrow(new LoginException("No service user"));

        ResourceResolver basicResolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(basicResolver.getUserID()).thenReturn(USER_AUTHOR);
        Mockito.when(basicResolver.getResource(PATH_TARGET + PATH_CHILD_A)).thenReturn(mockTargetResource);

        ResolveContext<Void> resolveContext = newResolveContext(basicResolver);
        ResourceContext resourceContext = Mockito.mock(ResourceContext.class);

        RelayProvider provider = newProvider(factory, relay);
        Resource result = provider.getResource(resolveContext, PATH_SOURCE + PATH_CHILD_A, resourceContext, null);

        // LoginException is caught and the original resolver is used as fallback
        assertNotNull(result);
        assertTrue(result instanceof RelayResource);
        assertEquals(PATH_SOURCE + PATH_CHILD_A, result.getPath());
        Mockito.verify(basicResolver).getResource(PATH_TARGET + PATH_CHILD_A);
    }

    /* ---------------
       Utility methods
       --------------- */

    private RelayProvider newProvider(RelayInfo relay) {
        return new RelayProvider(context.getService(ResourceResolverFactory.class), relay);
    }

    private static RelayProvider newProvider(ResourceResolverFactory factory, RelayInfo relay) {
        return new RelayProvider(factory, relay);
    }

    private static RelayInfo newRelayInfo(String source, String target) {
        return newRelayInfo(source, target, Collections.emptyList(), Collections.emptyList());
    }

    private static RelayInfo newRelayInfo(
        String source,
        String target,
        Collection<RelayMapping> userMappings,
        Collection<ChangeSample> changeSamples) {
        return new RelayInfo(newMapping(source, target), userMappings, changeSamples);
    }

    private static RelayMapping newMapping(String from, String to) {
        return ObjectConversionUtil.toObject(
            "{\"from\":\"" + from + "\",\"to\":\"" + to + "\"}",
            RelayMapping.class);
    }

    @SuppressWarnings("SameParameterValue")
    private static ChangeSample newChangeSample(String path) {
        return ObjectConversionUtil.toObject(
            "{\"path\":\"" + path + "\"}",
            ChangeSample.class);
    }

    private ResolveContext<Void> newResolveContext() {
        return newResolveContext(context.resourceResolver());
    }

    @SuppressWarnings("unchecked")
    private static ResolveContext<Void> newResolveContext(ResourceResolver resolver) {
        ResolveContext<Void> resolveContext = Mockito.mock(ResolveContext.class);
        Mockito.when(resolveContext.getResourceResolver()).thenReturn(resolver);
        return resolveContext;
    }

    private static ProviderContext newMockProviderContext(ObservationReporter reporter) {
        ProviderContext providerContext = Mockito.mock(ProviderContext.class);
        Mockito.when(providerContext.getObservationReporter()).thenReturn(reporter);
        return providerContext;
    }

    @SuppressWarnings("unchecked")
    private static ResourceProvider<Void> newMockProvider() {
        return Mockito.mock(ResourceProvider.class);
    }

    @SuppressWarnings("unchecked")
    private static ResolveContext<Void> newMockResolveContext() {
        return Mockito.mock(ResolveContext.class);
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<Collection<ResourceChange>> changeCaptor() {
        return (ArgumentCaptor<Collection<ResourceChange>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Collection.class);
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<ResourceChange>> listChangeCaptor() {
        return (ArgumentCaptor<List<ResourceChange>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
    }
}
