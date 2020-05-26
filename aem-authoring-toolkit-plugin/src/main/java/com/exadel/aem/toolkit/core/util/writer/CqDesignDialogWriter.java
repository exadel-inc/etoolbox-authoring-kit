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

import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.core.handlers.assets.dependson.DependsOnTabHandler;
import com.exadel.aem.toolkit.core.handlers.container.DialogContainer;
import com.exadel.aem.toolkit.core.handlers.widget.common.CustomDialogAnnotationHandler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;

/**
 * The {@link PackageEntryWriter} implementation for storing AEM TouchUI dialog definition (writes data to the
 * {@code _cq_design_dialog.xml} file within the current component folder before package is uploaded
 */
class CqDesignDialogWriter extends PackageEntryWriter {
    /**
     * Basic constructor
     * @param documentBuilder {@code DocumentBuilder} instance used to compose new XML DOM document as need by the logic
     *                                               of this writer
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    CqDesignDialogWriter(DocumentBuilder documentBuilder, Transformer transformer) {
        super(documentBuilder, transformer);
    }

    /**
     * Gets {@code XmlScope} value of current {@code PackageEntryWriter} implementation
     * @return {@link XmlScope} value
     */
    @Override
    XmlScope getXmlScope() {
        return XmlScope.CQ_DESIGN_DIALOG;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating {@code _cq_design_dialog.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if current {@code Class} is annotated with {@link DesignDialog}; otherwise, false
     */
    @Override
    boolean isProcessed(Class<?> componentClass) {
        return componentClass.isAnnotationPresent(DesignDialog.class);
    }

    /**
     * Overrides {@link PackageEntryWriter#populateDomDocument(Class, Element)} abstract method to write down contents
     * of {@code _cq_design_dialog.xml} file. To the root node, several XML building routines are applied in sequence: the predefined
     * dialog container builder, the common properties writer, {@code DependsOn} handlers and any {@code CustomHandler}s defined for
     * this component class
     * @param componentClass The {@code Class} being processed
     * @param root The root element of DOM {@link Document} to feed data to
     */
    @Override
    void populateDomDocument(Class<?> componentClass, Element root) {
        DesignDialog designDialog = componentClass.getDeclaredAnnotation(DesignDialog.class);
        PluginRuntime.context().getXmlUtility().mapProperties(root, designDialog);
        root.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.DIALOG);

        DialogLayout dialogLayout = ArrayUtils.isEmpty(designDialog.tabs()) ? designDialog.layout() : DialogLayout.TABS;
        DialogContainer.getContainer(dialogLayout).build(componentClass, root);

        writeCommonProperties(componentClass, XmlScope.CQ_DIALOG);
        new DependsOnTabHandler().accept(root, componentClass);
        new CustomDialogAnnotationHandler().accept(root, componentClass);
    }
}
