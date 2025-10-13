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
package com.exadel.aem.toolkit.core.configurator.services;

import java.io.IOException;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

class ConfigurationAdminFacade implements ConfigurationAdmin {
    private final ConfigurationAdmin delegate;

    ConfigurationAdminFacade(ConfigurationAdmin delegate) {
        this.delegate = delegate;
    }

    @Override
    public Configuration createFactoryConfiguration(String factoryPid, String location) throws IOException {
        return delegate.createFactoryConfiguration(factoryPid, location);
    }

    @Override
    public Configuration createFactoryConfiguration(String factoryPid) throws IOException {
        return delegate.createFactoryConfiguration(factoryPid);
    }

    @Override
    public Configuration getConfiguration(String pid, String location) throws IOException {
        return getConfiguration(pid);
    }

    @Override
    public Configuration getConfiguration(String pid) throws IOException {
        return delegate.getConfiguration(pid);
    }

    @Override
    public Configuration[] listConfigurations(String filter) throws InvalidSyntaxException, IOException {
        return delegate.listConfigurations(filter);
    }
}
