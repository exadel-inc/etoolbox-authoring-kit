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

import java.util.List;
import javax.jcr.Session;

import org.apache.commons.collections4.CollectionUtils;
import com.day.cq.replication.Agent;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationPathTransformer;

import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

/**
 * Transforms replication paths for configurator data to enable property-level patching
 */
class PropertyAwarePathTransformer implements ReplicationPathTransformer {

    /**
     * Transforms the replication path by replacing the standard configurator path segment with the patch-specific
     * segment
     * @param session           The JCR session performing the replication
     * @param path              The original replication path
     * @param replicationAction The replication action being performed
     * @param agent             The replication agent executing the action
     * @return The transformed path
     */
    @Override
    public String transform(Session session, String path, ReplicationAction replicationAction, Agent agent) {
        return path.replace("/configurator/", "/configurator/patch/");
    }

    /**
     * Determines whether this transformer should be applied to the given replication action
     * @param session           The JCR session performing the replication
     * @param replicationAction The replication action being evaluated
     * @param agent             The replication agent executing the action
     * @return True or false
     */
    @Override
    public boolean accepts(Session session, ReplicationAction replicationAction, Agent agent) {
        if (!replicationAction.getPath().startsWith(ConfiguratorConstants.ROOT_PATH)) {
            return false;
        }
        List<String> properties = ReplicationContext.getProperties();
        return CollectionUtils.isNotEmpty(properties);
    }
}
