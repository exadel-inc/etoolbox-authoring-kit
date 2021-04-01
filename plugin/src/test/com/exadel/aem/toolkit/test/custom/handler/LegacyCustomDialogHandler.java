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
package com.exadel.aem.toolkit.test.custom.handler;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.api.runtime.Injected;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.test.custom.annotation.CustomLegacyDialogAnnotation;

@Handles(value = CustomLegacyDialogAnnotation.class)
@SuppressWarnings("deprecation") // References to DialogHandler, RuntimeContext are retained for compatibility
public class LegacyCustomDialogHandler implements DialogHandler {

    @Override
    public String getName() {
        return "customDialogProcessing";
    }

    @Injected
    private RuntimeContext runtimeContext;


    @Override
    public void accept(Element element, Class<?> cls) {
        element.setAttribute("className", cls.getSimpleName());
        legacyVisitElements(element, elt -> {
            if (StringUtils.equals(elt.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE), ResourceTypes.MULTIFIELD)
                && isLegacyTopLevelMultifield(elt)) {
                elt.setAttribute("multifieldSpecial", "This is added to top-level Multifields");
            }
        });
        Element customChild = runtimeContext.getXmlUtility().createNodeElement("customChild");
        Element content = runtimeContext.getXmlUtility().getChildElement(element, "content");
        Element extItems = runtimeContext.getXmlUtility().getChildElement(content, "items");
        Element column = runtimeContext.getXmlUtility().getChildElement(extItems, "column");
        Element intItems = runtimeContext.getXmlUtility().getChildElement(column, "items");
        intItems.appendChild(customChild);
    }

    @Override
    public void accept(Source source, Target target) {
        visitElements(target, elt -> {
            if (StringUtils.equals(elt.getAttributes().get(DialogConstants.PN_SLING_RESOURCE_TYPE), ResourceTypes.MULTIFIELD)
                    && isTopLevelMultifield(elt)) {
                elt.attribute("multifieldSpecial", "This is added to top-level Multifields");
            }
        });
    }

    private static void visitElements(Target root, Consumer<Target> visitor) {
        visitor.accept(root);
        if (root.getChildren().isEmpty()) {
            return;
        }
        root.getChildren().forEach(elt -> visitElements(elt, visitor));
    }

    private static boolean isTopLevelMultifield(Target target) {
        String resourceType = Optional.ofNullable(target.getParent())
            .map(Target::getParent)
            .map(Target::getParent)
            .map(t -> t.getAttributes().get(DialogConstants.PN_SLING_RESOURCE_TYPE))
            .orElse(StringUtils.EMPTY);
        return !resourceType.equals(ResourceTypes.MULTIFIELD);
    }

    private static void legacyVisitElements(Element root, Consumer<Element> visitor) {
        visitor.accept(root);
        if (!root.hasChildNodes()) {
            return;
        }
        IntStream.range(0, root.getChildNodes().getLength())
            .mapToObj(pos -> (Element) root.getChildNodes().item(pos))
            .forEach(elt -> legacyVisitElements(elt, visitor));
    }

    private static boolean isLegacyTopLevelMultifield(Element element) {
        String resourceType = Optional.ofNullable(element.getParentNode())
            .map(Node::getParentNode)
            .map(Node::getParentNode)
            .map(node -> node.getAttributes().getNamedItem(DialogConstants.PN_SLING_RESOURCE_TYPE))
            .map(Node::getNodeValue)
            .orElse(StringUtils.EMPTY);
        return !resourceType.equals(ResourceTypes.MULTIFIELD);
    }
}
