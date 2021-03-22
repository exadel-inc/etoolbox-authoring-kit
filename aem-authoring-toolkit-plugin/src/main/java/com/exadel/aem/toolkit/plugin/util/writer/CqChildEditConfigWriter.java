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
package com.exadel.aem.toolkit.plugin.util.writer;

import javax.xml.transform.Transformer;

import org.w3c.dom.Document;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.meta.Scope;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.editconfig.EditConfigHandlingHelper;
import com.exadel.aem.toolkit.plugin.util.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * The {@link PackageEntryWriter} implementation for storing author UI aspects for child components that do not define
 * their own cq:editConfig. Writes data to the {@code _cq_childEditConfig.xml} file within the
 * current component folder before package is uploaded
 */
class CqChildEditConfigWriter extends PackageEntryWriter {
    /**
     * Basic constructor
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    CqChildEditConfigWriter(Transformer transformer) {
        super(transformer);
    }

    /**
     * Gets {@code XmlScope} value of current {@code PackageEntryWriter} implementation
     * @return {@link Scope} value
     */
    @Override
    Scope getScope() {
        return Scope.CQ_CHILD_EDIT_CONFIG;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating {@code _cq_ChildEditConfig.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if current {@code Class} is annotated with {@link ChildEditConfig}; otherwise, false
     */
    @Override
    boolean canProcess(Class<?> componentClass) {
        return componentClass.isAnnotationPresent(ChildEditConfig.class);
    }

    /**
     * Overrides {@link PackageEntryWriter#applySpecificProperties(Class, Target)} method to write down contents related
     * to the component's {@code cq:childEditConfig} node, or the {@code _cq_ChildEditConfig.xml} file
     * @param componentClass The {@code Class} being processed
     * @param target The root element of DOM {@link Document} to feed data to
     */
    @Override
    void applySpecificProperties(Class<?> componentClass, Target target) {
        ChildEditConfig childEditConfig = componentClass.getDeclaredAnnotation(ChildEditConfig.class);
        target
            .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG)
            .attributes(childEditConfig, AnnotationUtil.getPropertyMappingFilter(childEditConfig));
        EditConfigHandlingHelper.append(childEditConfig, target);
    }
}
