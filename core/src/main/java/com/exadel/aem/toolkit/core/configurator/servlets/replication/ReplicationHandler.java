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
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.replication.DefaultAggregateHandler;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;

import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

/**
 * Extends the default replication handler to define which configuration properties should be included during
 * replication
 */
class ReplicationHandler extends DefaultAggregateHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicationHandler.class);

    private final String[] properties;

    /**
     * Instantiates a new replication handler
     * @param properties An array of property names to be included during replication; nullable
     */
    ReplicationHandler(String[] properties) {
        this.properties = properties;
    }

    /**
     * Prepares the node at the specified path for replication by setting or removing the list of configuration
     * properties
     * @param session A JCR session
     * @param type    The replication action type
     * @param path    The path to the node to be replicated
     * @return A list of paths to be replicated
     * @throws ReplicationException If an error occurs during preparation
     */
    @Override
    public List<String> prepareForReplication(Session session, ReplicationActionType type, String path)
        throws ReplicationException {

        if (type == ReplicationActionType.ACTIVATE) {
            try {
                Node node = session.getNode(path);
                if (ArrayUtils.isNotEmpty(properties)) {
                    node.setProperty(ConfiguratorConstants.PN_REPLICATION_PROPS, properties);
                } else if (node.hasProperty(ConfiguratorConstants.PN_REPLICATION_PROPS)) {
                    node.getProperty(ConfiguratorConstants.PN_REPLICATION_PROPS).remove();
                }
            } catch (RepositoryException e) {
                LOG.error(
                    "Error managing the {} attribute at {}",
                    ConfiguratorConstants.PN_REPLICATION_PROPS,
                    path,
                    e);
            }
        } else if (type == ReplicationActionType.DEACTIVATE) {
            try {
                Node node = session.getNode(path);
                if (node.hasProperty(ConfiguratorConstants.PN_REPLICATION_PROPS)) {
                    node.getProperty(ConfiguratorConstants.PN_REPLICATION_PROPS).remove();
                }
            } catch (RepositoryException e) {
                LOG.error(
                    "Error removing the {} attribute at {}",
                    ConfiguratorConstants.PN_REPLICATION_PROPS,
                    path,
                    e);
            }
        }

        try {
            if (session.hasPendingChanges()) {
                session.save();
            }
        } catch (RepositoryException e) {
            LOG.error("Error saving changes prior to replication at {}", path, e);
        }

        return super.prepareForReplication(session, type, path);
    }
}
