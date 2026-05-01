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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

import com.exadel.aem.toolkit.core.AemContextFactory;

public class RelayProviderHostTest {

    private static final String PATH_MAPPING_A = "{\"from\":\"/content/source\",\"to\":\"/content/target\"}";
    private static final String PATH_MAPPING_B = "{\"from\":\"/content/source2\",\"to\":\"/content/target2\"}";
    private static final String PATH_MAPPING_MISSING_TO = "{\"from\":\"/content/source\"}";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    /* ------------------
       Service activation
       ------------------ */

    @Test
    public void shouldNotRegisterForInvalidConfig() throws InvalidSyntaxException {
        // Disabled with a valid mapping
        context.registerInjectActivateService(new RelayProviderHost(), newProps(false, PATH_MAPPING_A));
        assertEquals(0, countRegisteredProviders());

        // No path mappings
        context.registerInjectActivateService(new RelayProviderHost(), newProps(true));
        assertEquals(0, countRegisteredProviders());

        // Invalid mapping (missing "to" value)
        context.registerInjectActivateService(new RelayProviderHost(), newProps(true, PATH_MAPPING_MISSING_TO));
        assertEquals(0, countRegisteredProviders());
    }

    @Test
    public void shouldRegisterForEachValidMapping() throws InvalidSyntaxException {
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A, PATH_MAPPING_B));

        assertEquals(2, countRegisteredProviders());
    }

    @Test
    public void shouldSkipShadowedPath() throws InvalidSyntaxException {
        Dictionary<String, Object> extProps = new Hashtable<>();
        extProps.put(ResourceProvider.PROPERTY_ROOT, "/content/source");
        extProps.put(ResourceProvider.PROPERTY_NAME, "external-provider");
        context.bundleContext().registerService(ResourceProvider.class.getName(), new StubResourceProvider(), extProps);

        context.registerInjectActivateService(new RelayProviderHost(), newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());
    }

    /* ---------------------
       Service re-activation
       --------------------- */

    @Test
    public void shouldUpdateProviderOnReactivate() throws InvalidSyntaxException {
        RelayProviderHost host = context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());

        MockOsgi.activate(host, context.bundleContext(), newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());
    }

    @Test
    public void shouldUnregisterRemovedMappingOnReactivate() throws InvalidSyntaxException {
        RelayProviderHost host = context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A, PATH_MAPPING_B));

        assertEquals(2, countRegisteredProviders());

        MockOsgi.activate(host, context.bundleContext(), newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());
    }

    @Test
    public void shouldRegisterAddedMappingOnReactivate() throws InvalidSyntaxException {
        RelayProviderHost host = context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());

        MockOsgi.activate(host, context.bundleContext(), newProps(true, PATH_MAPPING_A, PATH_MAPPING_B));

        assertEquals(2, countRegisteredProviders());
    }

    /* ------------
       Deactivation
       ------------ */

    @Test
    public void shouldUnregisterOnDeactivate() throws InvalidSyntaxException {
        RelayProviderHost host = context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());

        MockOsgi.deactivate(host, context.bundleContext());

        assertEquals(0, countRegisteredProviders());
    }

    /* ---------------
       Utility methods
       --------------- */

    private int countRegisteredProviders() throws InvalidSyntaxException {
        ServiceReference<?>[] refs = context.bundleContext()
            .getAllServiceReferences(ResourceProvider.class.getName(), null);
        return refs != null ? refs.length : 0;
    }

    private static Map<String, Object> newProps(boolean enabled, String... pathMappings) {
        Map<String, Object> props = new HashMap<>();
        props.put("enabled", enabled);
        props.put("pathMappings", pathMappings);
        return props;
    }

    private static class StubResourceProvider extends ResourceProvider<Void> {

        @Override
        public Resource getResource(
            @Nonnull ResolveContext<Void> ctx,
            @Nonnull String path,
            @Nonnull ResourceContext resourceContext,
            Resource parent) {
            return null;
        }

        @Override
        public Iterator<Resource> listChildren(@Nonnull ResolveContext<Void> ctx, @Nonnull Resource parent) {
            return null;
        }
    }
}
