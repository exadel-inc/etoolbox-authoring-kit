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

import javax.xml.transform.Transformer;

import com.exadel.aem.toolkit.api.handlers.TargetFacade;
import org.w3c.dom.Document;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.core.handlers.editconfig.EditConfigHandlingHelper;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * The {@link PackageEntryWriter} implementation for storing subsidiary authoring process parameters for an AEM component,
 * such as dropdown behavior, in-place editing, etc. Writes data to the {@code _cq_editConfig.xml} file within the
 * current component folder before package is uploaded
 */
class CqEditConfigWriter extends PackageEntryWriter {
    /**
     * Basic constructor
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    CqEditConfigWriter(Transformer transformer) {
        super(transformer);
    }

    /**
     * Gets {@code XmlScope} value of current {@code PackageEntryWriter} implementation
     * @return {@link XmlScope} value
     */
    @Override
    XmlScope getXmlScope() {
        return XmlScope.CQ_EDIT_CONFIG;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating {@code _cq_editConfig.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if current {@code Class} is annotated with {@link EditConfig}; otherwise, false
     */
    @Override
    boolean isProcessed(Class<?> componentClass) {
        return componentClass.isAnnotationPresent(EditConfig.class);
    }

    /**
     * Overrides {@link PackageEntryWriter#populateDomDocument(Class, TargetFacade)} abstract method to write down contents
     * of {@code _cq_editConfig.xml} file
     * @param componentClass The {@code Class} being processed
     * @param root The root element of DOM {@link Document} to feed data to
     */
    @Override
    void populateDomDocument(Class<?> componentClass, TargetFacade root) {
        EditConfig editConfig = componentClass.getDeclaredAnnotation(EditConfig.class);
        root.setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG);
        EditConfigHandlingHelper.append(root, editConfig);
    }
}