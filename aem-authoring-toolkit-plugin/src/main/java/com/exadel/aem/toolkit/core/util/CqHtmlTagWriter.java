package com.exadel.aem.toolkit.core.util;

import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;

/**
 * The {@link PackageEntryWriter} implementation for storing decoration tag properties,
 * such as class and tagName. Writes data to the {@code _cq_htmlTag.xml} file within the
 * current component folder before package is uploaded
 */
public class CqHtmlTagWriter extends PackageEntryWriter {
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
    void populateDomDocument(Class<?> componentClass, Element root) {
        HtmlTag htmlTag = componentClass.getDeclaredAnnotation(HtmlTag.class);
        root.setAttribute(DialogConstants.PN_CLASS, htmlTag.className());
        root.setAttribute(DialogConstants.PN_TAG_NAME, htmlTag.tagName());
    }
}