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

package com.exadel.aem.toolkit.core.util.writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * The {@link PackageEntryWriter} implementation for storing decoration tag properties,
 * such as class and tagName. Writes data to the {@code _cq_htmlTag.xml} file within the
 * current component folder before package is uploaded
 */
class CqHtmlTagWriter extends PackageEntryWriter {
    /**
     * Basic constructor
     * @param documentBuilder {@code DocumentBuilder} instance used to compose new XML DOM document as need by the logic
     *                                               of this writer
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    CqHtmlTagWriter(DocumentBuilder documentBuilder, Transformer transformer) {
        super(documentBuilder, transformer);
    }

    /**
     * Gets {@code XmlScope} value of current {@code PackageEntryWriter} implementation
     * @return {@link XmlScope} value
     */
    @Override
    XmlScope getXmlScope() {
        return XmlScope.CQ_HTML_TAG;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating {@code _cq_htmlTag.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if current {@code Class} is annotated with {@link HtmlTag}; otherwise, false
     */
    @Override
    boolean isProcessed(Class<?> componentClass) {
        return componentClass.isAnnotationPresent(HtmlTag.class);
    }

    /**
     * Overrides {@link PackageEntryWriter#populateDomDocument(Class, Element)} abstract method to write down contents
     * of {@code _cq_htmlTag.xml} file
     * @param componentClass The {@code Class} being processed
     * @param root The root element of DOM {@link Document} to feed data to
     */
    @Override
    void populateDomDocument(Class<?> componentClass, Element root)  {
        HtmlTag htmlTag = componentClass.getDeclaredAnnotation(HtmlTag.class);
        root.setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED);
        PluginRuntime.context().getXmlUtility().mapProperties(root, htmlTag);
        writeCommonProperties(componentClass, XmlScope.CQ_HTML_TAG);
    }
}