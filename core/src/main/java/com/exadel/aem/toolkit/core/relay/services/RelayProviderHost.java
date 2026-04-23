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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

@Component(immediate = true, service = RelayProviderHost.class)
@Designate(ocd = RelayConfig.class)
public class RelayProviderHost {

    private static final String MAPPING_DELIMITER = "->";

    @Reference
    private transient ResourceResolverFactory resolverFactory;

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();

    @Activate
    private void activate(BundleContext bundleContext, RelayConfig config) {
        lock.lock();
        try {
            deactivate();
            Map<String, String> userMappings = new HashMap<>();
            for (String userMapping : config.userMappings()) {
                String source = StringUtils.substringBefore(userMapping, MAPPING_DELIMITER).trim();
                String target = StringUtils.substringAfter(userMapping, MAPPING_DELIMITER).trim();
                if (StringUtils.isAnyEmpty(source, target)) {
                    continue;
                }
                userMappings.put(source, target);
            }
            for (String pathMapping : config.pathMappings()) {
                String source = StringUtils.substringBefore(pathMapping, MAPPING_DELIMITER).trim();
                String target = StringUtils.substringAfter(pathMapping, MAPPING_DELIMITER).trim();
                if (StringUtils.isAnyEmpty(source, target)) {
                    continue;
                }
                createRelay(bundleContext, source, target, userMappings);
            }
        } finally {
            lock.unlock();
        }
    }

    @Deactivate
    private void deactivate() {
        lock.lock();
        try {
            for (ServiceRegistration<?> registration : registrations) {
                registration.unregister();
            }
            registrations.clear();
        } finally {
            lock.unlock();
        }
    }

    private void createRelay(BundleContext bundleContext, String source, String target, Map<String, String> userMappings) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(ResourceProvider.PROPERTY_ROOT, source);
        properties.put(ResourceChangeListener.PATHS, new String[]{target});
        ServiceRegistration<?> registration = bundleContext.registerService(
            new String[]{ResourceProvider.class.getName(), ResourceChangeListener.class.getName()},
            RelayProvider
                .builder()
                .resolverFactory(resolverFactory)
                .source(source)
                .target(target)
                .userMappings(userMappings)
                .build(),
            properties);
        registrations.add(registration);
    }
}

