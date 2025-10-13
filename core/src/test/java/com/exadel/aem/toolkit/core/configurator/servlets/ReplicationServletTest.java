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

import java.io.IOException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.exadel.aem.toolkit.core.AemContextFactory;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationServletTest {

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    @Mock
    private Replicator replicator;

    private ReplicationServlet replicationServlet;
    private Resource testResource;

    @Before
    public void setUp() throws PersistenceException {
        testResource = context.create().resource("/content/config/test");
        context.resourceResolver().commit();

        context.request().setResource(testResource);

        context.registerService(Replicator.class, replicator);
        replicationServlet = context.registerInjectActivateService(new ReplicationServlet());
    }

    @Test
    public void shouldPublishConfiguration() throws IOException, ReplicationException {
        context.requestPathInfo().setSelectorString("publish");

        replicationServlet.doPost(context.request(), context.response());

        Mockito.verify(replicator).replicate(
            any(Session.class),
            eq(ReplicationActionType.ACTIVATE),
            eq(testResource.getPath())
        );
    }

    @Test
    public void shouldUnpublishConfiguration() throws IOException, ReplicationException {
        context.requestPathInfo().setSelectorString("unpublish");

        replicationServlet.doPost(context.request(), context.response());

        Mockito.verify(replicator).replicate(
            any(Session.class),
            eq(ReplicationActionType.DEACTIVATE),
            eq(testResource.getPath())
        );
    }

    @Test
    public void shouldHandleReplicationException() throws IOException, ReplicationException {
        context.requestPathInfo().setSelectorString("publish");
        ReplicationException testException = new ReplicationException("Test replication error");

        Mockito.doThrow(testException).when(replicator).replicate(
            any(Session.class),
            eq(ReplicationActionType.ACTIVATE),
            eq(testResource.getPath())
        );

        replicationServlet.doPost(context.request(), context.response());

        assertEquals(500, context.response().getStatus());
    }

    @Test
    public void shouldReportErrorIfInvalidSelector() throws IOException, ReplicationException {
        for (String selector : new String[] {StringUtils.EMPTY, "other", null}) {
            context.requestPathInfo().setSelectorString(selector);

            replicationServlet.doPost(context.request(), context.response());
            assertEquals(400, context.response().getStatus());

            context.response().reset();
        }
    }
}
