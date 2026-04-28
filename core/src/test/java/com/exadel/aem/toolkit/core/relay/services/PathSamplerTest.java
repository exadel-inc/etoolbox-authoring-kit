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
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.testing.mock.jcr.MockJcr;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathSamplerTest {

    private static final String PATH_PLAIN = "/content/plain";
    private static final String PATH_ANOTHER = "/content/another";

    private static final String XPATH_MATCH_SAMPLED = "/jcr:root/content//*[@type='sampled']";

    @Test
    public void shouldReportEmptyWhenSamplesAreNullOrEmpty() {
        assertTrue(new PathSampler(null).isEmpty());
        assertTrue(new PathSampler(Collections.emptyList()).isEmpty());
        assertFalse(new PathSampler(Collections.singletonList(PATH_PLAIN)).isEmpty());
    }

    @Test
    public void shouldReturnEmptyChangesWithoutResolution() {
        PathSampler sampler = new PathSampler(Collections.singletonList(PATH_PLAIN));

        Collection<ResourceChange> changes = sampler.createChanges();

        assertTrue(changes.isEmpty());
    }

    @Test
    public void shouldProduceChangesFromJcrPaths() {
        // Session is not accessed during plain JCR path resolution; null is safe here
        PathSampler sampler = new PathSampler(Arrays.asList(PATH_PLAIN, PATH_ANOTHER));

        Collection<ResourceChange> changes = sampler.createChanges(null);

        assertEquals(2, changes.size());
        assertTrue(changes.stream().anyMatch(c -> PATH_PLAIN.equals(c.getPath())));
        assertTrue(changes.stream().anyMatch(c -> PATH_ANOTHER.equals(c.getPath())));
        changes.forEach(c -> assertEquals(ResourceChange.ChangeType.CHANGED, c.getType()));
    }

    @Test
    public void shouldProduceChangesFromXPathExpression() throws RepositoryException {
        Session session = MockJcr.newSession();
        Node contentNode = session.getRootNode().addNode("content");
        Node plainNode = contentNode.addNode("plain");
        Node anotherNode = contentNode.addNode("another");
        setXpathQueryResult(session, XPATH_MATCH_SAMPLED, Arrays.asList(plainNode, anotherNode));

        PathSampler sampler = new PathSampler(Collections.singletonList(XPATH_MATCH_SAMPLED));
        Collection<ResourceChange> changes = sampler.createChanges(session);

        assertEquals(2, changes.size());
        assertTrue(changes.stream().anyMatch(c -> PATH_PLAIN.equals(c.getPath())));
        assertTrue(changes.stream().anyMatch(c -> PATH_ANOTHER.equals(c.getPath())));
        changes.forEach(c -> assertEquals(ResourceChange.ChangeType.CHANGED, c.getType()));
    }

    @Test
    public void shouldProduceChangesFromMixedSamples() throws RepositoryException {
        Session session = MockJcr.newSession();
        Node anotherNode = session.getRootNode().addNode("content").addNode("another");
        setXpathQueryResult(session, XPATH_MATCH_SAMPLED, Collections.singletonList(anotherNode));

        // PATH_PLAIN is a plain JCR path; PATH_ANOTHER is resolved via the XPath expression
        PathSampler sampler = new PathSampler(Arrays.asList(PATH_PLAIN, XPATH_MATCH_SAMPLED));
        Collection<ResourceChange> changes = sampler.createChanges(session);

        assertEquals(2, changes.size());
        assertTrue(changes.stream().anyMatch(c -> PATH_PLAIN.equals(c.getPath())));
        assertTrue(changes.stream().anyMatch(c -> PATH_ANOTHER.equals(c.getPath())));
    }

    @Test
    public void shouldSkipUnrecognizedSamples() {
        PathSampler sampler = new PathSampler(Arrays.asList("not-a-path", "relative/path", ""));

        // Session is not accessed because none of the samples require resolution; null is safe
        Collection<ResourceChange> changes = sampler.createChanges(null);

        assertTrue(changes.isEmpty());
    }

    @Test
    public void shouldReturnEmptyChangesWhenXPathYieldsNoResults() {
        Session session = MockJcr.newSession();
        setXpathQueryResult(session, XPATH_MATCH_SAMPLED, Collections.emptyList());

        PathSampler sampler = new PathSampler(Collections.singletonList(XPATH_MATCH_SAMPLED));
        Collection<ResourceChange> changes = sampler.createChanges(session);

        assertTrue(changes.isEmpty());
    }

    @Test
    public void shouldResolveSamplesOnce() throws RepositoryException {
        Session session = MockJcr.newSession();
        Node plainNode = session.getRootNode().addNode("content").addNode("plain");
        setXpathQueryResult(session, XPATH_MATCH_SAMPLED, Collections.singletonList(plainNode));

        PathSampler sampler = new PathSampler(Collections.singletonList(XPATH_MATCH_SAMPLED));
        Collection<ResourceChange> firstResult = sampler.createChanges(session);
        // Second call with null session — if resolution were not cached, a NullPointerException would occur
        Collection<ResourceChange> secondResult = sampler.createChanges(null);

        assertEquals(firstResult.size(), secondResult.size());
        assertTrue(secondResult.stream().anyMatch(c -> PATH_PLAIN.equals(c.getPath())));
    }

    @Test
    public void shouldReturnEmptyChangesForNullSamples() {
        PathSampler sampler = new PathSampler(null);

        Collection<ResourceChange> changes = sampler.createChanges(null);

        assertTrue(changes.isEmpty());
    }

    /* ---------------
       Utility methods
       --------------- */

    @SuppressWarnings({"deprecation", "SameParameterValue"})
    private static void setXpathQueryResult(Session session, String expression, List<Node> nodes) {
        MockJcr.setQueryResult(session, expression, Query.XPATH, nodes);
    }
}
