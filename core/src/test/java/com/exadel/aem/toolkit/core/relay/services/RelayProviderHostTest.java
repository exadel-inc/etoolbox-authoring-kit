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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
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

    private static final String PATH_SOURCE = "/content/source";

    private static final String PATH_MAPPING_A = "{\"from\":\"/content/source\",\"to\":\"/content/target\"}";
    private static final String PATH_MAPPING_A_NEW_TARGET = "{\"from\":\"/content/source\",\"to\":\"/content/target-updated\"}";
    private static final String PATH_MAPPING_B = "{\"from\":\"/content/source2\",\"to\":\"/content/target2\"}";
    private static final String PATH_MAPPING_MISSING_TO = "{\"from\":\"/content/source\"}";
    private static final String PATH_MAPPING_A_DUP_SOURCE = "{\"from\":\"/content/source\",\"to\":\"/content/other\"}";

    private static final String USER_MAPPING_A = "{\"from\":\"admin\",\"to\":\"service-user\"}";
    private static final String USER_MAPPING_A_DUP = "{\"from\":\"admin\",\"to\":\"other-service\"}";
    private static final String USER_MAPPING_INVALID = "{\"from\":\"admin\"}";

    private static final String CHANGE_SAMPLE_A = "{\"path\":\"/content/dam\"}";
    private static final String CHANGE_SAMPLE_BLANK_PATH = "{\"path\":\"\"}";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    /* ------------------
       Service activation
       ------------------ */

    @Test
    public void shouldRegisterForEachValidMapping() throws InvalidSyntaxException {
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A, PATH_MAPPING_B));

        assertEquals(2, countRegisteredProviders());
    }

    @Test
    public void shouldParseUserMappingsAndSamples() throws InvalidSyntaxException {
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newExtendedProps(
                true,
                new String[]{PATH_MAPPING_A},
                new String[]{StringUtils.EMPTY, USER_MAPPING_INVALID, USER_MAPPING_A, USER_MAPPING_A_DUP},
                new String[]{StringUtils.EMPTY, CHANGE_SAMPLE_BLANK_PATH, CHANGE_SAMPLE_A}));
        assertEquals(1, countRegisteredProviders());
    }

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
    public void shouldSkipDuplicatePathMappings() throws InvalidSyntaxException {
        // Blank entry → null mapping → skipped; second mapping with same source → skipped
        context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, StringUtils.EMPTY, PATH_MAPPING_A, PATH_MAPPING_A_DUP_SOURCE));
        assertEquals(1, countRegisteredProviders());
    }

    @Test
    public void shouldSkipShadowedPath() throws InvalidSyntaxException {
        context.bundleContext().registerService(
            ResourceProvider.class.getName(),
            new StubResourceProvider(),
            newExtProviderProps("external-provider", PATH_SOURCE));

        context.registerInjectActivateService(new RelayProviderHost(), newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());
    }

    @Test
    public void shouldSkipShadowingByUnnamedProvider() throws InvalidSyntaxException {
        // Null PROPERTY_NAME → fallback to bundle symbolic name → relay is still shadowed
        context.bundleContext().registerService(
            ResourceProvider.class.getName(),
            new StubResourceProvider(),
            newExtProviderProps(null, PATH_SOURCE));
        context.registerInjectActivateService(new RelayProviderHost(), newProps(true, PATH_MAPPING_A));
        assertEquals(1, countRegisteredProviders());

        // Empty PROPERTY_NAME → same fallback behavior
        context.bundleContext().registerService(
            ResourceProvider.class.getName(),
            new StubResourceProvider(),
            newExtProviderProps("", "/content/source2"));
        context.registerInjectActivateService(new RelayProviderHost(), newProps(true, PATH_MAPPING_B));
        assertEquals(2, countRegisteredProviders());
    }

    @Test
    public void shouldRegisterWithRootlessExternalProvider() throws InvalidSyntaxException {
        // Provider with no PROPERTY_ROOT → not in the provided-paths map → relay is not shadowed
        context.bundleContext().registerService(
            ResourceProvider.class.getName(),
            new StubResourceProvider(),
            newExtProviderProps("rootless-provider", null));
        context.registerInjectActivateService(new RelayProviderHost(), newProps(true, PATH_MAPPING_A));
        assertEquals(2, countRegisteredProviders());
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
    public void shouldReregisterWhenTargetChanges() throws InvalidSyntaxException {
        RelayProviderHost host = context.registerInjectActivateService(
            new RelayProviderHost(),
            newProps(true, PATH_MAPPING_A));

        assertEquals(1, countRegisteredProviders());
        assertEquals("/content/target", getRegisteredTarget(PATH_SOURCE));

        MockOsgi.activate(host, context.bundleContext(), newProps(true, PATH_MAPPING_A_NEW_TARGET));

        assertEquals(1, countRegisteredProviders());
        assertEquals("/content/target-updated", getRegisteredTarget(PATH_SOURCE));
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

    @SuppressWarnings("SameParameterValue")
    private String getRegisteredTarget(String sourcePath) throws InvalidSyntaxException {
        ServiceReference<?>[] refs = context
            .bundleContext()
            .getAllServiceReferences(ResourceProvider.class.getName(), null);
        if (refs == null) {
            return null;
        }
        for (ServiceReference<?> ref : refs) {
            Object root = ref.getProperty(ResourceProvider.PROPERTY_ROOT);
            if (sourcePath.equals(root)) {
                String[] paths = (String[]) ref.getProperty(ResourceChangeListener.PATHS);
                return ArrayUtils.isNotEmpty(paths) ? paths[0] : null;
            }
        }
        return null;
    }

    private static Dictionary<String, Object> newExtProviderProps(String name, String root) {
        Dictionary<String, Object> props = new Hashtable<>();
        if (root != null) {
            props.put(ResourceProvider.PROPERTY_ROOT, root);
        }
        if (name != null) {
            props.put(ResourceProvider.PROPERTY_NAME, name);
        }
        return props;
    }

    private static Map<String, Object> newProps(boolean enabled, String... pathMappings) {
        Map<String, Object> props = new HashMap<>();
        props.put("enabled", enabled);
        props.put("pathMappings", pathMappings);
        return props;
    }

    private static Map<String, Object> newExtendedProps(
        boolean enabled,
        String[] pathMappings,
        String[] userMappings,
        String[] announcedPaths) {
        Map<String, Object> props = new HashMap<>();
        props.put("enabled", enabled);
        props.put("pathMappings", pathMappings);
        props.put("userMappings", userMappings);
        props.put("announcedPaths", announcedPaths);
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
