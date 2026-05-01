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
import java.util.List;
import java.util.stream.Collectors;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.relay.models.ChangeSample;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

public class PathSamplerTest {

    private static final String PATH_SOURCE = "/content/source";
    private static final String PATH_TARGET = "/content/target";
    private static final String PATH_CHILD = "/page1";

    private static final String XPATH_CHILDREN = "/jcr:root/content/target/*";
    private static final String XPATH_INVALID = "//[@invalid syntax";

    @Rule
    public final AemContext context = new AemContextBuilder()
        .resourceResolverType(ResourceResolverType.JCR_OAK)
        .build();

    /* ----------------
       JCR path samples
       ---------------- */

    @Test
    public void shouldReturnEmptyForNoSamples() {
        PathSampler samplerWithNull = PathSampler
            .builder()
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .build();
        assertTrue(samplerWithNull.createChanges().isEmpty());

        PathSampler samplerWithEmpty = PathSampler
            .builder()
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Collections.emptyList())
            .build();
        assertTrue(samplerWithEmpty.createChanges().isEmpty());
    }

    @Test
    public void shouldCreateChangesFromJcrPaths() {
        // Single path: emits one CHANGED event
        PathSampler singleSampler = PathSampler
            .builder()
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Collections.singletonList(newChangeSample(PATH_TARGET + PATH_CHILD)))
            .build();

        Collection<ResourceChange> singleResult = singleSampler.createChanges();
        assertEquals(1, singleResult.size());
        assertEquals(ResourceChange.ChangeType.CHANGED, singleResult.iterator().next().getType());

        // Multiple distinct paths: each emits a separate event
        PathSampler multiSampler = PathSampler
            .builder()
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Arrays.asList(
                newChangeSample(PATH_TARGET + "/page1"),
                newChangeSample(PATH_TARGET + "/page2")))
            .build();
        assertEquals(2, multiSampler.createChanges().size());

        // Duplicate path in samples: deduplicated by the internal path set
        PathSampler dupSampler = PathSampler.builder()
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Arrays.asList(
                newChangeSample(PATH_TARGET + PATH_CHILD),
                newChangeSample(PATH_TARGET + PATH_CHILD)))
            .build();
        assertEquals(1, dupSampler.createChanges().size());
    }

    @Test
    public void shouldRewriteTargetPaths() {
        PathSampler sampler = PathSampler
            .builder()
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Arrays.asList(
                newChangeSample(PATH_TARGET + PATH_CHILD),
                newChangeSample("/content/unrelated/page")))
            .build();

        Collection<ResourceChange> changes = sampler.createChanges();
        List<String> paths = changes.stream()
            .map(ResourceChange::getPath)
            .collect(Collectors.toList());

        assertEquals(2, paths.size());
        // Path under target is rewritten to source prefix
        assertTrue(paths.contains(PATH_SOURCE + PATH_CHILD));
        // Path outside target is returned unchanged
        assertTrue(paths.contains("/content/unrelated/page"));
    }

    /* --------------
       Result caching
       -------------- */

    @Test
    public void shouldCacheResolvedPaths() {
        PathSampler sampler = PathSampler
            .builder()
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Collections.singletonList(newChangeSample(PATH_TARGET + PATH_CHILD)))
            .build();

        // First call applies target→source rewriting
        Collection<ResourceChange> firstResult = sampler.createChanges();
        assertEquals(1, firstResult.size());
        assertEquals(PATH_SOURCE + PATH_CHILD, firstResult.iterator().next().getPath());

        // Second call returns raw cached paths without rewriting
        Collection<ResourceChange> secondResult = sampler.createChanges();
        assertEquals(1, secondResult.size());
        assertEquals(PATH_TARGET + PATH_CHILD, secondResult.iterator().next().getPath());
    }

    /* ----------------
       XPath resolution
       ---------------- */

    @Test
    public void shouldResolveXpathExpression() throws LoginException, PersistenceException {
        context.create().resource(PATH_TARGET + PATH_CHILD);
        context.resourceResolver().commit();

        PathSampler sampler = PathSampler
            .builder()
            .resolverFactory(newMockFactory())
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Collections.singletonList(newChangeSample(XPATH_CHILDREN)))
            .build();

        Collection<ResourceChange> changes = sampler.createChanges();

        assertEquals(1, changes.size());
        assertEquals(PATH_SOURCE + PATH_CHILD, changes.iterator().next().getPath());
    }

    @Test
    public void shouldApplyQueryLimit() throws LoginException, PersistenceException {
        for (int i = 0; i < 10; i++) {
            context.create().resource(PATH_TARGET + "/node" + i);
        }
        context.resourceResolver().commit();

        PathSampler sampler = PathSampler
            .builder()
            .resolverFactory(newMockFactory())
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Collections.singletonList(newChangeSampleWithLimit(XPATH_CHILDREN, 5)))
            .build();

        Collection<ResourceChange> changes = sampler.createChanges();

        assertEquals(5, changes.size());
    }

    @Test
    public void shouldSkipXpathOnError() throws LoginException {
        // Invalid XPath syntax causes Oak to throw InvalidQueryException (extends RepositoryException)
        PathSampler repositoryErrorSampler = PathSampler
            .builder()
            .resolverFactory(newMockFactory())
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Collections.singletonList(newChangeSample(XPATH_INVALID)))
            .build();

        assertTrue(repositoryErrorSampler.createChanges().isEmpty());

        // LoginException when obtaining a resolver: sample is skipped
        ResourceResolverFactory failingFactory = Mockito.mock(ResourceResolverFactory.class);
        Mockito
            .when(failingFactory.getServiceResourceResolver(Mockito.any()))
            .thenThrow(new LoginException("Service user not found"));

        PathSampler loginErrorSampler = PathSampler
            .builder()
            .resolverFactory(failingFactory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Collections.singletonList(newChangeSample(XPATH_CHILDREN)))
            .build();

        assertTrue(loginErrorSampler.createChanges().isEmpty());
    }

    /* -----------------
       Resolver rotation
       ----------------- */

    @Test
    public void shouldReuseResolverForSameUser() throws LoginException {
        ResourceResolver sessionResolver = newMockResolver();
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(factory.getServiceResourceResolver(Mockito.any())).thenReturn(sessionResolver);

        PathSampler sampler = PathSampler.builder()
            .resolverFactory(factory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Arrays.asList(
                newChangeSampleWithUser(XPATH_CHILDREN, "author"),
                newChangeSampleWithUser("/jcr:root/content//element(*)", "author")))
            .build();

        sampler.createChanges();

        // Factory is called once; the same resolver is reused for the second sample
        Mockito.verify(factory, Mockito.times(1)).getServiceResourceResolver(Mockito.any());
    }

    @Test
    public void shouldRotateResolverOnUserChange() throws LoginException {
        ResourceResolver resolverA = newMockResolver();
        ResourceResolver resolverB = newMockResolver();

        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        Mockito.when(factory.getServiceResourceResolver(Mockito.any()))
            .thenReturn(resolverA)
            .thenReturn(resolverB);

        PathSampler sampler = PathSampler.builder()
            .resolverFactory(factory)
            .source(PATH_SOURCE)
            .target(PATH_TARGET)
            .samples(Arrays.asList(
                newChangeSampleWithUser(XPATH_CHILDREN, "user1"),
                newChangeSampleWithUser("/jcr:root/content//element(*)", "user2")))
            .build();

        sampler.createChanges();

        // Factory is called once per distinct user
        Mockito.verify(factory, Mockito.times(2)).getServiceResourceResolver(Mockito.any());
        // The first resolver is closed when the user changes
        Mockito.verify(resolverA).close();
    }

    /* ---------------
       Utility methods
       --------------- */

    private static ChangeSample newChangeSample(String path) {
        return ObjectConversionUtil.toObject(
            "{\"path\":\"" + path + "\"}",
            ChangeSample.class);
    }

    @SuppressWarnings("SameParameterValue")
    private static ChangeSample newChangeSampleWithLimit(String path, int limit) {
        return ObjectConversionUtil.toObject(
            "{\"path\":\"" + path + "\",\"limit\":" + limit + "}",
            ChangeSample.class);
    }

    private static ChangeSample newChangeSampleWithUser(String path, String user) {
        return ObjectConversionUtil.toObject(
            "{\"path\":\"" + path + "\",\"user\":\"" + user + "\"}",
            ChangeSample.class);
    }

    private ResourceResolverFactory newMockFactory() throws LoginException {
        // We are not using the built-in resource resolver factory because of lack of support for
        // ResourceResolver#getPropertyMap() in the built-in mock resource resolver (dependency version issue).
        // Can be revised to use the real factory once the dependency is updated
        ResourceResolverFactory factory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver sessionResolver = newMockResolver();
        Mockito
            .when(factory.getServiceResourceResolver(Mockito.any()))
            .thenReturn(sessionResolver);
        return factory;
    }

    private ResourceResolver newMockResolver() {
        Session session = context.resourceResolver().adaptTo(Session.class);
        assertNotNull(session);
        ResourceResolver resolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(resolver.adaptTo(Session.class)).thenReturn(session);
        Mockito.when(resolver.getPropertyMap()).thenReturn(new HashMap<>());
        return resolver;
    }
}
