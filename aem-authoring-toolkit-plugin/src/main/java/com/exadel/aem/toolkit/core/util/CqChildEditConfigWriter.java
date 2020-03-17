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

package com.exadel.aem.toolkit.core.util;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import java.util.Arrays;

/**
 * The {@link PackageEntryWriter} implementation for storing author UI aspects for child components that do not define
 * their own cq:editConfig. Writes data to the {@code _cq_childEditConfig.xml} file within the
 * current component folder before package is uploaded
 */
public class CqChildEditConfigWriter extends PackageEntryWriter {
    CqChildEditConfigWriter(DocumentBuilder documentBuilder, Transformer transformer) {
        super(documentBuilder, transformer);
    }

    /**
     * Gets {@code XmlScope} value of current {@code PackageEntryWriter} implementation
     * @return {@link XmlScope} value
     */
    @Override
    XmlScope getXmlScope() {
        return XmlScope.CQ_CHILD_EDIT_CONFIG;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating {@code _cq_ChildEditConfig.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if current {@code Class} is annotated with {@link ChildEditConfig}; otherwise, false
     */
    @Override
    boolean isProcessed(Class<?> componentClass) {
        return componentClass.isAnnotationPresent(ChildEditConfig.class);
    }

    /**
     * Overrides {@link PackageEntryWriter#populateDomDocument(Class, Element)} abstract method to write down contents
     * of {@code _cq_ChildEditConfig.xml} file
     * @param componentClass The {@code Class} being processed
     * @param root The root element of DOM {@link Document} to feed data to
     */
    @Override
    void populateDomDocument(Class<?> componentClass, Element root) {
        ChildEditConfig actions = componentClass.getDeclaredAnnotation(ChildEditConfig.class);
        root.setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG);
        root.setAttribute(DialogConstants.PN_ACTIONS, Arrays.toString(actions.actions()));
        writeCommonProperties(componentClass, getXmlScope());
    }
}