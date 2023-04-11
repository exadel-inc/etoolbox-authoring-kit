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
package com.exadel.aem.toolkit.plugin.maven.xmlcomparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

class NodeComparator implements Comparator<Node> {

    private static final List<String> PRIORITY_ATTRIBUTES = Arrays.asList(
        "jcr:primaryType",
        "sling:resourceType",
        "name");

    @Override
    public int compare(Node first, Node second) {
        String firstName = first.getNodeName();
        String secondName = second.getNodeName();
        int firstPriorityPosition = PRIORITY_ATTRIBUTES.contains(firstName)
            ? PRIORITY_ATTRIBUTES.indexOf(first.getNodeName())
            : Integer.MAX_VALUE;
        int secondPriorityPosition = PRIORITY_ATTRIBUTES.contains(secondName)
            ? PRIORITY_ATTRIBUTES.indexOf(second.getNodeName())
            : Integer.MAX_VALUE;
        return firstPriorityPosition != secondPriorityPosition
            ? firstPriorityPosition - secondPriorityPosition
            : StringUtils.compare(firstName, secondName);
    }
}
