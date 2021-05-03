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
package com.exadel.aem.toolkit.plugin.writers;

import javax.xml.transform.Transformer;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;

/**
 * The {@link PackageEntryWriter} implementation for storing author UI aspects for child components that do not define
 * their own {@code cq:editConfig}. Writes data to the {@code _cq_childEditConfig.xml} file within the
 * current component folder before the package is uploaded
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
     * Gets {@code Scope} value of current {@code PackageEntryWriter} implementation
     * @return String value representing a valid scope
     */
    @Override
    String getScope() {
        return Scopes.CQ_CHILD_EDIT_CONFIG;
    }

    /**
     * Gets whether current {@code Class} is eligible for populating a {@code _cq_ChildEditConfig.xml} structure
     * @param componentClass The {@code Class} under consideration
     * @return True if the current {@code Class} is annotated with {@link ChildEditConfig}; otherwise, false
     */
    @Override
    boolean canProcess(Class<?> componentClass) {
        return componentClass.isAnnotationPresent(ChildEditConfig.class);
    }
}
