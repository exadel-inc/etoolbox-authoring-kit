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
package com.exadel.aem.toolkit.core.handlers.container;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.EnumUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.core.util.writer.PackageWriter;

/**
 * Represents dialog XML markup builder for either fixed-columns or tabbed TouchUI dialog. Typically applied to by
 * the implementation of {@code ContentXmlWriter} to populate markup elements produced by the set of component class
 * fields to the overall dialog markup
 */
public enum DialogContainer {
    FIXED_COLUMNS(new FixedColumnsHandler()),
    TABS(new TabsHandler());

    private BiConsumer<Class<?>, Element> handler;

    DialogContainer(BiConsumer<Class<?>, Element> handler) {
        this.handler = handler;
    }

    /**
     * Called via {@link PackageWriter} to build dialog markup based on the set
     * of component class fields
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement XML document root element
     */
    public void build(Class<?> componentClass, Element parentElement){
        handler.accept(componentClass, parentElement);
    }

    /**
     * Returns the {@code DialogContainer} instance for the particular layout
     * @param layout {@link DialogLayout} to match this container
     * @return {@code DialogContainer} for either fixed-columns or tabbed layout
     */
    public static DialogContainer getContainer(DialogLayout layout){
        return EnumUtils.getEnumMap(DialogContainer.class).get(layout.name());
    }
}
