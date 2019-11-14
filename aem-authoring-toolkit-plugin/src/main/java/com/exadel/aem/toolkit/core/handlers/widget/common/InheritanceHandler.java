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
package com.exadel.aem.toolkit.core.handlers.widget.common;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiConsumer;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.handlers.widget.DialogComponent;

/**
 * Handler for processing Granite UI widgets features "inherited" by the current component class {@code Field} from
 * other fields via {@link Extends} mechanism
 */
public class InheritanceHandler implements BiConsumer<Element, Field> {
    private BiConsumer<Element, Field> descendantChain;
    public InheritanceHandler(BiConsumer<Element, Field> descendantChain) {
        this.descendantChain = descendantChain;
    }

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element XML element
     * @param field Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {
        if (descendantChain == null) return;
        Deque<Field> inheritanceTree = getInheritanceTree(field);
        while (!inheritanceTree.isEmpty()) {
            descendantChain.accept(element, inheritanceTree.pollLast()); // to render 'ancestors' of context field starting from next handler in chain
        }
    }

    /**
     * Builds the inheritance sequence for the current {@code Field}
     * @param field Current {@code Field} instance
     * @return Ancestral {@code Field}s, as an ordered sequence
     */
    private static Deque<Field> getInheritanceTree(Field field) {
        Deque<Field> result = new LinkedList<>();
        DialogComponent referencedComponent = DialogComponent.fromField(field).orElse(null);
        if (referencedComponent == null) {
            return result;
        }
        Extends extendsAnnotation = field.getDeclaredAnnotation(Extends.class);
        while (extendsAnnotation != null) {
            String referencedFieldName = extendsAnnotation.field().isEmpty() ? field.getName() : extendsAnnotation.field();
            try {
                Field referencedField = extendsAnnotation.value().getDeclaredField(referencedFieldName);
                if (referencedField.equals(field) || result.contains(referencedField)) { // to avoid circular references
                    break;
                }
                if (referencedComponent.equals(DialogComponent.fromField(referencedField).orElse(null))) { // to avoid mixing up props of different components
                    result.add(referencedField);
                }
                extendsAnnotation = referencedField.getDeclaredAnnotation(Extends.class);
            } catch (NoSuchFieldException e) {
                PluginRuntime.context().getExceptionHandler().handle(e);
                extendsAnnotation = null;
            }
        }
        return result;
    }
}
