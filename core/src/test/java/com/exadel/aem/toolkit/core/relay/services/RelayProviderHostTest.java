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

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.junit.Rule;
import org.junit.Test;
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

    @Test
    public void shouldNotRegisterServicesWhenDisabled() throws Exception {
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(false, PATH_MAPPING_A));

        assertEquals(0, countRegisteredProviders());
    }

    @Test
    public void shouldNotRegisterServicesWithoutValidPathMappings() throws Exception {
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true));

        assertEquals(0, countRegisteredProviders());
    }

    @Test
    public void shouldSkipInvalidPathMappings() throws Exception {
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_MISSING_TO));

        assertEquals(0, countRegisteredProviders());
    }

    @Test
    public void shouldRegisterServicesForEachValidPathMapping() throws Exception {
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A, PATH_MAPPING_B));

        assertEquals(2, countRegisteredProviders());
    }

    @Test
    public void shouldUnregisterServicesOnDeactivate() throws Exception {
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

    private int countRegisteredProviders() throws Exception {
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
}
