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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.exadel.aem.toolkit.api.annotations.main.CommonProperties;
import com.exadel.aem.toolkit.api.annotations.main.CommonProperty;
import com.exadel.aem.toolkit.api.annotations.meta.DialogAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Modifies a DOM {@code Document} with XML-specific routines and data. This is intended
 * to be the final processing tier in a flow initiated by a {@code PackageEntryWriter}
 */
public class DomHandler {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided DOM {@code Document}
     * @param source   {@code Source} object used for data retrieval
     * @param document Resulting {@code Document} object
     * @param scope    Non-blank string representing an ordinary component part scope
     * @see Scopes
     */
    public void accept(Source source, Document document, String scope) {
        writeCommonProperties(source.adaptTo(Class.class), document, scope);

        if (Scopes.CQ_DIALOG.equals(scope)) {
            applyLegacyDialogHandlers(source.adaptTo(Class.class), document.getDocumentElement());
        }

    }

    /* -----------------
       Common properties
       ----------------- */

    /**
     * Maps the values set in {@link CommonProperties} annotation to nodes of a DOM document being built. The nodes are
     * picked by an {@link javax.xml.xpath.XPath}
     * @param componentClass Current {@code Class} instance
     * @param document       Resulting {@code Document} object
     * @param scope          Non-blank string representing an ordinary component part scope
     */
    private void writeCommonProperties(Class<?> componentClass, Document document, String scope) {
        Arrays.stream(componentClass.getAnnotationsByType(CommonProperty.class))
            .filter(p -> StringUtils.equals(scope, p.scope()))
            .forEach(p -> writeCommonProperty(p, getElementNodes(p.path(), document)));
    }

    /**
     * Called by {@link DomHandler#writeCommonProperties(Class, Document, String)} to process every {@link CommonProperty}
     * instance
     * @param property {@code CommonProperty} instance
     * @param elements Target {@code Node}s selected via an XPath
     */
    private static void writeCommonProperty(CommonProperty property, List<Element> elements) {
        elements.forEach(target -> target.setAttribute(property.name(), property.value()));
    }

    /**
     * Retrieves list of {@link Element} nodes from the current document selected by {@link XPath}
     * @param xPath    String xPath representation
     * @param document The document to search for nodes
     * @return List of {@code Element}s, or an empty list
     */
    private static List<Element> getElementNodes(String xPath, Document document) {
        XPath xPathInstance = XPathFactory.newInstance().newXPath();
        List<Element> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) xPathInstance.evaluate(xPath, document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Document) {
                    result.add(((Document) node).getDocumentElement());
                } else if (node instanceof Element) {
                    result.add((Element) node);
                }
            }
            if (result.isEmpty()) {
                throw new XPathExpressionException("Resolves to null or node of non-element type");
            }
        } catch (XPathExpressionException e) {
            PluginRuntime.context().getExceptionHandler().handle(String.format("Wrong XPath argument '%s'", xPath), e);
        }
        return result;
    }


    /* ----------------------
       Legacy dialog handlers
       ---------------------- */

    /**
     * Finds and triggers legacy handlers (those consuming the pair of {@code Element} and {@code Class<?>} references)
     * that operate class-wide
     * @param componentClass The {@code Class<?>} that a legacy handler processes
     * @param element        DOM {@code Element} object
     */
    @SuppressWarnings({"deprecation", "squid:S1905"}) // DialogHandler reference and DialogHandler#accept(Element, Class)
    // method are retained for compatibility and will be removed in a version after 2.0.2
    private static void applyLegacyDialogHandlers(Class<?> componentClass, Element element) {
        List<DialogAnnotation> customAnnotations = getLegacyDialogAnnotations(componentClass);
        PluginRuntime.context().getReflection().getHandlers().stream()
            .filter(handler -> handler instanceof DialogHandler)
            .map(handler -> (DialogHandler) handler)
            .filter(handler -> customAnnotations.stream()
                .anyMatch(annotation -> StringUtils.equals(annotation.source(), handler.getName())))
            .forEach(handler -> handler.accept(element, componentClass));
    }

    /**
     * Retrieves list of {@link DialogAnnotation} instances defined for the current {@code Class}
     * @param componentClass The {@code Class} being processed
     * @return List of values, empty or non-empty
     */
    @SuppressWarnings("deprecation") // DialogAnnotation processing is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    private static List<DialogAnnotation> getLegacyDialogAnnotations(Class<?> componentClass) {
        return Arrays.stream(componentClass.getDeclaredAnnotations())
            .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class) != null)
            .map(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class))
            .collect(Collectors.toList());
    }
}
