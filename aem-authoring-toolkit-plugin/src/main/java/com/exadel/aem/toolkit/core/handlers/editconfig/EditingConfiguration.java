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
package com.exadel.aem.toolkit.core.handlers.editconfig;

import java.util.function.BiConsumer;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;

/**
 * Generates and triggers the chain of handlers to store {@code cq:editConfig} XML markup
 */
public class EditingConfiguration {
    private EditingConfiguration() {
    }

    /**
     * Called via {@link com.exadel.aem.toolkit.core.util.PackageWriter} to build in-place editing markup based on the
     * set of component class fields
     * @param element {@code Element} representing {@code cq:editConfig} XML node
     * @param editConfig {@link EditConfig} instance
     */
    public static void append(Element element, EditConfig editConfig){
        getHandlerChain().accept(element, editConfig);
    }

    /**
     * Generates the chain of handlers to store {@code cq:editConfig} XML markup
     * @return {@code BiConsumer<Element, EditConfig>} instance
     */
    private static BiConsumer<Element, EditConfig> getHandlerChain() {
        return new PropertiesHandler()
                .andThen(new DropTargetsHandler())
                .andThen(new FormParametersHandler())
                .andThen(new InplaceEditingHandler())
                .andThen(new ListenersHandler());
    }
}
