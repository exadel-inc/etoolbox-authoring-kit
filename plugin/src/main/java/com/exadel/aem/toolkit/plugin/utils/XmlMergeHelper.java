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
package com.exadel.aem.toolkit.plugin.utils;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contains utility methods for merging XML documents
 */
public class XmlMergeHelper {

    /**
     * Default (instantiation-restricting) constructor
     */
    private XmlMergeHelper() {
    }

    /**
     * Merges two XML documents. The method modifies the original document by applying the changes from the patch
     * document node by node
     * @param original Original XML document
     * @param patch    XML document containing changes to be applied to the original document
     */
    public static void merge(Document original, Document patch) {
        if (original == null || patch == null) {
            return;
        }
        if (original.getDocumentElement() == null) {
            if (patch.getDocumentElement() != null) {
                Node importedNode = original.importNode(patch.getDocumentElement(), true);
                original.appendChild(importedNode);
            }
            return;
        }
        mergeNodes(original.getDocumentElement(), patch.getDocumentElement());
    }

    /**
     * Recursively merges two XML nodes
     * @param original Node from the original document
     * @param patch    Node from the patch document
     */
    private static void mergeNodes(Node original, Node patch) {
        if (original == null || patch == null) {
            return;
        }
        if (original.getNodeType() == Node.ELEMENT_NODE && patch.getNodeType() == Node.ELEMENT_NODE) {
            mergeAttributes((Element) original, (Element) patch);
        }

        Map<String, Node> originalChildrenMap = new HashMap<>();
        NodeList originalChildren = original.getChildNodes();
        for (int i = 0; i < originalChildren.getLength(); i++) {
            Node child = originalChildren.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = child.getNodeName();
                originalChildrenMap.put(nodeName, child);
            }
        }

        NodeList patchChildren = patch.getChildNodes();
        for (int i = 0; i < patchChildren.getLength(); i++) {
            Node patchChild = patchChildren.item(i);
            if (patchChild.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = patchChild.getNodeName();
                if (originalChildrenMap.containsKey(nodeName)) {
                    Node originalChild = originalChildrenMap.remove(nodeName);
                    mergeNodes(originalChild, patchChild);
                } else {
                    Node importedNode = original.getOwnerDocument().importNode(patchChild, true);
                    original.appendChild(importedNode);
                }
            } else if (patchChild.getNodeType() == Node.TEXT_NODE) {
                String patchText = patchChild.getNodeValue();
                original.setTextContent(patchText);
            }
        }
    }

    /**
     * Merges attributes from patch element to the original element
     * @param original Element from the original document
     * @param patch    Element from the patch document
     */
    private static void mergeAttributes(Element original, Element patch) {
        NamedNodeMap patchAttributes = patch.getAttributes();
        for (int i = 0; i < patchAttributes.getLength(); i++) {
            Node attribute = patchAttributes.item(i);
            original.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
        }
    }
}
