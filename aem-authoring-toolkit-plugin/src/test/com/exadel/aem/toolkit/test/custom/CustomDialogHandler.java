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

package com.exadel.aem.toolkit.test.custom;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;

@SuppressWarnings("unused")
public class CustomDialogHandler implements DialogHandler {

    @Override
    public String getName() {
        return "customDialogProcessing";
    }

    @Override
    public void accept(Element element, Class<?> aClass) {
        visitElements(element, elt -> {
            if (StringUtils.equals(elt.getAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY), ResourceTypes.MULTIFIELD)
                && isTopLevelMultifield(elt)) {
                elt.setAttribute("multifieldSpecial", "This is added to top-level Multifields");
            }
        });
        element.getFirstChild();

    }

    private static void visitElements(Element root, Consumer<Element> visitor) {
        visitor.accept(root);
        if (!root.hasChildNodes()) {
            return;
        }
        IntStream.range(0, root.getChildNodes().getLength())
                .mapToObj(pos -> (Element) root.getChildNodes().item(pos))
                .forEach(elt -> visitElements(elt, visitor));
    }

    private static boolean isTopLevelMultifield(Element element) {
        String resourceType = Optional.ofNullable(element.getParentNode())
                .map(Node::getParentNode)
                .map(Node::getParentNode)
                .map(node -> node.getAttributes().getNamedItem(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY))
                .map(Node::getNodeValue)
                .orElse(StringUtils.EMPTY);
        return !resourceType.equals(ResourceTypes.MULTIFIELD);
    }
}
