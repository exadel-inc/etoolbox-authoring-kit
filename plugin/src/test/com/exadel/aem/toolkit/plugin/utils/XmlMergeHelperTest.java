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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlMergeHelperTest {

    private DocumentBuilder documentBuilder;
    private Document original;
    private XPath xPath;

    @Before
    public void setUp() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        documentBuilder = factory.newDocumentBuilder();
        original = documentBuilder.parse(getClass().getResourceAsStream("/utils/xmlmerge/original.xml"));
        XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
    }

    @Test
    public void testOverwritingAttributes() throws IOException, SAXException, XPathExpressionException {
        String patchContent = "<jcr:root jcr:title='Override'><content><items jcr:primaryType='override'/></content></jcr:root>";
        Document patch = documentBuilder.parse(toXmlStream(patchContent));
        XmlMergeHelper.merge(original, patch);

        Element root = find(original, "/root");
        Assert.assertNotNull(root);
        Assert.assertEquals("nt:unstructured", root.getAttribute("jcr:primaryType"));
        Assert.assertEquals("Override", root.getAttribute("jcr:title"));

        Element items = find(original, "/root/content/items");
        Assert.assertNotNull(items);
        Assert.assertEquals("override", items.getAttribute("jcr:primaryType"));
    }

    @Test
    public void testOverwritingTextContent() throws IOException, SAXException, XPathExpressionException {
        String patchContent = "<jcr:root jcr:title='Override'><content><items><second><items><consectetur>New Content</consectetur></items></second></items></content></jcr:root>";
        Document patch = documentBuilder.parse(toXmlStream(patchContent));
        XmlMergeHelper.merge(original, patch);

        Element consectetur = find(original, "//consectetur");
        Assert.assertNotNull(consectetur);
        Assert.assertEquals("New Content", consectetur.getTextContent());
    }

    @Test
    public void testAddingMoreAttributes() throws IOException, SAXException, XPathExpressionException {
        String patchContent = "<jcr:root><content><items foo='bar'><first><items><dolor><value1 anotherValue='{Long}42'/></dolor></items></first></items></content></jcr:root>";
        Document patch = documentBuilder.parse(toXmlStream(patchContent));
        XmlMergeHelper.merge(original, patch);

        Element items = find(original, "//items");
        Assert.assertNotNull(items);
        Assert.assertEquals("bar", items.getAttribute("foo"));

        Element value1 = find(original, "//dolor/value1");
        Assert.assertNotNull(value1);
        Assert.assertEquals("{Long}42", value1.getAttribute("anotherValue"));
    }

    @Test
    public void testAppendingNewNodes() throws IOException, SAXException, XPathExpressionException {
        String patchContent = "<jcr:root><content><items><first><items><dolor><value3 jcr:primaryType='nt:unstructured' value='{Decimal}1000'/></dolor></items></first><third foo='bar'/></items></content></jcr:root>";
        Document patch = documentBuilder.parse(toXmlStream(patchContent));
        XmlMergeHelper.merge(original, patch);

        Element items = find(original, "//content/items");
        Assert.assertNotNull(items);
        Assert.assertEquals(3, getChildElements(items).size());

        Element dolor = find(original, "//dolor");
        Assert.assertNotNull(dolor);
        List<Element> childElements = getChildElements(dolor);
        Assert.assertEquals(3, childElements.size());
        Assert.assertEquals("{Decimal}1000", childElements.get(2).getAttribute("value"));

        Element third = find(original, "/root/content/items/third");
        Assert.assertNotNull(third);
        Assert.assertEquals("bar", third.getAttribute("foo"));
    }

    @Test
    public void testEmptyOriginalDocument() throws IOException, SAXException, XPathExpressionException {
        Document empty = documentBuilder.newDocument();
        String patchContent = "<jcr:root jcr:title='Root'><jcr:content jcr:title='Page'/></jcr:root>";
        Document patch = documentBuilder.parse(toXmlStream(patchContent));
        XmlMergeHelper.merge(empty, patch);

        Assert.assertEquals("Root", empty.getDocumentElement().getAttribute("jcr:title"));

        Element content = find(empty, "/root/content");
        Assert.assertNotNull(content);
        Assert.assertEquals("Page", content.getAttribute("jcr:title"));
    }

    @Test
    public void testEmptyPatchDocument() {
        Document patch = documentBuilder.newDocument();
        XmlMergeHelper.merge(original, patch);

        Assert.assertEquals("jcr:root", original.getDocumentElement().getNodeName());
        Assert.assertEquals("Lorem ipsum", original.getDocumentElement().getAttribute("jcr:title"));
    }

    /* ---------------
       Utility methods
       --------------- */

    private Element find(Document document, String xpath) throws XPathExpressionException {
        XPathExpression expression = xPath.compile(xpath);
        return (Element) expression.evaluate(document, XPathConstants.NODE);
    }

    private List<Element> getChildElements(Element parent) {
        List<Element> children = new ArrayList<>();
        NodeList nodeList = parent.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                children.add((Element) node);
            }
        }
        return children;
    }

    private static InputStream toXmlStream(String value) {
        String fullContent = value
            .replace("<jcr:root", "<jcr:root xmlns:jcr='http://www.jcp.org/jcr/1.0' xmlns:nt='http://www.jcp.org/jcr/nt/1.0'")
            .replace('\'', '"');
        return IOUtils.toInputStream(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + fullContent,
            "UTF-8");
    }
}
