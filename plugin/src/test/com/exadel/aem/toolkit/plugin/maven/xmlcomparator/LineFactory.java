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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.exadel.aem.toolkit.core.CoreConstants;

class LineFactory {

    private static final NodeComparator NODE_COMPARATOR = new NodeComparator();

    private static final String NAMESPACE_PREFIX = "xmlns:";
    private static final String TAG_START = "<";
    private static final String TAG_END = ">";
    private static final String ELLIPSIS = "...";

    private static final String ATTRIBUTE_TEMPLATE = "%s=\"%s\"";
    private static final String CLOSING_TAG_TEMPLATE = "</%s>";
    private static final String XPATH_NAME_TEMPLATE = "%s[%d]";

    private final Element root;
    private final String focusedPath;
    private final int lengthLimit;

    public LineFactory(Element root, String focusedPath, int lengthLimit) {
        this.root = root;
        this.focusedPath = StringUtils.substringBeforeLast(focusedPath, XmlComparatorConstants.SEPARATOR_ATTRIBUTE);
        this.lengthLimit = lengthLimit;
    }

    public List<Line> getLines() {
        return getLines(root, "/root[1]", 0);
    }

    private List<Line> getLines(Element element, String path, int indentLevel) {
        boolean isWithinFocus = StringUtils.startsWith(
            StringUtils.substringBeforeLast(path, XmlComparatorConstants.SEPARATOR_ATTRIBUTE),
            focusedPath);

        String indent = StringUtils.repeat(XmlComparatorConstants.LOG_INDENT, indentLevel);
        String nestedIndent = indent + XmlComparatorConstants.LOG_INDENT;

        List<Line> tagStart = isWithinFocus
            ? getLines(path, indent + TAG_START + element.getTagName())
            : getLines(path, indent + TAG_START + element.getTagName() + ELLIPSIS);
        List<Line> attributeSection = new ArrayList<>();

        String ending = (element.hasChildNodes() ? StringUtils.EMPTY : CoreConstants.SEPARATOR_SLASH) + TAG_END;

        if (isWithinFocus) {
            List<Node> attributeNodes = getSortedAttributeNodes(element);
            for (Node node : attributeNodes) {
                String attributePath = path + XmlComparatorConstants.SEPARATOR_ATTRIBUTE + stripNamespace(node.getNodeName());
                String attributeContent = nestedIndent + String.format(ATTRIBUTE_TEMPLATE, node.getNodeName(), node.getNodeValue());
                List<Line> nodeLines = getLines(attributePath, attributeContent);
                attributeSection.addAll(nodeLines);
            }
            attributeSection.get(attributeSection.size() - 1).append(ending);
        } else {
            tagStart.get(tagStart.size() - 1).append(ending);
        }

        List<Line> childNodesSection = new ArrayList<>();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (!(childNode instanceof Element)) {
                continue;
            }
            Element childElement = (Element) childNode;
            String childPath = StringUtils.stripEnd(path, CoreConstants.SEPARATOR_SLASH)
                + CoreConstants.SEPARATOR_SLASH
                + getChildXPathName(childElement);
            List<Line> childLines = getLines(childElement, childPath, indentLevel + 1);
            childNodesSection.addAll(childLines);
        }

        List<Line> tagEnd = new ArrayList<>();
        if (element.hasChildNodes()) {
            tagEnd.addAll(getLines(
                path + CoreConstants.SEPARATOR_SLASH,
                indent + String.format(CLOSING_TAG_TEMPLATE, element.getTagName())));
        }
        return Lists.newArrayList(Iterables.concat(tagStart, attributeSection, childNodesSection, tagEnd));
    }

    private List<Line> getLines(String id, String text) {
        int length = StringUtils.length(text);
        if (length <= lengthLimit) {
            return new ArrayList<>(Collections.singletonList(new Line(id, text)));
        }
        List<Line> result = new ArrayList<>();
        int position = 0;
        do {
            String currentId = (position == 0) ? id : null;
            String currentValue = StringUtils.substring(text, position, Math.min(position + lengthLimit, length));
            result.add(new Line(currentId, currentValue));
            position += lengthLimit;
        } while (position < length);
        return result;
    }


    private static List<Node> getSortedAttributeNodes(Element element) {
        List<Node> result = new ArrayList<>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            if (!StringUtils.startsWith(item.getNodeName(), NAMESPACE_PREFIX)) {
                result.add(item);
            }
        }
        result.sort(NODE_COMPARATOR);
        return result;
    }

    /* ---------------
       Utility methods
       --------------- */

    private static String stripNamespace(String value) {
        if (!StringUtils.contains(value, CoreConstants.SEPARATOR_COLON)) {
            return value;
        }
        return StringUtils.substringAfter(value, CoreConstants.SEPARATOR_COLON);
    }

    private static String getChildXPathName(Element value) {
        int siblingCount = 1;
        Node previous = value.getPreviousSibling();
        while (previous != null) {
            if (previous.getNodeType() == value.getNodeType()
                && StringUtils.equals(previous.getNodeName(), value.getNodeName())) {
                siblingCount++;
            }
            previous = previous.getPreviousSibling();
        }
        return String.format(XPATH_NAME_TEMPLATE, value.getNodeName(), siblingCount);
    }
}
