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
import javax.servlet.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
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
import org.osgi.framework.BundleContext;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationContentFilterFactory;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.ReplicationPathTransformer;
import com.day.cq.replication.Replicator;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationServletTest {

    @Rule
    public AemContext context = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);

    @Mock
    private Replicator replicator;

    @Mock
    private ConfigChangeListener configChangeListener;

    private ReplicationServlet replicationServlet;
    private Resource testResource;

    @Before
    public void setUp() throws PersistenceException {
        testResource = context.create().resource("/content/config/test");
        context.resourceResolver().commit();

        context.request().setResource(testResource);

        context.registerService(Replicator.class, replicator);
        context.registerService(ConfigChangeListener.class, configChangeListener);

        Mockito.when(configChangeListener.isEnabled()).thenReturn(true);

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
        assertEquals(HttpStatus.SC_OK, context.response().getStatus());
    }

    @Test
    public void shouldPublishAllProperties() throws IOException, ReplicationException {
        context.requestPathInfo().setSelectorString("publish");
        context.request().setParameterMap(java.util.Collections.singletonMap("properties", "all"));

        replicationServlet.doPost(context.request(), context.response());

        Mockito.verify(replicator).replicate(
            any(Session.class),
            eq(ReplicationActionType.ACTIVATE),
            eq(testResource.getPath())
        );
        assertEquals(HttpStatus.SC_OK, context.response().getStatus());
    }

    @Test
    public void shouldUnpublishConfiguration() throws IOException, ReplicationException {
        context.requestPathInfo().setSelectorString("unpublish");

        replicationServlet.doPost(context.request(), context.response());

        Mockito.verify(replicator).replicate(
            any(Session.class),
            eq(ReplicationActionType.DEACTIVATE),
            eq(testResource.getPath()),
            any(ReplicationOptions.class)
        );
        assertEquals(HttpStatus.SC_OK, context.response().getStatus());
    }

    @Test
    public void shouldCleanUpEmptyNodeAfterUnpublish() throws IOException, ReplicationException {
        Resource emptyResource = context.create().resource("/content/config/empty");
        context.resourceResolver().commit();
        context.request().setResource(emptyResource);

        assertNotNull(context.resourceResolver().getResource(emptyResource.getPath()));

        context.requestPathInfo().setSelectorString("unpublish");

        replicationServlet.doPost(context.request(), context.response());

        Mockito.verify(replicator).replicate(
            any(Session.class),
            eq(ReplicationActionType.DEACTIVATE),
            eq(emptyResource.getPath()),
            any(ReplicationOptions.class)
        );
        assertNull(context.resourceResolver().getResource(emptyResource.getPath()));
    }

    @Test
    public void shouldNotCleanUpNodeWithChildrenAfterUnpublish() throws IOException {
        Resource parentResource = context.create().resource("/content/config/parent");
        context.create().resource("/content/config/parent/child");
        context.resourceResolver().commit();
        context.request().setResource(parentResource);

        assertNotNull(context.resourceResolver().getResource(parentResource.getPath()));

        context.requestPathInfo().setSelectorString("unpublish");
        replicationServlet.doPost(context.request(), context.response());

        assertNotNull(context.resourceResolver().getResource(parentResource.getPath()));
    }

    @Test
    public void shouldHandleReplicationException() throws IOException, ReplicationException {
        context.requestPathInfo().setSelectorString("publish");
        ReplicationException testException = new ReplicationException("NOT AN EXCEPTION: testing ReplicationServlet logic");

        Mockito.doThrow(testException).when(replicator).replicate(
            any(Session.class),
            eq(ReplicationActionType.ACTIVATE),
            eq(testResource.getPath())
        );

        replicationServlet.doPost(context.request(), context.response());

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, context.response().getStatus());
    }

    @Test
    public void shouldReportErrorIfInvalidSelector() throws IOException {
        for (String selector : new String[] {StringUtils.EMPTY, "other", null}) {
            context.requestPathInfo().setSelectorString(selector);

            replicationServlet.doPost(context.request(), context.response());

            assertEquals(HttpStatus.SC_BAD_REQUEST, context.response().getStatus());
            context.response().reset();
        }
    }

    @Test
    public void shouldReportErrorWhenDisabled() throws IOException, ReplicationException {
        Mockito.when(configChangeListener.isEnabled()).thenReturn(false);

        context.requestPathInfo().setSelectorString("publish");

        replicationServlet.doPost(context.request(), context.response());

        assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, context.response().getStatus());
        Mockito.verify(replicator, Mockito.never()).replicate(
            any(Session.class),
            any(ReplicationActionType.class),
            any(String.class)
        );
    }

    @Test
    public void shouldRegisterSubsidiaryServicesWhenEnabled() {
        BundleContext bundleContext = context.bundleContext();

        assertNotNull(bundleContext.getServiceReference(Filter.class));
        assertNotNull(bundleContext.getServiceReference(ReplicationPathTransformer.class));
        assertNotNull(bundleContext.getServiceReference(ReplicationContentFilterFactory.class));
    }

    @Test
    public void shouldNotRegisterSubsidiaryServicesWhenDisabled() {
        Mockito.when(configChangeListener.isEnabled()).thenReturn(false);

        AemContext disabledContext = AemContextFactory.newInstance(ResourceResolverType.JCR_OAK);
        disabledContext.registerService(Replicator.class, replicator);
        disabledContext.registerService(ConfigChangeListener.class, configChangeListener);

        disabledContext.registerInjectActivateService(new ReplicationServlet());

        BundleContext bundleContext = disabledContext.bundleContext();

        assertNull(bundleContext.getServiceReference(Filter.class));
        assertNull(bundleContext.getServiceReference(ReplicationPathTransformer.class));
        assertNull(bundleContext.getServiceReference(ReplicationContentFilterFactory.class));
    }
}
