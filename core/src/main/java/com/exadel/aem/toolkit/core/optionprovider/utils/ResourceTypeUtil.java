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
package com.exadel.aem.toolkit.core.optionprovider.utils;

import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.TagConstants;

/**
 * Contains utility methods for detecting the type of resource
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class ResourceTypeUtil {

    /**
     * Default (instantiation-preventing) constructor
     */
    private ResourceTypeUtil() {
    }

    /**
     * Gets whether the current datasource path is a path to a tags folder
     * @param resource {@code Resource} instance representing the (expected) datasource
     * @return True if this datasource contains tags; otherwise, false
     */
    public static boolean isTagCollection(Resource resource) {
        try {
            Node resourceNode = Objects.requireNonNull(resource.adaptTo(Node.class));
            if (!resourceNode.isNodeType(JcrConstants.NT_FOLDER) && !resourceNode.isNodeType(TagConstants.NT_TAG)) {
                return false;
            }
            return resourceNode.hasNodes()
                && resourceNode.getNodes().nextNode().isNodeType(TagConstants.NT_TAG);
        } catch (RepositoryException | NullPointerException e) {
            return false;
        }
    }
}
