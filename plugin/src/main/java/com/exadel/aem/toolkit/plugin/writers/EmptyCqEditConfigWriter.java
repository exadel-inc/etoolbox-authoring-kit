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

import java.util.function.BiConsumer;
import javax.xml.transform.Transformer;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.common.EmptyCqEditConfigHandler;

/**
 * The {@link PackageEntryWriter} implementation for creating an empty {@code _cq_editConfig.xml} file within the
 * current component folder before the package is uploaded
 */
class EmptyCqEditConfigWriter extends PackageEntryWriter {

    private static final BiConsumer<Source, Target> HANDLER = new EmptyCqEditConfigHandler();

    /**
     * Basic constructor
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    EmptyCqEditConfigWriter(Transformer transformer) {
        super(transformer);
    }

    /**
     * Gets {@code Scope} value of the current {@code PackageEntryWriter} implementation
     * @return String value representing a valid scope
     */
    @Override
    String getScope() {
        return Scopes.CQ_EDIT_CONFIG;
    }

    /**
     * Gets whether the current {@code Class} is eligible for populating a {@code _cq_editConfig.xml} structure. This
     * is a stub implementation designed to always return {@code true}
     * @param componentClass The {@code Class} under consideration
     * @return Boolean value
     */
    @Override
    boolean canProcess(Class<?> componentClass) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    BiConsumer<Source, Target> getHandlers() {
        return HANDLER;
    }
}
