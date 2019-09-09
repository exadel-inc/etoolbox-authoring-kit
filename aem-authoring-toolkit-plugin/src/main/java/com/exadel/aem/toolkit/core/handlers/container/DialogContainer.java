/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

public enum DialogContainer {
    FIXED_COLUMNS(new FixedColumnsHandler()),
    TABS(new TabsHandler());

    private BiConsumer<Class<?>, Element> handler;

    DialogContainer(BiConsumer<Class<?>, Element> handler) {
        this.handler = handler;
    }

    public void build(Class<?> clazz, Element parentElement){
        this.getHandler().accept(clazz, parentElement);
    }

    public BiConsumer<Class<?>, Element> getHandler() {
        return handler;
    }

    public static DialogContainer getContainer(DialogLayout layout){
        return EnumUtils.getEnumMap(DialogContainer.class).get(layout.name());
    }
}
