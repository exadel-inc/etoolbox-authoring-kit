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
package com.exadel.aem.toolkit.core.configurator.servlets.replication;

import java.util.List;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationContentFilter;
import com.day.cq.replication.ReplicationContentFilterFactory;

import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

/**
 * Creates replication content filters that selectively include only specific properties during replication
 * of configurator data
 */
class PropertyAwareFilterFactory implements ReplicationContentFilterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public ReplicationContentFilter createFilter(ReplicationAction replicationAction) {
        if (!replicationAction.getPath().startsWith(ConfiguratorConstants.ROOT_PATH)) {
            return null;
        }
        List<String> properties = ReplicationContext.getProperties();
        if (CollectionUtils.isEmpty(properties)) {
            return null;
        }

        return new ReplicationContentFilter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean accepts(Node node) {
                try {
                    return node.getPath().startsWith(ConfiguratorConstants.ROOT_PATH)
                        && node.getPath().endsWith(ConfiguratorConstants.SUFFIX_SLASH_DATA);
                } catch (RepositoryException e) {
                    return false;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean accepts(Property property) {
                try {
                    return JcrConstants.JCR_PRIMARYTYPE.equals(property.getName())
                    || JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY.equals(property.getName())
                    || properties.contains(property.getName());
                } catch (RepositoryException e) {
                    return false;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean allowsDescent(Node node) {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public List<String> getFilteredPaths() {
                return null;
            }
        };
    }
}
