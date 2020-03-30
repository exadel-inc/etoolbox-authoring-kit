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

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import org.w3c.dom.Element;

import java.util.function.BiConsumer;

/**
 * Generates and triggers the chain of handlers to store {@code cq:childEditConfig} XML markup
 */
public class ChildEditingConfiguration {
    private ChildEditingConfiguration() {
    }

    /**
     * Called via {@link com.exadel.aem.toolkit.core.util.PackageWriter} to build in-place editing markup based on the
     * set of component class fields
     * @param element {@code Element} representing {@code cq:childEditConfig} XML node
     * @param childEditConfig {@link EditConfig} instance
     */
    public static void append(Element element, EditConfig childEditConfig){
        getHandlerChain().accept(element, childEditConfig);
    }

    /**
     * Generates the chain of handlers to store {@code cq:childEditConfig} XML markup
     * @return {@code BiConsumer<Element, EditConfig>} instance
     */
    private static BiConsumer<Element, EditConfig> getHandlerChain() {
        return new DropTargetsHandler()
                .andThen(new ListenersHandler());
    }
}
