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
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
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
import java.lang.annotation.Annotation;

/**
 * The {@link PackageEntryWriter} implementation for storing AEM TouchUI dialog definition (writes data to the
 * {@code _cq_dialog.xml} file within the current component folder before package is uploaded
 */
class CqDialogWriter extends PackageEntryWriter {

    private final XmlScope scope;

    /**
     * Basic constructor
     *
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    CqDialogWriter(Transformer transformer, XmlScope scope) {
        super(documentBuilder, transformer);
        this.scope = scope;
    }

    /**
     * Gets {@code XmlScope} value of current {@code PackageEntryWriter} implementation
     *
     * @return {@link XmlScope} value
     */
    @Override
    XmlScope getXmlScope() {
        return scope;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating {@code _cq_dialog.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if current {@code Class} is annotated with {@link Dialog} or {@link DesignDialog}; otherwise, false
     */
    @Override
    boolean isProcessed(Class<?> componentClass) {
        return XmlScope.CQ_DIALOG.equals(scope) ? componentClass.isAnnotationPresent(Dialog.class) : componentClass.isAnnotationPresent(DesignDialog.class);
    }

    /**
     * Overrides {@link PackageEntryWriter#populateDomDocument(Class, Target)} abstract method to write down contents
     * of {@code _cq_dialog.xml} file. To the targetFacade node, several XML building routines are applied in sequence: the predefined
     * dialog container builder, the common properties writer, {@code DependsOn} handlers and any {@code CustomHandler}s defined for
     * this component class
     *
     * @param componentClass The {@code Class} being processed
     * @param target   The targetFacade element of DOM {@link Document} to feed data to
     */
    @Override
    void populateDomDocument(Class<?> componentClass, Element root) {
        Annotation dialog = XmlScope.CQ_DIALOG.equals(scope) ? componentClass.getDeclaredAnnotation(Dialog.class)
                : componentClass.getDeclaredAnnotation(DesignDialog.class);
        PluginRuntime.context().getXmlUtility().mapProperties(root, dialog, scope);
        root.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.DIALOG);

        DialogLayout dialogLayout = getLayout(dialog);
        DialogContainer.getContainer(dialogLayout).build(componentClass, root);

        writeCommonProperties(componentClass, scope);
        new DependsOnTabHandler().accept(root, componentClass);
        new CustomDialogAnnotationHandler().accept(root, componentClass);
    }

    private DialogLayout getLayout(Annotation annotation) {
        if (XmlScope.CQ_DIALOG.equals(scope)) {
            return ArrayUtils.isEmpty(((Dialog) annotation).tabs()) ? ((Dialog) annotation).layout() : DialogLayout.TABS;
        } else {
            return ArrayUtils.isEmpty(((DesignDialog) annotation).tabs()) ? ((DesignDialog) annotation).layout() : DialogLayout.TABS;
        }
    }
}
